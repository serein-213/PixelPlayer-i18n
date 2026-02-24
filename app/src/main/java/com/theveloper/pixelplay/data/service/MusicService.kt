package com.theveloper.pixelplay.data.service

import android.app.ForegroundServiceStartNotAllowedException
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.theveloper.pixelplay.MainActivity
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.model.PlayerInfo
import com.theveloper.pixelplay.data.preferences.UserPreferencesRepository
import com.theveloper.pixelplay.data.repository.MusicRepository
import com.theveloper.pixelplay.data.service.player.DualPlayerEngine
import com.theveloper.pixelplay.data.service.player.TransitionController
import com.theveloper.pixelplay.ui.glancewidget.ControlWidget4x2
import com.theveloper.pixelplay.ui.glancewidget.PixelPlayGlanceWidget
import com.theveloper.pixelplay.ui.glancewidget.PlayerActions
import com.theveloper.pixelplay.ui.glancewidget.PlayerInfoStateDefinition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import com.theveloper.pixelplay.data.equalizer.EqualizerManager
import com.theveloper.pixelplay.data.model.WidgetThemeColors
import com.theveloper.pixelplay.data.preferences.AlbumArtPaletteStyle
import com.theveloper.pixelplay.presentation.viewmodel.ColorSchemeProcessor
import androidx.compose.ui.graphics.toArgb
import com.theveloper.pixelplay.ui.glancewidget.BarWidget4x1
import com.theveloper.pixelplay.ui.glancewidget.GridWidget2x2
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import com.theveloper.pixelplay.data.preferences.ThemePreference
import com.theveloper.pixelplay.data.service.auto.AutoMediaBrowseTree
import com.theveloper.pixelplay.presentation.viewmodel.ColorSchemePair
import com.theveloper.pixelplay.utils.MediaItemBuilder

import javax.inject.Inject

// Acciones personalizadas para compatibilidad con el widget existente


@UnstableApi
@AndroidEntryPoint
class MusicService : MediaLibraryService() {

    @Inject
    lateinit var engine: DualPlayerEngine
    @Inject
    lateinit var controller: TransitionController
    @Inject
    lateinit var musicRepository: MusicRepository
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository
    @Inject
    lateinit var equalizerManager: EqualizerManager
    @Inject
    lateinit var colorSchemeProcessor: ColorSchemeProcessor
    @Inject
    lateinit var autoMediaBrowseTree: AutoMediaBrowseTree

    private var favoriteSongIds = emptySet<String>()
    private var mediaSession: MediaLibraryService.MediaLibrarySession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var keepPlayingInBackground = true
    private var isManualShuffleEnabled = false
    private var persistentShuffleEnabled = false
    // Holds the previous main-thread UncaughtExceptionHandler so we can restore it in onDestroy.
    private var previousMainThreadExceptionHandler: Thread.UncaughtExceptionHandler? = null
    // --- Counted Play State ---
    private var countedPlayActive = false
    private var countedPlayTarget = 0
    private var countedPlayCount = 0
    private var countedOriginalId: String? = null
    private var countedPlayListener: Player.Listener? = null

    companion object {
        private const val TAG = "MusicService_PixelPlay"
        const val NOTIFICATION_ID = 101
        const val ACTION_SLEEP_TIMER_EXPIRED = "com.theveloper.pixelplay.ACTION_SLEEP_TIMER_EXPIRED"
    }

    override fun onCreate() {
        // Media3's Cast SDK callback path (MediaSessionImpl$$ExternalSyntheticLambda →
        // Util.postOrRun → MediaNotificationManager.updateNotificationInternal) calls
        // Service.startForeground() directly, bypassing onUpdateNotification() entirely.
        // Since startForeground() is final we cannot override it. Instead we intercept
        // ForegroundServiceStartNotAllowedException on the main thread before it reaches
        // ActivityThread and crashes the process.
        val existingHandler = Thread.currentThread().uncaughtExceptionHandler
        previousMainThreadExceptionHandler = existingHandler
        Thread.currentThread().setUncaughtExceptionHandler { thread, throwable ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                throwable is ForegroundServiceStartNotAllowedException
            ) {
                Timber.tag(TAG).w(throwable, "Suppressed ForegroundServiceStartNotAllowedException from Media3/Cast internal path")
            } else {
                existingHandler?.uncaughtException(thread, throwable)
            }
        }

        super.onCreate()
        
        // Ensure engine is ready (re-initialize if service was restarted)
        engine.initialize()

        engine.masterPlayer.addListener(playerListener)

        // Handle player swaps (crossfade) to keep MediaSession in sync
        engine.addPlayerSwapListener { newPlayer ->
            serviceScope.launch(Dispatchers.Main) {
                val oldPlayer = mediaSession?.player
                oldPlayer?.removeListener(playerListener)

                mediaSession?.player = newPlayer
                newPlayer.addListener(playerListener)

                Timber.tag("MusicService").d("Swapped MediaSession player to new instance.")
                requestWidgetFullUpdate(force = true)
                mediaSession?.let { refreshMediaSessionUi(it) }
            }
        }

        controller.initialize()

        // Restore equalizer state from preferences and attach to audio session.
        // This ensures the equalizer is active even before the user opens the EQ screen.
        serviceScope.launch {
            val eqEnabled = userPreferencesRepository.equalizerEnabledFlow.first()
            val presetName = userPreferencesRepository.equalizerPresetFlow.first()
            val customBands = userPreferencesRepository.equalizerCustomBandsFlow.first()
            val bassBoostEnabled = userPreferencesRepository.bassBoostEnabledFlow.first()
            val bassBoostStrength = userPreferencesRepository.bassBoostStrengthFlow.first()
            val virtualizerEnabled = userPreferencesRepository.virtualizerEnabledFlow.first()
            val virtualizerStrength = userPreferencesRepository.virtualizerStrengthFlow.first()
            val loudnessEnabled = userPreferencesRepository.loudnessEnhancerEnabledFlow.first()
            val loudnessStrength = userPreferencesRepository.loudnessEnhancerStrengthFlow.first()

            equalizerManager.restoreState(
                eqEnabled, presetName, customBands,
                bassBoostEnabled, bassBoostStrength,
                virtualizerEnabled, virtualizerStrength,
                loudnessEnabled, loudnessStrength
            )

            val sessionId = engine.getAudioSessionId()
            if (sessionId != 0) {
                equalizerManager.attachToAudioSession(sessionId)
            }

            // Re-attach equalizer whenever the active audio session changes (e.g. crossfade)
            engine.activeAudioSessionId.collect { newSessionId ->
                if (newSessionId != 0) {
                    equalizerManager.attachToAudioSession(newSessionId)
                }
            }
        }

        serviceScope.launch {
            userPreferencesRepository.keepPlayingInBackgroundFlow.collect { enabled ->
                keepPlayingInBackground = enabled
            }
        }

        serviceScope.launch {
            userPreferencesRepository.persistentShuffleEnabledFlow.collect { enabled ->
                persistentShuffleEnabled = enabled
            }
        }

        // Initialize shuffle state from preferences
        serviceScope.launch {
            val persistent = userPreferencesRepository.persistentShuffleEnabledFlow.first()
            if (persistent) {
                isManualShuffleEnabled = userPreferencesRepository.isShuffleOnFlow.first()
                mediaSession?.let { refreshMediaSessionUi(it) }
            }
        }

        val callback = object : MediaLibrarySession.Callback {
            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                val defaultResult = super.onConnect(session, controller)
                val customCommands = listOf(
                    MusicNotificationProvider.CUSTOM_COMMAND_LIKE,
                    MusicNotificationProvider.CUSTOM_COMMAND_SHUFFLE_ON,
                    MusicNotificationProvider.CUSTOM_COMMAND_SHUFFLE_OFF,
                    MusicNotificationProvider.CUSTOM_COMMAND_SET_SHUFFLE_STATE,
                    MusicNotificationProvider.CUSTOM_COMMAND_CYCLE_REPEAT_MODE,
                    MusicNotificationProvider.CUSTOM_COMMAND_COUNTED_PLAY
                ).map { SessionCommand(it, Bundle.EMPTY) }

                val sessionCommandsBuilder = SessionCommands.Builder()
                    .addSessionCommands(defaultResult.availableSessionCommands.commands)
                customCommands.forEach { sessionCommandsBuilder.add(it) }

                return MediaSession.ConnectionResult.accept(
                    sessionCommandsBuilder.build(),
                    defaultResult.availablePlayerCommands
                )
            }

            override fun onCustomCommand(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                customCommand: SessionCommand,
                args: Bundle
            ): ListenableFuture<SessionResult> {
                Timber.tag("MusicService")
                    .d("onCustomCommand received: ${customCommand.customAction}")
                when (customCommand.customAction) {
                    MusicNotificationProvider.CUSTOM_COMMAND_COUNTED_PLAY -> {
                        val count = args.getInt("count", 1)
                        startCountedPlay(count)
                    }
                    MusicNotificationProvider.CUSTOM_COMMAND_CANCEL_COUNTED_PLAY -> {
                        stopCountedPlay()
                    }
                    MusicNotificationProvider.CUSTOM_COMMAND_SHUFFLE_ON -> {
                        Timber.tag("MusicService")
                            .d("Executing SHUFFLE_ON. Current shuffleMode: ${session.player.shuffleModeEnabled}")
                        updateManualShuffleState(session, enabled = true, broadcast = true)
                    }
                    MusicNotificationProvider.CUSTOM_COMMAND_SHUFFLE_OFF -> {
                        Timber.tag("MusicService")
                            .d("Executing SHUFFLE_OFF. Current shuffleMode: ${session.player.shuffleModeEnabled}")
                        updateManualShuffleState(session, enabled = false, broadcast = true)
                    }
                    MusicNotificationProvider.CUSTOM_COMMAND_SET_SHUFFLE_STATE -> {
                        val enabled = args.getBoolean(
                            MusicNotificationProvider.EXTRA_SHUFFLE_ENABLED,
                            false
                        )
                        updateManualShuffleState(session, enabled = enabled, broadcast = false)
                    }
                    MusicNotificationProvider.CUSTOM_COMMAND_CYCLE_REPEAT_MODE -> {
                        val currentMode = session.player.repeatMode
                        val newMode = when (currentMode) {
                            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                            else -> Player.REPEAT_MODE_OFF
                        }
                        session.player.repeatMode = newMode
                        refreshMediaSessionUi(session)
                    }
                    MusicNotificationProvider.CUSTOM_COMMAND_LIKE -> {
                        val songId = session.player.currentMediaItem?.mediaId ?: return@onCustomCommand Futures.immediateFuture(SessionResult(
                            SessionError.ERROR_UNKNOWN))
                        Timber.tag("MusicService").d("Executing LIKE for songId: $songId")
                        val isCurrentlyFavorite = favoriteSongIds.contains(songId)
                        val targetFavoriteState = !isCurrentlyFavorite
                        favoriteSongIds = if (isCurrentlyFavorite) {
                            favoriteSongIds - songId
                        } else {
                            favoriteSongIds + songId
                        }

                        refreshMediaSessionUi(session)

                        serviceScope.launch {
                            Timber.tag("MusicService").d("Toggling favorite status for $songId")
                            musicRepository.setFavoriteStatus(songId, targetFavoriteState)
                            userPreferencesRepository.setFavoriteSong(songId, targetFavoriteState)
                            Timber.tag("MusicService")
                                .d("Toggled favorite status. Updating notification.")
                            refreshMediaSessionUi(session)
                        }
                    }
                }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }

            // --- Android Auto: Media Library Browsing ---

            override fun onGetLibraryRoot(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                params: MediaLibraryService.LibraryParams?
            ): ListenableFuture<LibraryResult<MediaItem>> {
                val rootItem = MediaItem.Builder()
                    .setMediaId(AutoMediaBrowseTree.ROOT_ID)
                    .setMediaMetadata(
                        androidx.media3.common.MediaMetadata.Builder()
                            .setTitle("PixelPlay")
                            .setIsBrowsable(true)
                            .setIsPlayable(false)
                            .setMediaType(androidx.media3.common.MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                            .build()
                    )
                    .build()
                return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
            }

            override fun onGetChildren(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                parentId: String,
                page: Int,
                pageSize: Int,
                params: MediaLibraryService.LibraryParams?
            ): ListenableFuture<LibraryResult<com.google.common.collect.ImmutableList<MediaItem>>> {
                return serviceScope.future {
                    try {
                        val children = autoMediaBrowseTree.getChildren(parentId, page, pageSize)
                        LibraryResult.ofItemList(children, params)
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "onGetChildren failed for parentId=$parentId")
                        LibraryResult.ofError(SessionError.ERROR_UNKNOWN)
                    }
                }
            }

            override fun onGetItem(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                mediaId: String
            ): ListenableFuture<LibraryResult<MediaItem>> {
                return serviceScope.future {
                    try {
                        val item = autoMediaBrowseTree.getItem(mediaId)
                        if (item != null) {
                            LibraryResult.ofItem(item, null)
                        } else {
                            LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
                        }
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "onGetItem failed for mediaId=$mediaId")
                        LibraryResult.ofError(SessionError.ERROR_UNKNOWN)
                    }
                }
            }

            override fun onSearch(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                query: String,
                params: MediaLibraryService.LibraryParams?
            ): ListenableFuture<LibraryResult<Void>> {
                // Signal that search is supported; results delivered via onGetSearchResult
                return Futures.immediateFuture(LibraryResult.ofVoid())
            }

            override fun onGetSearchResult(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                query: String,
                page: Int,
                pageSize: Int,
                params: MediaLibraryService.LibraryParams?
            ): ListenableFuture<LibraryResult<com.google.common.collect.ImmutableList<MediaItem>>> {
                return serviceScope.future {
                    try {
                        val allResults = autoMediaBrowseTree.search(query)
                        val effectivePage = page.coerceAtLeast(0)
                        val effectivePageSize = if (pageSize > 0) pageSize else Int.MAX_VALUE
                        val offset = (effectivePage.toLong() * effectivePageSize.toLong())
                            .coerceAtMost(Int.MAX_VALUE.toLong())
                            .toInt()
                        val pagedResults = allResults
                            .drop(offset)
                            .take(effectivePageSize)

                        LibraryResult.ofItemList(pagedResults, params)
                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e, "onGetSearchResult failed for query=$query")
                        LibraryResult.ofError(SessionError.ERROR_UNKNOWN)
                    }
                }
            }

            override fun onAddMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: MutableList<MediaItem>
            ): ListenableFuture<MutableList<MediaItem>> {
                return serviceScope.future {
                    val resolvedItems = mediaItems.mapNotNull { requestedItem ->
                        val songId = requestedItem.mediaId
                        val song = musicRepository.getSong(songId).first()
                        song?.let { MediaItemBuilder.build(it) }
                    }.toMutableList()
                    resolvedItems
                }
            }
        }

        mediaSession = MediaLibrarySession.Builder(this, engine.masterPlayer, callback)
            .setSessionActivity(getOpenAppPendingIntent())
            .setBitmapLoader(CoilBitmapLoader(this, serviceScope))
            .build()

        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .build()
                .also { it.setSmallIcon(R.drawable.monochrome_player) }
        )
        mediaSession?.let { refreshMediaSessionUi(it) }

        serviceScope.launch {
            userPreferencesRepository.favoriteSongIdsFlow.collect { ids ->
                Timber.tag("MusicService")
                    .d("favoriteSongIdsFlow collected. New ids size: ${ids.size}")
                val oldIds = favoriteSongIds
                favoriteSongIds = ids
                val currentSongId = mediaSession?.player?.currentMediaItem?.mediaId
                if (currentSongId != null) {
                    val wasFavorite = oldIds.contains(currentSongId)
                    val isFavorite = ids.contains(currentSongId)
                    if (wasFavorite != isFavorite) {
                        Timber.tag("MusicService")
                            .d("Favorite status changed for current song. Updating notification.")
                        mediaSession?.let { refreshMediaSessionUi(it) }
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            val player = mediaSession?.player ?: return@let
            when (action) {
                PlayerActions.PLAY_PAUSE -> player.playWhenReady = !player.playWhenReady
                PlayerActions.NEXT -> player.seekToNext()
                PlayerActions.PREVIOUS -> player.seekToPrevious()
                PlayerActions.PLAY_FROM_QUEUE -> {
                    val songId = intent.getLongExtra("song_id", -1L)
                    if (songId != -1L) {
                        val timeline = player.currentTimeline
                        if (!timeline.isEmpty) {
                            val window = androidx.media3.common.Timeline.Window()
                            for (i in 0 until timeline.windowCount) {
                                timeline.getWindow(i, window)
                                if (window.mediaItem.mediaId.toLongOrNull() == songId) {
                                    player.seekTo(i, C.TIME_UNSET)
                                    player.play()
                                    break
                                }
                            }
                        }
                    }
                }
                PlayerActions.SHUFFLE -> {
                    val newState = !isManualShuffleEnabled
                    mediaSession?.let { session ->
                        updateManualShuffleState(session, enabled = newState, broadcast = true)
                    }
                }
                PlayerActions.REPEAT -> {
                    val newMode = when (player.repeatMode) {
                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                        Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                        else -> Player.REPEAT_MODE_OFF
                    }
                    player.repeatMode = newMode
                    requestWidgetFullUpdate(force = true)
                }
                ACTION_SLEEP_TIMER_EXPIRED -> {
                    Timber.tag(TAG).d("Sleep timer expired action received. Pausing player.")
                    player.pause()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val player = engine.masterPlayer
            Timber.tag(TAG).d("onIsPlayingChanged: $isPlaying. Duration: ${player.duration}, Seekable: ${player.isCurrentMediaItemSeekable}")
            requestWidgetFullUpdate()
            mediaSession?.let { refreshMediaSessionUi(it) }
        }
        
        override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
             val canSeek = availableCommands.contains(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
             val player = engine.masterPlayer
             Timber.tag(TAG).w("onAvailableCommandsChanged. Can Seek Command? $canSeek. IsSeekable? ${player.isCurrentMediaItemSeekable}. Duration: ${player.duration}")
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            Timber.tag(TAG).d("Playback state changed: $playbackState")
            mediaSession?.let { refreshMediaSessionUi(it) }
        }

        override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
            requestWidgetFullUpdate(force = true)
            mediaSession?.let { refreshMediaSessionUi(it) }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            Timber.tag("MusicService")
                .d("playerListener.onShuffleModeEnabledChanged: $shuffleModeEnabled")
            requestWidgetFullUpdate(force = true)
            mediaSession?.let { refreshMediaSessionUi(it) }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            requestWidgetFullUpdate(force = true)
            mediaSession?.let { refreshMediaSessionUi(it) }
        }

        override fun onPlayerError(error: PlaybackException) {
            Timber.tag(TAG).e(error, "Error en el reproductor: ")
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        val allowBackground = keepPlayingInBackground

        if (!allowBackground) {
            player?.apply {
                playWhenReady = false
                stop()
                clearMediaItems()
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            super.onTaskRemoved(rootIntent)
            return
        }

        if (player == null || !player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            release()
            mediaSession = null
        }
        engine.release()
        controller.release()
        serviceScope.cancel()
        Thread.currentThread().setUncaughtExceptionHandler(previousMainThreadExceptionHandler)
        previousMainThreadExceptionHandler = null
        super.onDestroy()
    }

    private fun getOpenAppPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("ACTION_SHOW_PLAYER", true) // Signal to MainActivity to show the player
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // --- LÓGICA PARA ACTUALIZACIÓN DE WIDGETS Y DATOS ---
    private var debouncedWidgetUpdateJob: Job? = null
    private val WIDGET_STATE_DEBOUNCE_MS = 300L

    private fun requestWidgetFullUpdate(force: Boolean = false) {
        debouncedWidgetUpdateJob?.cancel()
        debouncedWidgetUpdateJob = serviceScope.launch {
            if (!force) {
                delay(WIDGET_STATE_DEBOUNCE_MS)
            }
            processWidgetUpdateInternal()
        }
    }

    private suspend fun processWidgetUpdateInternal() {
        val playerInfo = buildPlayerInfo()
        updateGlanceWidgets(playerInfo)
    }

    private suspend fun buildPlayerInfo(): PlayerInfo {
        val player = engine.masterPlayer
        // Batch all main-thread reads into a single context switch (was 7 separate hops → 1)
        var currentItem: MediaItem? = null
        var isPlaying = false
        var repeatMode = Player.REPEAT_MODE_OFF
        var currentPosition = 0L
        var totalDuration = 0L
        var snapshotWindowIndex = 0
        var snapshotTimeline: androidx.media3.common.Timeline = androidx.media3.common.Timeline.EMPTY
        withContext(Dispatchers.Main) {
            currentItem = player.currentMediaItem
            isPlaying = player.isPlaying
            repeatMode = player.repeatMode
            currentPosition = player.currentPosition
            totalDuration = player.duration.coerceAtLeast(0)
            snapshotWindowIndex = player.currentMediaItemIndex
            snapshotTimeline = player.currentTimeline
        }
        val shuffleEnabled = isManualShuffleEnabled // Manual shuffle for sync with PlayerViewModel

        val title = currentItem?.mediaMetadata?.title?.toString().orEmpty()
        val artist = currentItem?.mediaMetadata?.artist?.toString().orEmpty()
        val mediaId = currentItem?.mediaId
        val artworkUri = currentItem?.mediaMetadata?.artworkUri
        val artworkData = currentItem?.mediaMetadata?.artworkData

        val (artBytes, artUriString) = getAlbumArtForWidget(artworkData, artworkUri)

        // Merge two IO preference reads into a single context switch
        val (playerTheme, paletteStyle) = withContext(Dispatchers.IO) {
            Pair(
                userPreferencesRepository.playerThemePreferenceFlow.first(),
                AlbumArtPaletteStyle.fromStorageKey(userPreferencesRepository.albumArtPaletteStyleFlow.first().storageKey)
            )
        }

        val schemePair: ColorSchemePair? = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && playerTheme == ThemePreference.DYNAMIC ->
                ColorSchemePair(
                    light = dynamicLightColorScheme(applicationContext),
                    dark = dynamicDarkColorScheme(applicationContext)
                )
            artUriString != null ->
                // Skip heavy palette recomputation when art + style haven't changed
                if (artUriString == cachedSchemeArtUri && paletteStyle == cachedSchemePaletteStyle) {
                    cachedColorSchemePair
                } else {
                    colorSchemeProcessor.getOrGenerateColorScheme(artUriString, paletteStyle).also {
                        cachedSchemeArtUri = artUriString
                        cachedSchemePaletteStyle = paletteStyle
                        cachedColorSchemePair = it
                    }
                }
            else -> null
        }

        val widgetColors = schemePair?.let {
            WidgetThemeColors(
                lightSurfaceContainer = it.light.primaryContainer.toArgb(),
                lightTitle = it.light.onPrimaryContainer.toArgb(),
                lightArtist = it.light.onPrimaryContainer.copy(alpha = 0.7f).toArgb(),
                lightPlayPauseBackground = it.light.primary.toArgb(),
                lightPlayPauseIcon = it.light.onPrimary.toArgb(),
                lightPrevNextBackground = it.light.onPrimary.toArgb(),
                lightPrevNextIcon = it.light.primary.toArgb(),
                
                darkSurfaceContainer = it.dark.primaryContainer.toArgb(),
                darkTitle = it.dark.onPrimaryContainer.toArgb(),
                darkArtist = it.dark.onPrimaryContainer.copy(alpha = 0.7f).toArgb(),
                darkPlayPauseBackground = it.dark.primary.toArgb(),
                darkPlayPauseIcon = it.dark.onPrimary.toArgb(),
                darkPrevNextBackground = it.dark.onPrimary.toArgb(),
                darkPrevNextIcon = it.dark.primary.toArgb()
            )
        }

        val isFavorite = false
//        val isFavorite = mediaId?.let {
//            //musicRepository.getFavoriteSongs().firstOrNull()?.any { song -> song.id.toString() == it }
//        } ?: false

        val queueItems = mutableListOf<com.theveloper.pixelplay.data.model.QueueItem>()
        // Reuse snapshotTimeline / snapshotWindowIndex captured at the top — no extra main-thread hop
        if (!snapshotTimeline.isEmpty) {
            val window = androidx.media3.common.Timeline.Window()

            // Empezar desde la siguiente canción en la cola
            val startIndex = if (snapshotWindowIndex + 1 < snapshotTimeline.windowCount) snapshotWindowIndex + 1 else 0

            // Limitar el número de elementos de la cola a 4
            val endIndex = (startIndex + 4).coerceAtMost(snapshotTimeline.windowCount)
            for (i in startIndex until endIndex) {
                snapshotTimeline.getWindow(i, window)
                val mediaItem = window.mediaItem
                val songId = mediaItem.mediaId.toLongOrNull()
                if (songId != null) {
                    val (artBytes, _) = getAlbumArtForWidget(
                        embeddedArt = mediaItem.mediaMetadata?.artworkData,
                        artUri = mediaItem.mediaMetadata?.artworkUri
                    )
                    queueItems.add(
                        com.theveloper.pixelplay.data.model.QueueItem(
                            id = songId,
                            albumArtBitmapData = artBytes
                        )
                    )
                }
            }
        }

        return PlayerInfo(
            songTitle = title,
            artistName = artist,
            isPlaying = isPlaying,
            albumArtUri = artUriString,
            albumArtBitmapData = artBytes,
            currentPositionMs = currentPosition,
            totalDurationMs = totalDuration,
            isFavorite = isFavorite,
            queue = queueItems,
            themeColors = widgetColors,
            isShuffleEnabled = shuffleEnabled,
            repeatMode = repeatMode
        )
    }

    private val widgetArtByteArrayCache = object : LruCache<String, ByteArray>(5 * 256 * 1024) {
        override fun sizeOf(key: String, value: ByteArray): Int = value.size
    }

    // Color scheme cache: skip recomputation when art URI and palette style haven't changed
    private var cachedSchemeArtUri: String? = null
    private var cachedSchemePaletteStyle: AlbumArtPaletteStyle? = null
    private var cachedColorSchemePair: ColorSchemePair? = null

    private suspend fun getAlbumArtForWidget(embeddedArt: ByteArray?, artUri: Uri?): Pair<ByteArray?, String?> = withContext(Dispatchers.IO) {
        if (embeddedArt != null && embeddedArt.isNotEmpty()) {
            return@withContext embeddedArt to artUri?.toString()
        }
        val uri = artUri ?: return@withContext null to null
        val artUriString = uri.toString()
        val cachedArt = widgetArtByteArrayCache.get(artUriString)
        if (cachedArt != null) {
            return@withContext cachedArt to artUriString
        }
        val loadedArt = loadBitmapDataFromUri(uri = uri, context = baseContext)
        if (loadedArt != null) {
            widgetArtByteArrayCache.put(artUriString, loadedArt)
        }
        return@withContext loadedArt to artUriString
    }

    private suspend fun updateGlanceWidgets(playerInfo: PlayerInfo) = withContext(Dispatchers.IO) {
        try {
            val glanceManager = GlanceAppWidgetManager(applicationContext)

            val glanceIds = glanceManager.getGlanceIds(PixelPlayGlanceWidget::class.java)
            glanceIds.forEach { id ->
                updateAppWidgetState(applicationContext, PlayerInfoStateDefinition, id) { playerInfo }
                PixelPlayGlanceWidget().update(applicationContext, id)
            }

            val barGlanceIds = glanceManager.getGlanceIds(BarWidget4x1::class.java)
            barGlanceIds.forEach { id ->
                updateAppWidgetState(applicationContext, PlayerInfoStateDefinition, id) { playerInfo }
                BarWidget4x1().update(applicationContext, id)
            }

            val controlGlanceIds = glanceManager.getGlanceIds(ControlWidget4x2::class.java)
            controlGlanceIds.forEach { id ->
                updateAppWidgetState(applicationContext, PlayerInfoStateDefinition, id) { playerInfo }
                ControlWidget4x2().update(applicationContext, id)
            }

            val gridGlanceIds = glanceManager.getGlanceIds(GridWidget2x2::class.java)
            gridGlanceIds.forEach { id ->
                updateAppWidgetState(applicationContext, PlayerInfoStateDefinition, id) { playerInfo }
                GridWidget2x2().update(applicationContext, id)
            }
            
            if (glanceIds.isNotEmpty() || barGlanceIds.isNotEmpty() || controlGlanceIds.isNotEmpty() || gridGlanceIds.isNotEmpty()) {
                 Log.d(TAG, "Widgets actualizados: ${playerInfo.songTitle} (Original: ${glanceIds.size}, Bar: ${barGlanceIds.size}, Control: ${controlGlanceIds.size})")
            } else {
                Log.w(TAG, "No se encontraron widgets para actualizar")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar el widget", e)
        }
    }

    private suspend fun loadBitmapDataFromUri(context: Context, uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val request = ImageRequest.Builder(context).data(uri).size(Size(256, 256)).allowHardware(false).build()
            val drawable = context.imageLoader.execute(request).drawable
            drawable?.let {
                val bitmap = it.toBitmap(256, 256)
                val stream = ByteArrayOutputStream()
                // Do NOT recycle bitmap here: toBitmap() may return Coil's cached Bitmap
                // object directly. Recycling it would invalidate any copy already handed
                // to Media3, causing "Can't copy a recycled bitmap" on setMetadata().
                // Coil manages the lifecycle of its own cached bitmaps.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, stream)
                } else {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                }
                stream.toByteArray()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallo al cargar bitmap desde URI: $uri", e)
            null
        }
    }

    fun isSongFavorite(songId: String?): Boolean {
        return songId != null && favoriteSongIds.contains(songId)
    }

    fun isManualShuffleEnabled(): Boolean {
        return isManualShuffleEnabled
    }

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        val playWhenReady = session.player.playWhenReady
        val playbackState = session.player.playbackState

        // Android 12+ (API 31+): Only request foreground when actively playing.
        // This prevents requesting foreground start when player is idle/ended.
        val shouldStartInForeground = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startInForegroundRequired && playWhenReady
                    && playbackState != Player.STATE_IDLE
                    && playbackState != Player.STATE_ENDED
        } else {
            startInForegroundRequired
        }

        try {
            super.onUpdateNotification(session, shouldStartInForeground)
        } catch (e: Exception) {
            Timber.tag(TAG).w(e, "onUpdateNotification suppressed: ${e.message}")
        }
    }

    override fun startForegroundService(serviceIntent: Intent?): ComponentName? {
        // Android 12+ (API 31+): Media3 calls startForegroundService asynchronously
        // (e.g. after bitmap loading or Cast SDK callbacks). By that time the app may
        // already be in the background, causing ForegroundServiceStartNotAllowedException.
        // Catch the exception and fall back to startService — if the service is already
        // foreground, the subsequent Service.startForeground() call will just update
        // the notification without throwing.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return try {
                super.startForegroundService(serviceIntent)
            } catch (e: ForegroundServiceStartNotAllowedException) {
                Timber.tag(TAG).w(e, "startForegroundService not allowed, falling back to startService")
                startService(serviceIntent)
            }
        }
        return super.startForegroundService(serviceIntent)
    }

    private fun refreshMediaSessionUi(session: MediaSession) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val player = session.player
            val playbackState = player.playbackState
            val isActivelyPlaying = player.playWhenReady &&
                    playbackState != Player.STATE_IDLE &&
                    playbackState != Player.STATE_ENDED
            if (!isActivelyPlaying) {
                Timber.tag(TAG).d(
                    "Skipping media button preference update on API 31+ while inactive: " +
                            "playWhenReady=${player.playWhenReady}, state=$playbackState"
                )
                return
            }
        }

        val buttons = buildMediaButtonPreferences(session)
        // setMediaButtonPreferences triggers a notification update internally via
        // MediaControllerListener.onMediaButtonPreferencesChanged → onUpdateNotificationInternal,
        // which correctly determines if the service should run in foreground.
        // Do NOT manually call onUpdateNotification(session, false) here — that bypasses
        // Media3's shouldRunInForeground logic and can remove foreground status, leading to
        // ForegroundServiceStartNotAllowedException when async callbacks fire later.
        session.setMediaButtonPreferences(buttons)
    }

    private fun updateManualShuffleState(
        session: MediaSession,
        enabled: Boolean,
        broadcast: Boolean
    ) {
        val changed = isManualShuffleEnabled != enabled
        isManualShuffleEnabled = enabled
        
        if (persistentShuffleEnabled) {
            serviceScope.launch {
                userPreferencesRepository.setShuffleOn(enabled)
            }
        }

        if (broadcast && changed) {
            val args = Bundle().apply {
                putBoolean(MusicNotificationProvider.EXTRA_SHUFFLE_ENABLED, enabled)
            }
            session.broadcastCustomCommand(
                SessionCommand(MusicNotificationProvider.CUSTOM_COMMAND_SET_SHUFFLE_STATE, Bundle.EMPTY),
                args
            )
        }
        refreshMediaSessionUi(session)
        requestWidgetFullUpdate(force = true)
    }

    private fun buildMediaButtonPreferences(session: MediaSession): List<CommandButton> {
        val player = session.player
        val songId = player.currentMediaItem?.mediaId
        val isFavorite = isSongFavorite(songId)
        val likeButton = CommandButton.Builder(
            if (isFavorite) CommandButton.ICON_HEART_FILLED else CommandButton.ICON_HEART_UNFILLED
        )
            .setDisplayName("Like")
            .setSessionCommand(SessionCommand(MusicNotificationProvider.CUSTOM_COMMAND_LIKE, Bundle.EMPTY))
            .build()

        val shuffleOn = isManualShuffleEnabled
        val shuffleCommandAction = if (shuffleOn) {
            MusicNotificationProvider.CUSTOM_COMMAND_SHUFFLE_OFF
        } else {
            MusicNotificationProvider.CUSTOM_COMMAND_SHUFFLE_ON
        }
        val shuffleButton = CommandButton.Builder(
            if (shuffleOn) CommandButton.ICON_SHUFFLE_ON else CommandButton.ICON_SHUFFLE_OFF
        )
            .setDisplayName("Shuffle")
            .setSessionCommand(SessionCommand(shuffleCommandAction, Bundle.EMPTY))
            .build()

        val repeatButton = CommandButton.Builder(
            when (player.repeatMode) {
                Player.REPEAT_MODE_ONE -> CommandButton.ICON_REPEAT_ONE
                Player.REPEAT_MODE_ALL -> CommandButton.ICON_REPEAT_ALL
                else -> CommandButton.ICON_REPEAT_OFF
            }
        )
            .setDisplayName("Repeat")
            .setSessionCommand(SessionCommand(MusicNotificationProvider.CUSTOM_COMMAND_CYCLE_REPEAT_MODE, Bundle.EMPTY))
            .build()

        return listOf(likeButton, shuffleButton, repeatButton)
    }

    // ------------------------
    // Counted Play Controls
    // ------------------------
    fun startCountedPlay(count: Int) {
        val player = engine.masterPlayer
        val currentItem = player.currentMediaItem ?: return

        stopCountedPlay()  // reset previous

        countedPlayTarget = count
        countedPlayCount = 1
        countedOriginalId = currentItem.mediaId
        countedPlayActive = true

        // Force repeat-one
        player.repeatMode = Player.REPEAT_MODE_ONE

        val listener = object : Player.Listener {

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (!countedPlayActive) return

                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                    countedPlayCount++

                    if (countedPlayCount > countedPlayTarget) {
                        player.pause()
                        stopCountedPlay()
                        return
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (!countedPlayActive) return

                // If user manually changes the song -> cancel
                if (mediaItem?.mediaId != countedOriginalId) {
                    stopCountedPlay()
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                // Prevent user from disabling repeat-one
                if (countedPlayActive && repeatMode != Player.REPEAT_MODE_ONE) {
                    player.repeatMode = Player.REPEAT_MODE_ONE
                }
            }
        }

        countedPlayListener = listener
        player.addListener(listener)
    }

    fun stopCountedPlay() {
        if (!countedPlayActive) return

        countedPlayActive = false
        countedPlayTarget = 0
        countedPlayCount = 0
        countedOriginalId = null

        countedPlayListener?.let {
            engine.masterPlayer.removeListener(it)
        }
        countedPlayListener = null

        // Restore normal repeat mode (OFF)
        engine.masterPlayer.repeatMode = Player.REPEAT_MODE_OFF
    }

    /**
     * Bridges a suspend block into a [ListenableFuture] for Media3 callback methods.
     */
    private fun <T> CoroutineScope.future(block: suspend () -> T): ListenableFuture<T> {
        val future = SettableFuture.create<T>()
        launch(Dispatchers.IO) {
            try {
                future.set(block())
            } catch (e: Exception) {
                future.setException(e)
            }
        }
        return future
    }

}
