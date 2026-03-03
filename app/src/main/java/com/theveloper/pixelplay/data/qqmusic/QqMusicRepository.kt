package com.theveloper.pixelplay.data.qqmusic

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.theveloper.pixelplay.data.database.AlbumEntity
import com.theveloper.pixelplay.data.database.ArtistEntity
import com.theveloper.pixelplay.data.database.MusicDao
import com.theveloper.pixelplay.data.database.QqMusicDao
import com.theveloper.pixelplay.data.database.QqMusicPlaylistEntity
import com.theveloper.pixelplay.data.database.QqMusicSongEntity
import com.theveloper.pixelplay.data.database.SongArtistCrossRef
import com.theveloper.pixelplay.data.database.SongEntity
import com.theveloper.pixelplay.data.database.toSong
import com.theveloper.pixelplay.data.network.qqmusic.QqMusicApiService
import com.theveloper.pixelplay.data.preferences.PlaylistPreferencesRepository
import com.theveloper.pixelplay.data.stream.BulkSyncResult
import com.theveloper.pixelplay.data.stream.CloudMusicUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.absoluteValue

@Singleton
class QqMusicRepository @Inject constructor(
    private val api: QqMusicApiService,
    private val dao: QqMusicDao,
    private val musicDao: MusicDao,
    private val playlistPreferencesRepository: PlaylistPreferencesRepository,
    @ApplicationContext private val context: Context
) {

    private companion object {
        private const val QQ_MUSIC_SONG_ID_OFFSET = 6_000_000_000_000L
        private const val QQ_MUSIC_ALBUM_ID_OFFSET = 7_000_000_000_000L
        private const val QQ_MUSIC_ARTIST_ID_OFFSET = 8_000_000_000_000L
        private const val QQ_MUSIC_PARENT_DIRECTORY = "/Cloud/QQMusic"
        private const val QQ_MUSIC_GENRE = "QQ Music"
        private const val QQ_MUSIC_PLAYLIST_PREFIX = "qqmusic_playlist:"
    }

    data class BulkSyncResult(
        val playlistCount: Int,
        val syncedSongCount: Int,
        val failedPlaylistCount: Int
    )

    private val prefs: SharedPreferences =
        context.getSharedPreferences("qqmusic_prefs", Context.MODE_PRIVATE)

    private val _isLoggedInFlow = MutableStateFlow(false)
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedInFlow.asStateFlow()
    private val inFlightSongUrlRequests = java.util.concurrent.ConcurrentHashMap<String, CompletableDeferred<Result<String>>>()
    private val lastSongUrlAttemptAtMs = java.util.concurrent.ConcurrentHashMap<String, Long>()
    private val songUrlRequestCooldownMs = 1500L
    private val qqSongUrlRequestMutex = Mutex()
    @Volatile
    private var lastGlobalSongUrlRequestAtMs = 0L
    private val globalSongUrlRequestIntervalMs = 1100L

    init {
        initFromSavedCookies()
        _isLoggedInFlow.value = api.hasLogin()
    }

    val isLoggedIn: Boolean
        get() = api.hasLogin()

    val userNickname: String?
        get() = prefs.getString("qqmusic_nickname", null)

    fun getPlaylists(): Flow<List<QqMusicPlaylistEntity>> = dao.getAllPlaylists()

    fun initFromSavedCookies() {
        val cookieJson = prefs.getString("qqmusic_cookies", null) ?: return
        runCatching {
            val map = jsonToMap(cookieJson)
            if (map.isNotEmpty()) {
                api.setPersistedCookies(map)
            }
        }.onFailure {
            Timber.w(it, "Failed to restore QQ Music cookies")
        }
    }

    suspend fun loginWithCookies(cookieJson: String): Result<String> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val cookies = jsonToMap(cookieJson)
                if (cookies.isEmpty()) {
                    throw IllegalStateException("No cookies captured")
                }
                prefs.edit().putString("qqmusic_cookies", cookieJson).apply()
                api.setPersistedCookies(cookies)
                if (!api.hasLogin()) {
                    throw IllegalStateException("QQ Music login cookie not detected")
                }

                val nickname = "QQ Music User"
                prefs.edit().putString("qqmusic_nickname", nickname).apply()
                _isLoggedInFlow.value = true
                nickname
            }
        }
    }

    suspend fun getSongUrl(songMid: String): Result<String> {
        val now = System.currentTimeMillis()
        val lastAttempt = lastSongUrlAttemptAtMs[songMid]
        if (lastAttempt != null && now - lastAttempt < songUrlRequestCooldownMs) {
            Timber.d("Skip QQ song URL retry due to cooldown: songMid=$songMid")
            return Result.failure(IllegalStateException("QQ song URL request throttled"))
        }
        lastSongUrlAttemptAtMs[songMid] = now

        inFlightSongUrlRequests[songMid]?.let {
            return it.await()
        }

        val requestDeferred = CompletableDeferred<Result<String>>()
        val existing = inFlightSongUrlRequests.putIfAbsent(songMid, requestDeferred)
        if (existing != null) {
            return existing.await()
        }

        val result = withContext(Dispatchers.IO) {
            runCatching {
                // Phase 1: Request without filename to get default M4A URL and discover media_mid.
                val defaultPurl = requestPurl(songMid)

                if (defaultPurl.isBlank()) {
                    throw IllegalStateException("No playable URL for songMid=$songMid (empty purl)")
                }

                val defaultUrl = if (defaultPurl.startsWith("http")) defaultPurl
                    else "https://ws.stream.qqmusic.qq.com/$defaultPurl"

                // Extract media_mid from purl (format: "C400{mediaMid}.m4a?vkey=...")
                val mediaMid = defaultPurl.substringBefore("?").drop(4).substringBefore(".")
                if (mediaMid.isNotBlank()) {
                    // Phase 2: Try MP3 320kbps with discovered media_mid.
                    val mp3Filename = "M500${mediaMid}.mp3"
                    val mp3Purl = requestPurl(songMid, filename = mp3Filename)
                    if (mp3Purl.isNotBlank()) {
                        Timber.d("Resolved QQ Music MP3 URL for songMid=$songMid")
                        return@runCatching if (mp3Purl.startsWith("http")) mp3Purl
                            else "https://ws.stream.qqmusic.qq.com/$mp3Purl"
                    }
                    Timber.d("MP3 unavailable for songMid=$songMid, falling back to M4A")
                }

                // Fallback: use the default M4A URL.
                Timber.d("Resolved QQ Music M4A URL for songMid=$songMid")
                defaultUrl
            }
        }

        requestDeferred.complete(result)
        inFlightSongUrlRequests.remove(songMid, requestDeferred)
        return result
    }

    /**
     * Make a single vkey request and return the purl string (empty if unavailable).
     * Respects the global rate-limit mutex.
     */
    private suspend fun requestPurl(songMid: String, filename: String? = null): String {
        qqSongUrlRequestMutex.withLock {
            val now = System.currentTimeMillis()
            val waitMs = globalSongUrlRequestIntervalMs - (now - lastGlobalSongUrlRequestAtMs)
            if (waitMs > 0) delay(waitMs)
            lastGlobalSongUrlRequestAtMs = System.currentTimeMillis()
        }

        val raw = api.getSongDownloadUrl(songMid, filename = filename)
        val root = JSONObject(raw)
        return root
            .optJSONObject("req_0")
            ?.optJSONObject("data")
            ?.optJSONArray("midurlinfo")
            ?.optJSONObject(0)
            ?.optString("purl", "")
            .orEmpty()
    }

    suspend fun logout() {
        api.logout()
        prefs.edit().clear().apply()
        
        // Delete all QQ Music playlists from the database
        val playlistsToDelete = dao.getAllPlaylistsList()
        playlistsToDelete.forEach { playlist ->
            dao.deleteSongsByPlaylist(playlist.id)
            dao.deletePlaylist(playlist.id)
            deleteAppPlaylistForQqMusicPlaylist(playlist.id)
        }
        
        musicDao.clearAllQqMusicSongs()
        _isLoggedInFlow.value = false
    }

    suspend fun deletePlaylistById(playlistId: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteSongsByPlaylist(playlistId)
            dao.deletePlaylist(playlistId)
            deleteAppPlaylistForQqMusicPlaylist(playlistId)
            syncUnifiedLibrarySongsFromQqMusic()
        }
    }

    // ─── Content Sync ──────────────────────────────────────────────────

    suspend fun syncUserPlaylists(): Result<List<QqMusicPlaylistEntity>> {
        Timber.d("syncUserPlaylists called, isLoggedIn=$isLoggedIn")
        if (!isLoggedIn) {
            Timber.w("syncUserPlaylists: Not logged in, aborting")
            return Result.failure(Exception("Not logged in"))
        }
        return withContext(Dispatchers.IO) {
            runCatching {
                val raw = api.getUserPlaylists()
                Timber.d("syncUserPlaylists: response length=${raw.length}")
                val root = JSONObject(raw)
                val code = root.optInt("code", -1)
                if (code != 0) {
                    throw Exception("QQ Music API Error: code=$code. Check your login.")
                }

                val data = root.optJSONObject("data") ?: return@runCatching emptyList()
                val cdlist = data.optJSONArray("cdlist") ?: return@runCatching emptyList()

                val entities = mutableListOf<QqMusicPlaylistEntity>()
                for (i in 0 until cdlist.length()) {
                    val pl = cdlist.optJSONObject(i) ?: continue
                    val songCount = pl.optInt("songnum", 0)
                    // Skip empty playlists (deleted or not yet populated)
                    if (songCount == 0) continue
                    val name = decodeBase64IfNeeded(pl.optString("dissname", ""))
                    // Skip playlists with blank names
                    if (name.isBlank()) continue
                    entities.add(
                        QqMusicPlaylistEntity(
                            id = pl.optLong("dissid", 0L),
                            name = name,
                            coverUrl = pl.optString("logo", ""),
                            songCount = songCount,
                            lastSyncTime = System.currentTimeMillis()
                        )
                    )
                }

                val localPlaylists = dao.getAllPlaylistsList()
                val remoteIds = entities.map { it.id }.toSet()
                val stalePlaylists = localPlaylists.filter { it.id !in remoteIds }

                stalePlaylists.forEach { stale ->
                    dao.deleteSongsByPlaylist(stale.id)
                    dao.deletePlaylist(stale.id)
                    deleteAppPlaylistForQqMusicPlaylist(stale.id)
                }

                entities.forEach { dao.insertPlaylist(it) }
                
                if (stalePlaylists.isNotEmpty()) {
                    syncUnifiedLibrarySongsFromQqMusic()
                }
                
                entities
            }
        }
    }

    suspend fun syncPlaylistSongs(playlistId: Long): Result<Int> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val raw = api.getPlaylistDetail(playlistId)
                val root = JSONObject(raw)

                val code = root.optInt("code", -1)
                if (code != 0) {
                    throw Exception("API error code=$code")
                }

                val cdlist = root.optJSONArray("cdlist") ?: throw Exception("No cdlist")
                val firstCd = cdlist.optJSONObject(0) ?: throw Exception("Empty cdlist")
                val songlist = firstCd.optJSONArray("songlist") ?: return@runCatching 0

                val entities = mutableListOf<QqMusicSongEntity>()
                for (i in 0 until songlist.length()) {
                    val track = songlist.optJSONObject(i) ?: continue
                    entities.add(parseTrackToEntity(track, playlistId))
                }

                dao.deleteSongsByPlaylist(playlistId)
                dao.insertSongs(entities)

                // Update app playlist
                val playlistName = entities.firstOrNull()?.let { "QQ Music Playlist" } ?: "Playlist $playlistId"
                // Ideally we should get the actual name from the list, but for now we search
                val name = dao.getAllPlaylistsList().find { it.id == playlistId }?.name ?: playlistName
                updateAppPlaylistForQqMusicPlaylist(playlistId, name, entities)

                syncUnifiedLibrarySongsFromQqMusic()

                Timber.d("Synced ${entities.size} songs for QQ Music playlist $playlistId")
                entities.size
            }
        }
    }

    suspend fun syncAllPlaylistsAndSongs(): Result<BulkSyncResult> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val playlistResult = syncUserPlaylists().getOrElse { return@runCatching BulkSyncResult(0, 0, 0) }
                if (playlistResult.isEmpty()) {
                    return@runCatching BulkSyncResult(0, 0, 0)
                }

                var syncedSongCount = 0
                var failedPlaylistCount = 0

                playlistResult.forEach { playlist ->
                    val songSyncResult = syncPlaylistSongs(playlist.id)
                    songSyncResult.fold(
                        onSuccess = { count -> syncedSongCount += count },
                        onFailure = {
                            failedPlaylistCount += 1
                            Timber.w(it, "Failed syncing QQ playlist ${playlist.id}")
                        }
                    )
                }

                syncUnifiedLibrarySongsFromQqMusic()

                BulkSyncResult(
                    playlistCount = playlistResult.size,
                    syncedSongCount = syncedSongCount,
                    failedPlaylistCount = failedPlaylistCount
                )
            }
        }
    }

    private fun parseTrackToEntity(track: JSONObject, playlistId: Long): QqMusicSongEntity {
        // Handle both older and newer field names
        val mid = track.optString("songmid", track.optString("mid", ""))
        val title = decodeBase64IfNeeded(track.optString("songname", track.optString("title", "Unknown")))
        
        val singerArray = track.optJSONArray("singer")
        val artist = if (singerArray != null && singerArray.length() > 0) {
            val names = mutableListOf<String>()
            for (i in 0 until singerArray.length()) {
                names.add(decodeBase64IfNeeded(singerArray.optJSONObject(i)?.optString("name", "") ?: ""))
            }
            names.filter { it.isNotBlank() }.joinToString(", ")
        } else {
            "Unknown Artist"
        }

        val album = decodeBase64IfNeeded(track.optString("albumname", track.optJSONObject("album")?.optString("name", "") ?: ""))
        val albumMid = track.optString("albummid", track.optJSONObject("album")?.optString("mid", "") ?: "")
        val duration = track.optLong("interval", 0) * 1000L  // Convert to milliseconds

        return QqMusicSongEntity(
            id = "${playlistId}_$mid",
            songMid = mid,
            playlistId = playlistId,
            title = title,
            artist = artist,
            album = album,
            albumMid = albumMid,
            duration = duration,
            albumArtUrl = if (albumMid.isNotBlank()) {
                "https://y.qq.com/music/photo_new/T002R300x300M000$albumMid.jpg"
            } else null,
            mimeType = "audio/mpeg",
            bitrate = null,
            dateAdded = System.currentTimeMillis()
        )
    }

    /**
     * Decode Base64-encoded string if it looks like Base64.
     * QQ Music FCG endpoints return some fields as Base64 after Zlib decompression.
     */
    private fun decodeBase64IfNeeded(input: String): String {
        if (input.isBlank()) return input
        // Check if it looks like Base64: only contains A-Za-z0-9+/= and no Chinese/special chars
        val base64Pattern = Regex("^[A-Za-z0-9+/=]+$")
        if (!base64Pattern.matches(input)) return input
        // Must be at least 4 chars and valid length for Base64
        if (input.length < 4) return input
        return try {
            val decoded = Base64.decode(input, Base64.DEFAULT)
            val result = String(decoded, Charsets.UTF_8)
            // Verify the decoded result contains actual readable text
            if (result.isNotBlank() && !result.contains('\u0000')) result else input
        } catch (_: Exception) {
            input
        }
    }

    private fun jsonToMap(json: String): Map<String, String> =
        CloudMusicUtils.jsonToMap(json)

    // ─── Unified Library Sync ──────────────────────────────────────────

    private suspend fun syncUnifiedLibrarySongsFromQqMusic() {
        val qqMusicSongs = dao.getAllQqMusicSongsList()
        val existingUnifiedIds = musicDao.getAllQqMusicSongIds()

        if (qqMusicSongs.isEmpty()) {
            if (existingUnifiedIds.isNotEmpty()) {
                musicDao.clearAllQqMusicSongs()
            }
            return
        }

        val songs = ArrayList<SongEntity>(qqMusicSongs.size)
        val artists = LinkedHashMap<Long, ArtistEntity>()
        val albums = LinkedHashMap<Long, AlbumEntity>()
        val crossRefs = mutableListOf<SongArtistCrossRef>()

        qqMusicSongs.forEach { qqSong ->
            val songId = toUnifiedSongId(qqSong.songMid)
            val artistNames = parseArtistNames(qqSong.artist)
            val primaryArtistName = artistNames.firstOrNull() ?: "Unknown Artist"
            val primaryArtistId = toUnifiedArtistId(primaryArtistName)

            artistNames.forEachIndexed { index, artistName ->
                val artistId = toUnifiedArtistId(artistName)
                artists.putIfAbsent(
                    artistId,
                    ArtistEntity(
                        id = artistId,
                        name = artistName,
                        trackCount = 0,
                        imageUrl = null
                    )
                )
                crossRefs.add(
                    SongArtistCrossRef(
                        songId = songId,
                        artistId = artistId,
                        isPrimary = index == 0
                    )
                )
            }

            val albumId = toUnifiedAlbumId(qqSong.albumMid ?: "", qqSong.album)
            val albumName = qqSong.album.ifBlank { "Unknown Album" }
            albums.putIfAbsent(
                albumId,
                AlbumEntity(
                    id = albumId,
                    title = albumName,
                    artistName = primaryArtistName,
                    artistId = primaryArtistId,
                    songCount = 0,
                    year = 0,
                    albumArtUriString = qqSong.albumArtUrl
                )
            )

            songs.add(
                SongEntity(
                    id = songId,
                    title = qqSong.title,
                    artistName = qqSong.artist.ifBlank { primaryArtistName },
                    artistId = primaryArtistId,
                    albumArtist = null,
                    albumName = albumName,
                    albumId = albumId,
                    contentUriString = "qqmusic://${qqSong.songMid}",
                    albumArtUriString = qqSong.albumArtUrl,
                    duration = qqSong.duration,
                    genre = QQ_MUSIC_GENRE,
                    filePath = "",
                    parentDirectoryPath = QQ_MUSIC_PARENT_DIRECTORY,
                    isFavorite = false,
                    lyrics = null,
                    trackNumber = 0,
                    year = 0,
                    dateAdded = qqSong.dateAdded.takeIf { it > 0 } ?: System.currentTimeMillis(),
                    mimeType = qqSong.mimeType,
                    bitrate = qqSong.bitrate,
                    sampleRate = null,
                    telegramChatId = null,
                    telegramFileId = null
                )
            )
        }

        val albumCounts = songs.groupingBy { it.albumId }.eachCount()
        val finalAlbums = albums.values.map { album ->
            album.copy(songCount = albumCounts[album.id] ?: 0)
        }

        val currentUnifiedIds = songs.map { it.id }.toSet()
        val deletedUnifiedIds = existingUnifiedIds.filter { it !in currentUnifiedIds }

        musicDao.incrementalSyncMusicData(
            songs = songs,
            albums = finalAlbums,
            artists = artists.values.toList(),
            crossRefs = crossRefs,
            deletedSongIds = deletedUnifiedIds
        )
    }

    private fun parseArtistNames(rawArtist: String): List<String> =
        CloudMusicUtils.parseArtistNames(rawArtist)

    private fun toUnifiedSongId(mid: String): Long {
        val hash = mid.hashCode().toLong().absoluteValue
        return -(QQ_MUSIC_SONG_ID_OFFSET + hash)
    }

    private fun toUnifiedAlbumId(albumMid: String, albumName: String): Long {
        val normalized = if (albumMid.isNotBlank()) albumMid.hashCode().toLong().absoluteValue 
                         else albumName.lowercase().hashCode().toLong().absoluteValue
        return -(QQ_MUSIC_ALBUM_ID_OFFSET + normalized)
    }

    private fun toUnifiedArtistId(artistName: String): Long {
        return -(QQ_MUSIC_ARTIST_ID_OFFSET + artistName.lowercase().hashCode().toLong().absoluteValue)
    }

    // ─── App Playlist Management ────────────────────────────────────────

    private suspend fun getAppPlaylistIdForQqMusic(qqPlaylistId: Long): String {
        return "$QQ_MUSIC_PLAYLIST_PREFIX$qqPlaylistId"
    }

    private suspend fun updateAppPlaylistForQqMusicPlaylist(
        qqPlaylistId: Long,
        playlistName: String,
        qqEntities: List<QqMusicSongEntity>
    ) {
        try {
            val unifiedSongIds = qqEntities.map { entity ->
                toUnifiedSongId(entity.songMid).toString()
            }

            val appPlaylistId = getAppPlaylistIdForQqMusic(qqPlaylistId)
            val allPlaylists = playlistPreferencesRepository.userPlaylistsFlow
            val existingPlaylist = withContext(Dispatchers.IO) {
                allPlaylists.map { playlists ->
                    playlists.find { it.id == appPlaylistId }
                }.first()
            }

            if (existingPlaylist != null) {
                playlistPreferencesRepository.updatePlaylist(
                    existingPlaylist.copy(
                        name = playlistName,
                        songIds = unifiedSongIds,
                        lastModified = System.currentTimeMillis(),
                        source = "QQMUSIC"
                    )
                )
                Timber.d("Updated app playlist for QQ Music playlist $qqPlaylistId: $playlistName")
            } else {
                playlistPreferencesRepository.createPlaylist(
                    name = playlistName,
                    songIds = unifiedSongIds,
                    customId = appPlaylistId,
                    source = "QQMUSIC"
                )
                Timber.d("Created new app playlist for QQ Music playlist $qqPlaylistId: $playlistName")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to update/create app playlist for QQ Music playlist $qqPlaylistId")
        }
    }

    private suspend fun deleteAppPlaylistForQqMusicPlaylist(qqPlaylistId: Long) {
        try {
            val appPlaylistId = getAppPlaylistIdForQqMusic(qqPlaylistId)
            playlistPreferencesRepository.deletePlaylist(appPlaylistId)
            Timber.d("Deleted app playlist for QQ Music playlist $qqPlaylistId")
        } catch (e: Exception) {
            Timber.w(e, "Failed to delete app playlist for QQ Music playlist $qqPlaylistId")
        }
    }
}
