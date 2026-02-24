package com.theveloper.pixelplay.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.model.LibraryTabId
import com.theveloper.pixelplay.data.model.StorageFilter
import com.theveloper.pixelplay.presentation.components.MiniPlayerHeight
import com.theveloper.pixelplay.ui.theme.GoogleSansRounded

private data class LibraryEmptySpec(
    val iconRes: Int,
    val titleRes: Int,
    val subtitleRes: Int
)

private fun libraryEmptySpec(
    tabId: LibraryTabId,
    storageFilter: StorageFilter
): LibraryEmptySpec {
    return when (tabId) {
        LibraryTabId.SONGS -> when (storageFilter) {
            StorageFilter.ALL -> LibraryEmptySpec(
                iconRes = R.drawable.rounded_music_off_24,
                titleRes = R.string.library_empty_no_songs_yet,
                subtitleRes = R.string.library_empty_add_music_subtitle
            )
            StorageFilter.OFFLINE -> LibraryEmptySpec(
                iconRes = R.drawable.rounded_music_off_24,
                titleRes = R.string.library_empty_no_local_songs,
                subtitleRes = R.string.library_empty_try_filter_or_rescan
            )
            StorageFilter.ONLINE -> LibraryEmptySpec(
                iconRes = R.drawable.rounded_music_off_24,
                titleRes = R.string.library_empty_no_cloud_songs,
                subtitleRes = R.string.library_empty_sync_cloud_sources
            )
        }

        LibraryTabId.ALBUMS -> when (storageFilter) {
            StorageFilter.ALL -> LibraryEmptySpec(
                iconRes = R.drawable.rounded_album_24,
                titleRes = R.string.library_empty_no_albums,
                subtitleRes = R.string.library_empty_albums_appear_when_grouped
            )
            StorageFilter.OFFLINE -> LibraryEmptySpec(
                iconRes = R.drawable.rounded_album_24,
                titleRes = R.string.library_empty_no_local_albums,
                subtitleRes = R.string.library_empty_local_songs_required
            )
            StorageFilter.ONLINE -> LibraryEmptySpec(
                iconRes = R.drawable.rounded_album_24,
                titleRes = R.string.library_empty_no_cloud_albums,
                subtitleRes = R.string.library_empty_cloud_albums_appear_after_sync
            )
        }

        LibraryTabId.ARTISTS -> when (storageFilter) {
            StorageFilter.ALL -> LibraryEmptySpec(
                iconRes = R.drawable.rounded_artist_24,
                titleRes = R.string.library_empty_no_artists,
                subtitleRes = R.string.library_empty_artists_after_indexing
            )
            StorageFilter.OFFLINE -> LibraryEmptySpec(
                iconRes = R.drawable.rounded_artist_24,
                titleRes = R.string.library_empty_no_local_artists,
                subtitleRes = R.string.library_empty_no_artist_metadata
            )
            StorageFilter.ONLINE -> LibraryEmptySpec(
                iconRes = R.drawable.rounded_artist_24,
                titleRes = R.string.library_empty_no_cloud_artists,
                subtitleRes = R.string.library_empty_cloud_artist_after_sync
            )
        }

        LibraryTabId.LIKED -> when (storageFilter) {
            StorageFilter.ALL -> LibraryEmptySpec(
                iconRes = R.drawable.rounded_favorite_24,
                titleRes = R.string.library_empty_no_liked_songs,
                subtitleRes = R.string.library_empty_tap_heart_to_like
            )
            StorageFilter.OFFLINE -> LibraryEmptySpec(
                iconRes = R.drawable.rounded_favorite_24,
                titleRes = R.string.library_empty_no_liked_local_songs,
                subtitleRes = R.string.library_empty_switch_source_or_like
            )
            StorageFilter.ONLINE -> LibraryEmptySpec(
                iconRes = R.drawable.rounded_favorite_24,
                titleRes = R.string.library_empty_no_liked_cloud_songs,
                subtitleRes = R.string.library_empty_like_cloud_tracks
            )
        }

        LibraryTabId.FOLDERS -> LibraryEmptySpec(
            iconRes = R.drawable.ic_folder,
            titleRes = R.string.library_empty_no_folders,
            subtitleRes = R.string.library_empty_internal_storage_folders
        )

        LibraryTabId.PLAYLISTS -> LibraryEmptySpec(
            iconRes = R.drawable.rounded_playlist_play_24,
            titleRes = R.string.library_empty_no_playlists,
            subtitleRes = R.string.library_empty_create_first_playlist
        )
    }
}

@Composable
internal fun LibraryExpressiveEmptyState(
    tabId: LibraryTabId,
    storageFilter: StorageFilter,
    bottomBarHeight: Dp,
    modifier: Modifier = Modifier
) {
    val spec = remember(tabId, storageFilter) { libraryEmptySpec(tabId, storageFilter) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = 28.dp,
                end = 28.dp,
                bottom = bottomBarHeight + MiniPlayerHeight + 24.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                tonalElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = spec.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(spec.titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(spec.subtitleRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
