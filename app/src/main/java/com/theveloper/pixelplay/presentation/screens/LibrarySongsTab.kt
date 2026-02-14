package com.theveloper.pixelplay.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.model.SortOption
import com.theveloper.pixelplay.presentation.components.MiniPlayerHeight
import com.theveloper.pixelplay.presentation.viewmodel.PlayerViewModel
import com.theveloper.pixelplay.presentation.viewmodel.StablePlayerState
import com.theveloper.pixelplay.presentation.components.subcomps.EnhancedSongListItem
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.paging.compose.LazyPagingItems
import androidx.paging.LoadState


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibrarySongsTab(
    songs: LazyPagingItems<Song>, // Changed from ImmutableList<Song>
    isLoading: Boolean, // Kept for initial load or other states, though Paging has its own
    stablePlayerState: StablePlayerState,
    playerViewModel: PlayerViewModel,
    bottomBarHeight: Dp,
    onMoreOptionsClick: (Song) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    sortOption: SortOption, // Added sortOption parameter
    // Multi-selection parameters
    isSelectionMode: Boolean = false,
    selectedSongIds: Set<String> = emptySet(),
    onSongLongPress: (Song) -> Unit = {},
    onSongSelectionToggle: (Song) -> Unit = {},
    onLocateCurrentSongVisibilityChanged: (Boolean) -> Unit = {},
    onRegisterLocateCurrentSongAction: ((() -> Unit)?) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val visibilityCallback by rememberUpdatedState(onLocateCurrentSongVisibilityChanged)
    val registerActionCallback by rememberUpdatedState(onRegisterLocateCurrentSongAction)
    
    val currentSongId = stablePlayerState.currentSong?.id

    // Check if list is effectively empty (based on Paging state)
    // val isListEmpty = songs.itemCount == 0 && songs.loadState.refresh is LoadState.NotLoading
    
    // Calculate current song index for auto-scroll
    val currentSongListIndex = remember(songs.itemSnapshotList.items.size, currentSongId) {
        if (currentSongId == null) -1
        else {
            val snapshot = songs.itemSnapshotList.items
             snapshot.indexOfFirst { it.id == currentSongId }
        }
    }

    // Auto-scroll logic is adapted below with locateCurrentSongAction

    val locateCurrentSongAction: (() -> Unit)? = remember(currentSongListIndex, listState) {
        if (currentSongListIndex < 0) {
            null
        } else {
            {
                coroutineScope.launch {
                    listState.animateScrollToItem(currentSongListIndex)
                }
            }
        }
    }

    LaunchedEffect(locateCurrentSongAction) {
        registerActionCallback(locateCurrentSongAction)
    }

    // Scroll to top when sort option changes
    LaunchedEffect(sortOption) {
        listState.scrollToItem(0)
    }

    LaunchedEffect(currentSongListIndex, songs, isLoading, listState) {
        if (currentSongListIndex < 0 || songs.itemCount == 0 || isLoading) {
            visibilityCallback(false)
            return@LaunchedEffect
        }

        snapshotFlow {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                false
            } else {
                currentSongListIndex in visibleItems.first().index..visibleItems.last().index
            }
        }
            .distinctUntilChanged()
            .collect { isVisible ->
                visibilityCallback(!isVisible)
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            visibilityCallback(false)
            registerActionCallback(null)
        }
    }

    // Handle different loading states
    val refreshState = songs.loadState.refresh
    val reachedEndOfPagination = songs.loadState.append.endOfPaginationReached
    val shouldShowInitialLoading = songs.itemCount == 0 && (
        isLoading ||
            refreshState is LoadState.Loading ||
            (refreshState is LoadState.NotLoading && !reachedEndOfPagination)
    )

    when {
        refreshState is LoadState.Error && songs.itemCount == 0 -> {
            val error = (refreshState as LoadState.Error).error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error loading songs", style = MaterialTheme.typography.titleMedium)
                    Text(
                        error.localizedMessage ?: "Unknown error",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { songs.retry() }) {
                        Text("Retry")
                    }
                }
            }
        }
        shouldShowInitialLoading -> {
            // Initial loading - show skeleton placeholders
            LazyColumn(
                modifier = Modifier
                    .padding(start = 12.dp, end = 24.dp, bottom = 6.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 26.dp,
                            topEnd = 26.dp,
                            bottomStart = PlayerSheetCollapsedCornerRadius,
                            bottomEnd = PlayerSheetCollapsedCornerRadius
                        )
                    )
                    .fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = bottomBarHeight + MiniPlayerHeight + ListExtraBottomGap)
            ) {
                items(12, key = { "skeleton_song_$it" }) { // Show 12 skeleton items
                    EnhancedSongListItem(
                        song = Song.emptySong(),
                        isPlaying = false,
                        isLoading = true,
                        isCurrentSong = false,
                        onMoreOptionsClick = {},
                        onClick = {}
                    )
                }
            }
        }
        songs.itemCount == 0 && refreshState is LoadState.NotLoading && reachedEndOfPagination -> {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_music_off_24),
                        contentDescription = stringResource(R.string.library_empty_no_songs),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.library_empty_no_songs), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.library_empty_no_songs_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        else -> {
            // Songs loaded
            Box(modifier = Modifier.fillMaxSize()) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize(),
                    indicator = {
                        PullToRefreshDefaults.LoadingIndicator(
                            state = pullToRefreshState,
                            isRefreshing = isRefreshing,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .padding(start = 12.dp, end = if (listState.canScrollForward || listState.canScrollBackward) 22.dp else 12.dp, bottom = 6.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 26.dp,
                                        topEnd = 26.dp,
                                        bottomStart = PlayerSheetCollapsedCornerRadius,
                                        bottomEnd = PlayerSheetCollapsedCornerRadius
                                    )
                                ),
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = bottomBarHeight + MiniPlayerHeight + 30.dp)
                        ) {
                            //item(key = "songs_top_spacer") { Spacer(Modifier.height(0.dp)) }

                            items(
                                count = songs.itemCount,
                                key = { index -> songs.peek(index)?.id ?: index },
                                contentType = { "song" }
                            ) { index ->
                                val song = songs[index]
                                
                                if (song != null) {
                                    val isPlayingThisSong = song.id == stablePlayerState.currentSong?.id && stablePlayerState.isPlaying
                                    val isSelected = selectedSongIds.contains(song.id)
                                    
                                    val rememberedOnMoreOptionsClick: (Song) -> Unit = remember(onMoreOptionsClick) {
                                        { songFromListItem -> onMoreOptionsClick(songFromListItem) }
                                    }
                                    
                                    // In selection mode, click toggles selection instead of playing
                                    val rememberedOnClick: () -> Unit = remember(song, isSelectionMode) {
                                        if (isSelectionMode) {
                                            { onSongSelectionToggle(song) }
                                        } else {
                                            // Use snapshot items for playback queue
                                            { playerViewModel.showAndPlaySong(song, songs.itemSnapshotList.items, "Library") }
                                        }
                                    }
                                    
                                    val rememberedOnLongPress: () -> Unit = remember(song) {
                                        { onSongLongPress(song) }
                                    }

                                    EnhancedSongListItem(
                                        song = song,
                                        isPlaying = isPlayingThisSong,
                                        isCurrentSong = stablePlayerState.currentSong?.id == song.id,
                                        isLoading = false,
                                        isSelected = isSelected,
                                        isSelectionMode = isSelectionMode,
                                        onLongPress = rememberedOnLongPress,
                                        onMoreOptionsClick = rememberedOnMoreOptionsClick,
                                        onClick = rememberedOnClick
                                    )
                                } else {
                                     // Placeholder
                                     EnhancedSongListItem(
                                        song = Song.emptySong(),
                                        isPlaying = false,
                                        isLoading = true,
                                        isCurrentSong = false,
                                        onMoreOptionsClick = {},
                                        onClick = {}
                                     )
                                }
                            }
                        }
                        
                        // ScrollBar Overlay
                        val bottomPadding = if (stablePlayerState.currentSong != null && stablePlayerState.currentSong != Song.emptySong()) 
                            bottomBarHeight + MiniPlayerHeight + 16.dp 
                        else 
                            bottomBarHeight + 16.dp

                        com.theveloper.pixelplay.presentation.components.ExpressiveScrollBar(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp, top = 16.dp, bottom = bottomPadding),
                            listState = listState
                        )
                    }
                }
            }
        }
    }
}
