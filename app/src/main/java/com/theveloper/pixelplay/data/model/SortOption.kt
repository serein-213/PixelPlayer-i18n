package com.theveloper.pixelplay.data.model

import androidx.compose.runtime.Immutable
import com.theveloper.pixelplay.R

// Sealed class for Sort Options
@Immutable
sealed class SortOption(val storageKey: String, val displayName: String, val displayNameStringResId: Int) {
    // Song Sort Options
    object SongDefaultOrder : SortOption("song_default_order", "Default Order", R.string.sort_option_song_default_order)
    object SongTitleAZ : SortOption("song_title_az", "Title (A-Z)", R.string.sort_option_song_title_az)
    object SongTitleZA : SortOption("song_title_za", "Title (Z-A)", R.string.sort_option_song_title_za)
    object SongArtist : SortOption("song_artist", "Artist", R.string.sort_option_song_artist)
    object SongAlbum : SortOption("song_album", "Album", R.string.sort_option_song_album)
    object SongDateAdded : SortOption("song_date_added", "Date Added", R.string.sort_option_song_date_added)
    object SongDuration : SortOption("song_duration", "Duration", R.string.sort_option_song_duration)

    // Album Sort Options
    object AlbumTitleAZ : SortOption("album_title_az", "Title (A-Z)", R.string.sort_option_album_title_az)
    object AlbumTitleZA : SortOption("album_title_za", "Title (Z-A)", R.string.sort_option_album_title_za)
    object AlbumArtist : SortOption("album_artist", "Artist", R.string.sort_option_album_artist)
    object AlbumReleaseYear : SortOption("album_release_year", "Release Year", R.string.sort_option_album_release_year)
    object AlbumSizeAsc : SortOption("album_size_asc", "Fewest Songs", R.string.sort_option_album_size_asc)
    object AlbumSizeDesc : SortOption("album_size_desc", "Most Songs", R.string.sort_option_album_size_desc)

    // Artist Sort Options
    object ArtistNameAZ : SortOption("artist_name_az", "Name (A-Z)", R.string.sort_option_artist_name_az)
    object ArtistNameZA : SortOption("artist_name_za", "Name (Z-A)", R.string.sort_option_artist_name_za)
    // object ArtistNumSongs : SortOption("artist_num_songs", "Number of Songs") // Requires ViewModel change & data

    // Playlist Sort Options
    object PlaylistNameAZ : SortOption("playlist_name_az", "Name (A-Z)", R.string.sort_option_playlist_name_az)
    object PlaylistNameZA : SortOption("playlist_name_za", "Name (Z-A)", R.string.sort_option_playlist_name_za)
    object PlaylistDateCreated : SortOption("playlist_date_created", "Date Created", R.string.sort_option_playlist_date_created)
    // object PlaylistNumSongs : SortOption("playlist_num_songs", "Number of Songs") // Requires ViewModel change & data

    // Liked Sort Options (similar to Songs)
    object LikedSongTitleAZ : SortOption("liked_title_az", "Title (A-Z)", R.string.sort_option_liked_title_az)
    object LikedSongTitleZA : SortOption("liked_title_za", "Title (Z-A)", R.string.sort_option_liked_title_za)
    object LikedSongArtist : SortOption("liked_artist", "Artist", R.string.sort_option_liked_artist)
    object LikedSongAlbum : SortOption("liked_album", "Album", R.string.sort_option_liked_album)
    object LikedSongDateLiked : SortOption("liked_date_liked", "Date Liked", R.string.sort_option_liked_date_liked)

    // Folder Sort Options
    object FolderNameAZ : SortOption("folder_name_az", "Name (A-Z)", R.string.sort_option_folder_name_az)
    object FolderNameZA : SortOption("folder_name_za", "Name (Z-A)", R.string.sort_option_folder_name_za)
    object FolderSongCountAsc : SortOption("folder_song_count_asc", "Fewest Songs", R.string.sort_option_folder_song_count_asc)
    object FolderSongCountDesc : SortOption("folder_song_count_desc", "Most Songs", R.string.sort_option_folder_song_count_desc)
    object FolderSubdirCountAsc : SortOption("folder_subdir_count_asc", "Fewest Subfolders", R.string.sort_option_folder_subdir_count_asc)
    object FolderSubdirCountDesc : SortOption("folder_subdir_count_desc", "Most Subfolders", R.string.sort_option_folder_subdir_count_desc)

    companion object {

        val SONGS: List<SortOption> by lazy {
             listOf(
                SongDefaultOrder,
                SongTitleAZ,
                SongTitleZA,
                SongArtist,
                SongAlbum,
                SongDateAdded,
                SongDuration
            )
        }
        val ALBUMS: List<SortOption> by lazy {
            listOf(
                AlbumTitleAZ,
                AlbumTitleZA,
                AlbumArtist,
                AlbumReleaseYear,
                AlbumSizeAsc,
                AlbumSizeDesc
            )
        }
        val ARTISTS: List<SortOption> by lazy {
            listOf(
                ArtistNameAZ,
                ArtistNameZA
            )
        }
        val PLAYLISTS: List<SortOption> by lazy {
            listOf(
                PlaylistNameAZ,
                PlaylistNameZA,
                PlaylistDateCreated
            )
        }
        val FOLDERS: List<SortOption> by lazy {
            listOf(
                FolderNameAZ,
                FolderNameZA,
                FolderSongCountAsc,
                FolderSongCountDesc,
                FolderSubdirCountAsc,
                FolderSubdirCountDesc
            )
        }
        val LIKED: List<SortOption> by lazy {
            listOf(
                LikedSongTitleAZ,
                LikedSongTitleZA,
                LikedSongArtist,
                LikedSongAlbum,
                LikedSongDateLiked
            )
        }

        fun fromStorageKey(
            rawValue: String?,
            allowed: Collection<SortOption>,
            fallback: SortOption
        ): SortOption {
            if (rawValue.isNullOrBlank()) {
                return fallback
            }

            val sanitized = allowed.filterIsInstance<SortOption>()
            if (sanitized.isEmpty()) {
                return fallback
            }

            sanitized.firstOrNull { option -> option.storageKey == rawValue }?.let { matched ->
                return matched
            }

            // Legacy values used display names; fall back to matching within the allowed group.
            return sanitized.firstOrNull { option -> option.displayName == rawValue } ?: fallback
        }
    }
}
