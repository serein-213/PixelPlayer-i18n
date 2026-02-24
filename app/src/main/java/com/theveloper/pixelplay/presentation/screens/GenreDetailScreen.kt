package com.theveloper.pixelplay.presentation.screens

import com.theveloper.pixelplay.presentation.navigation.navigateSafely
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.model.Artist
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.presentation.components.ExpressiveTopBarContent
import com.theveloper.pixelplay.presentation.components.ExpressiveScrollBar
import com.theveloper.pixelplay.presentation.components.GenreSortBottomSheet
import com.theveloper.pixelplay.presentation.components.MiniPlayerHeight
import com.theveloper.pixelplay.presentation.components.SmartImage
import com.theveloper.pixelplay.presentation.components.SongInfoBottomSheet
import com.theveloper.pixelplay.presentation.components.subcomps.EnhancedSongListItem
import com.theveloper.pixelplay.presentation.screens.QuickFillDialog
import com.theveloper.pixelplay.presentation.viewmodel.GenreDetailViewModel
import com.theveloper.pixelplay.presentation.viewmodel.GroupedSongListItem
import com.theveloper.pixelplay.presentation.viewmodel.PlayerViewModel
import com.theveloper.pixelplay.presentation.viewmodel.StablePlayerState
import com.theveloper.pixelplay.ui.theme.LocalPixelPlayDarkTheme
import com.theveloper.pixelplay.utils.formatDuration
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape
import kotlin.math.roundToInt

// --- Data Models & Helpers ---

enum class SortOption { ARTIST, ALBUM, TITLE }

sealed class SectionData {
    abstract val id: String

    data class ArtistSection(
        override val id: String,
        val artistName: String,
        val albums: List<AlbumData>
    ) : SectionData()

    data class AlbumSection(
        override val id: String,
        val album: AlbumData
    ) : SectionData()

    data class FlatList(
        val songs: List<Song>
    ) : SectionData() {
        override val id = "flat_list"
    }
}

data class AlbumData(
    val name: String,
    val artUri: String?,
    val songs: List<Song>
)

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GenreDetailScreen(
    navController: NavHostController,
    genreId: String,
    decodedGenreId: String = java.net.URLDecoder.decode(genreId, "UTF-8"),
    playerViewModel: PlayerViewModel,
    viewModel: GenreDetailViewModel = hiltViewModel(),
    playlistViewModel: com.theveloper.pixelplay.presentation.viewmodel.PlaylistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stablePlayerState by playerViewModel.stablePlayerState.collectAsStateWithLifecycle()
    val favoriteSongIds by playerViewModel.favoriteSongIds.collectAsStateWithLifecycle()
    val playlistUiState by playlistViewModel.uiState.collectAsStateWithLifecycle()
    val libraryGenres by playerViewModel.genres.collectAsStateWithLifecycle()
    
    // Get artists to resolve images
    val artists by playerViewModel.artistsFlow.collectAsStateWithLifecycle(initialValue = persistentListOf())

    val darkMode = LocalPixelPlayDarkTheme.current

    // Scroll & Collapsing Top Bar State
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val minTopBarHeight = 58.dp + statusBarHeight // Reduced by 6dp from 64.dp
    val maxTopBarHeight = 200.dp
    val minTopBarHeightPx = with(density) { minTopBarHeight.toPx() }
    val maxTopBarHeightPx = with(density) { maxTopBarHeight.toPx() }

    val topBarHeight = remember { Animatable(maxTopBarHeightPx) }
    var collapseFraction by remember { mutableStateOf(0f) }

    LaunchedEffect(topBarHeight.value) {
        collapseFraction = 1f - ((topBarHeight.value - minTopBarHeightPx) / (maxTopBarHeightPx - minTopBarHeightPx)).coerceIn(0f, 1f)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val isScrollingDown = delta < 0

                // If scrolling up (content going down) and list is not at top, don't expand yet
                if (!isScrollingDown && (lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0)) {
                    return Offset.Zero
                }

                val previousHeight = topBarHeight.value
                val newHeight = (previousHeight + delta).coerceIn(minTopBarHeightPx, maxTopBarHeightPx)
                val consumed = newHeight - previousHeight

                if (consumed.roundToInt() != 0) {
                    coroutineScope.launch { topBarHeight.snapTo(newHeight) }
                }

                // Make sure we consume scroll only if we actually resized the bar
                val canConsumeScroll = !(isScrollingDown && newHeight == minTopBarHeightPx)
                return if (canConsumeScroll) Offset(0f, consumed) else Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val currentHeight = topBarHeight.value
                if (currentHeight > minTopBarHeightPx && currentHeight < maxTopBarHeightPx) {
                    // Decide target based on proximity and velocity
                    val targetHeight = if (available.y > 500f) {
                        maxTopBarHeightPx // Flinging down -> Expand
                    } else if (available.y < -500f) {
                        minTopBarHeightPx // Flinging up -> Collapse
                    } else {
                        // Snap to nearest
                        if (currentHeight > (minTopBarHeightPx + maxTopBarHeightPx) / 2) maxTopBarHeightPx else minTopBarHeightPx
                    }
                    
                    coroutineScope.launch {
                        topBarHeight.animateTo(
                            targetValue = targetHeight,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow) 
                        )
                    }
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    // Colors
    val defaultContainer = MaterialTheme.colorScheme.surfaceVariant
    val defaultOnContainer = MaterialTheme.colorScheme.onSurfaceVariant
    val themeGenre = remember(libraryGenres, decodedGenreId) {
        libraryGenres.firstOrNull { it.id.equals(decodedGenreId, ignoreCase = true) }
    }
    val themeColor = remember(themeGenre, decodedGenreId, darkMode, defaultContainer, defaultOnContainer) {
        if (themeGenre != null) {
            com.theveloper.pixelplay.ui.theme.GenreThemeUtils.getGenreThemeColor(
                genre = themeGenre,
                isDark = darkMode,
                fallbackGenreId = decodedGenreId
            )
        } else {
            com.theveloper.pixelplay.ui.theme.GenreThemeColor(
                defaultContainer,
                defaultOnContainer
            )
        }
    }
    
    val startColor = themeColor.container
    val contentColor = themeColor.onContainer
    val genreDisplayName = themeGenre?.name ?: uiState.genre?.name ?: "Genre"
    val genreShuffleLabel = "$genreDisplayName Shuffle"
    
    // FAB Logic
    var showSortSheet by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(SortOption.ARTIST) }
    var showSongOptionsSheet by remember { mutableStateOf<Song?>(null) }
    var showPlaylistBottomSheet by remember { mutableStateOf(false) }
    var showQuickFillDialog by remember { mutableStateOf(false) }

    val isUnknownGenre = remember(decodedGenreId) {
        decodedGenreId.equals("unknown", ignoreCase = true) || decodedGenreId.equals("unknown genre", ignoreCase = true)
    }
    
    val customGenres by playerViewModel.customGenres.collectAsStateWithLifecycle()
    val customGenreIcons by playerViewModel.customGenreIcons.collectAsStateWithLifecycle()
    val isMiniPlayerVisible = stablePlayerState.currentSong != null
    val fabBottomPadding by animateDpAsState(
        targetValue = if (isMiniPlayerVisible) MiniPlayerHeight + 16.dp else 16.dp, 
        label = "fabPadding"
    )

    val sortedSongs = remember(uiState.songs, sortOption) {
        when (sortOption) {
            SortOption.ARTIST -> uiState.songs.sortedBy { it.artist }
            SortOption.ALBUM -> uiState.songs.sortedBy { it.album }
            SortOption.TITLE -> uiState.songs.sortedBy { it.title }
        }
    }
    
    val displaySections = remember(sortedSongs, sortOption) {
        if (sortOption == SortOption.ARTIST) {
            buildSectionsByArtist(sortedSongs)
        } else if (sortOption == SortOption.ALBUM) {
             buildSectionsByAlbum(sortedSongs)
        } else {
            listOf(SectionData.FlatList(sortedSongs))
        }
    }

    // Dynamic Theme
    val genreColorScheme = remember(themeGenre, decodedGenreId, darkMode) {
        com.theveloper.pixelplay.ui.theme.GenreThemeUtils.getGenreColorScheme(
            genre = themeGenre,
            genreIdFallback = decodedGenreId,
            isDark = darkMode
        )
    }

    // Capture Neutral Colors from the App Theme (before overriding)
    val baseColorScheme = MaterialTheme.colorScheme

    MaterialTheme(colorScheme = genreColorScheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .background(MaterialTheme.colorScheme.background) // Uses new theme background
        ) {
            val currentTopBarHeightDp = with(density) { topBarHeight.value.toDp() }

            // Content
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(
                    top = currentTopBarHeightDp + 8.dp, // Push content down initially
                    start = 8.dp,
                    // Only add end padding if scrollbar is visible (collapsed header)
                    end = if ((lazyListState.canScrollForward || lazyListState.canScrollBackward) && collapseFraction > 0.95f) 20.dp else 8.dp,
                    bottom = fabBottomPadding + 148.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                displaySections.forEach { section ->
                    when (section) {
                        is SectionData.ArtistSection -> {
                            ArtistSection(
                                section = section,
                                artists = artists,
                                onSongClick = { song ->
                                    playerViewModel.showAndPlaySong(song, sortedSongs, genreDisplayName)
                                },
                                stablePlayerState = stablePlayerState,
                                onMoreOptionsClick = { song -> showSongOptionsSheet = song }
                            )
                        }
                        is SectionData.AlbumSection -> {
                            AlbumSection(
                                section = section,
                                onSongClick = { song ->
                                    playerViewModel.showAndPlaySong(song, sortedSongs, genreDisplayName)
                                },
                                stablePlayerState = stablePlayerState,
                                onMoreOptionsClick = { song -> showSongOptionsSheet = song }
                            )
                        }
                        is SectionData.FlatList -> {
                            // Add vertical spacing before flat list if needed, or handle within
                            // For FlatList, we can just use items directly but simpler to keep consistency
                            items(
                                items = section.songs,
                                key = { it.id }
                            ) { song ->
                                Box(modifier = Modifier.animateItem()) {
                                    EnhancedSongListItem(
                                        song = song,
                                        isPlaying = stablePlayerState.isPlaying,
                                        isCurrentSong = stablePlayerState.currentSong?.id == song.id,
                                        onClick = {
                                            playerViewModel.showAndPlaySong(song, sortedSongs, genreDisplayName)
                                        },
                                        onMoreOptionsClick = { showSongOptionsSheet = it }
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                    // Add spacing between sections
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }

            // Only show scrollbar when the top bar is mostly collapsed to avoid visual conflict
            if (collapseFraction > 0.95f) {
                ExpressiveScrollBar(
                    listState = lazyListState,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(
                            top = currentTopBarHeightDp + 12.dp, // Added 12.dp as requested
                            bottom = fabBottomPadding + 112.dp // Increased by 40dp as requested (72 + 40 = 112)
                        )
                )
            }

            // Collapsible Top Bar with Gradient (On Top of List, High Z-Index)
            // This ensures the gradient is ON TOP of the scrolling content, so content scrolls BEHIND it.
            GenreCollapsibleTopBar(
                title = genreDisplayName,
                collapseFraction = collapseFraction,
                headerHeight = currentTopBarHeightDp,
                onBackPressed = { navController.popBackStack() },
                startColor = startColor,
                contentColor = contentColor,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                collapsedContentColor = MaterialTheme.colorScheme.onSurface
            )
        
            // FAB
            Box(
                 modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = fabBottomPadding + 16.dp, end = 16.dp)
                    .zIndex(10f) // Ensure FAB is above everything
            ) {
                 MediumFloatingActionButton(
                    onClick = { showSortSheet = true },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape = AbsoluteSmoothCornerShape(24.dp, 60)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = stringResource(id = R.string.selection_options),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        
            // Sorting/Options Bottom Sheet
            if (showSortSheet) {
                GenreSortBottomSheet(
                    onDismiss = { showSortSheet = false },
                    currentSort = sortOption,
                    onSortSelected = {
                        sortOption = it
                        showSortSheet = false
                    },
                    onShuffle = {
                        if (uiState.songs.isNotEmpty()) {
                            playerViewModel.showAndPlaySong(uiState.songs.random(), uiState.songs, genreShuffleLabel)
                            showSortSheet = false
                        }
                    },
                    headerContent = if (isUnknownGenre) {
                        {
                            Button(
                                onClick = {
                                    showSortSheet = false
                                    showQuickFillDialog = true
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                shape = racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape(16.dp, 60)
                            ) {
                                Icon(Icons.Rounded.AutoFixHigh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.quick_fill_button),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else null
                )
            }

            // Quick Fill Dialog
            // QuickFillDialog with Base Theme (Independent of Genre Theme)
            MaterialTheme(colorScheme = baseColorScheme) {
                QuickFillDialog(
                    visible = showQuickFillDialog,
                    songs = uiState.songs,
                    customGenres = customGenres,
                    customGenreIcons = customGenreIcons,
                    onDismiss = { showQuickFillDialog = false },
                    onApply = { songs, genre ->
                        playerViewModel.batchEditGenre(songs, genre)
                        showQuickFillDialog = false
                    },
                    onAddCustomGenre = { genre, iconRes ->
                        playerViewModel.addCustomGenre(genre, iconRes)
                    }
                )
            }
        
            // Song Options Bottom Sheet
            showSongOptionsSheet?.let { song ->
                val isFavorite = favoriteSongIds.contains(song.id)

                MaterialTheme(
                    colorScheme = genreColorScheme,
                    typography = MaterialTheme.typography,
                    shapes = MaterialTheme.shapes
                ) {
                    SongInfoBottomSheet(
                        song = song,
                        isFavorite = isFavorite,
                        onToggleFavorite = {
                            playerViewModel.toggleFavoriteSpecificSong(song)
                        },
                        onDismiss = { showSongOptionsSheet = null },
                        onPlaySong = {
                            playerViewModel.showAndPlaySong(song, sortedSongs, genreDisplayName)
                            showSongOptionsSheet = null
                        },
                        onAddToQueue = {
                            playerViewModel.addSongToQueue(song)
                            showSongOptionsSheet = null
                            playerViewModel.sendToast("Added to the queue")
                        },
                        onAddNextToQueue = {
                            playerViewModel.addSongNextToQueue(song)
                            showSongOptionsSheet = null
                            playerViewModel.sendToast("Will play next")
                        },
                        onAddToPlayList = {
                            showPlaylistBottomSheet = true
                        },
                        onDeleteFromDevice = playerViewModel::deleteFromDevice,
                        onNavigateToAlbum = {
                            com.theveloper.pixelplay.presentation.navigation.Screen.AlbumDetail.createRoute(song.albumId).let { route ->
                                navController.navigateSafely(route)
                            }
                            showSongOptionsSheet = null
                        },
                        onNavigateToArtist = {
                            com.theveloper.pixelplay.presentation.navigation.Screen.ArtistDetail.createRoute(song.artistId).let { route ->
                                navController.navigateSafely(route)
                            }
                            showSongOptionsSheet = null
                        },
                        onEditSong = { newTitle, newArtist, newAlbum, newGenre, newLyrics, newTrackNumber, coverArtUpdate ->
                            playerViewModel.editSongMetadata(song, newTitle, newArtist, newAlbum, newGenre, newLyrics, newTrackNumber, coverArtUpdate)
                        },
                        generateAiMetadata = { fields ->
                            playerViewModel.generateAiMetadata(song, fields)
                        },
                        removeFromListTrigger = {}
                    )
                }

                if (showPlaylistBottomSheet) {
                    com.theveloper.pixelplay.presentation.components.PlaylistBottomSheet(
                        playlistUiState = playlistUiState,
                        songs = listOf(song),
                        onDismiss = { showPlaylistBottomSheet = false },
                        bottomBarHeight = 0.dp, // Or calculate if needed
                        playerViewModel = playerViewModel
                    )
                }
            }
        
            // Loading/Error States
            if (uiState.isLoadingSongs) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

// --- Top Bar Component ---
@Composable
fun GenreCollapsibleTopBar(
    title: String,
    collapseFraction: Float,
    headerHeight: Dp,
    onBackPressed: () -> Unit,
    startColor: Color,
    containerColor: Color,
    contentColor: Color,
    collapsedContentColor: Color
) {
    LocalDensity.current
    val solidAlpha = (collapseFraction * 2f).coerceIn(0f, 1f)
    val animatedContentColor = androidx.compose.ui.graphics.lerp(
        start = contentColor,
        stop = collapsedContentColor,
        fraction = solidAlpha
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .zIndex(5f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(containerColor.copy(alpha = solidAlpha)) 
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            startColor.copy(alpha = 0.8f * (1f - solidAlpha)),
                            startColor.copy(alpha = 0f)
                        )
                    )
                )
        )

        Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
             FilledIconButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 4.dp)
                    .zIndex(10f),
                onClick = onBackPressed,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = animatedContentColor.copy(alpha = 0.1f),
                    contentColor = animatedContentColor
                )
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = animatedContentColor)
            }

            ExpressiveTopBarContent(
                title = title,
                collapseFraction = collapseFraction,
                modifier = Modifier.fillMaxSize(),
                collapsedTitleStartPadding = 68.dp,
                expandedTitleStartPadding = 20.dp,
                maxLines = 1,
                contentColor = animatedContentColor
            )
        }
    }
}


private fun buildSectionsByArtist(songs: List<Song>): List<SectionData> {
    val grouped = songs.groupBy { it.artist ?: "Unknown Artist" }
    return grouped.map { (artist, artistSongs) ->
        val albums = artistSongs.groupBy { it.album ?: "Unknown Album" }.map { (albumName, albumSongs) ->
             AlbumData(albumName, albumSongs.firstOrNull()?.albumArtUriString, albumSongs)
        }
        SectionData.ArtistSection("artist_$artist", artist, albums)
    }
}

private fun buildSectionsByAlbum(songs: List<Song>): List<SectionData> {
     val grouped = songs.groupBy { it.album ?: "Unknown Album" }
     return grouped.map { (album, albumSongs) ->
         SectionData.AlbumSection(
             "album_$album",
             AlbumData(album, albumSongs.firstOrNull()?.albumArtUriString, albumSongs)
         )
     }
}

// --- Section Extensions ---

fun LazyListScope.ArtistSection(
    section: SectionData.ArtistSection,
    artists: List<Artist>,
    onSongClick: (Song) -> Unit,
    stablePlayerState: StablePlayerState,
    onMoreOptionsClick: (Song) -> Unit
) {
    val artistImage = artists.find { it.name.equals(section.artistName, ignoreCase = true) }?.imageUrl

    // 1. Artist Header
    item(key = "header_${section.id}") {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
            shape = AbsoluteSmoothCornerShape(
                cornerRadiusTR = 24.dp, smoothnessAsPercentTR = 60,
                cornerRadiusTL = 24.dp, smoothnessAsPercentTL = 60,
                cornerRadiusBR = 0.dp, smoothnessAsPercentBR = 0,
                cornerRadiusBL = 0.dp, smoothnessAsPercentBL = 0
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Content
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!artistImage.isNullOrEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(artistImage)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = section.artistName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Generic Artist",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .fillMaxSize()
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = section.artistName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    // 2. Albums Loop
    section.albums.forEachIndexed { albumIndex, album ->
        if (albumIndex > 0) {
            item(key = "divider_${section.id}_$albumIndex") {
                 Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.alpha(0.3f))
                }
            }
        }

        AlbumSectionItems(
            album = album,
            keyPrefix = section.id, // Pass section ID (includes artist name)
            onSongClick = onSongClick,
            stablePlayerState = stablePlayerState,
            onMoreOptionsClick = onMoreOptionsClick,
            isLastAlbumInSection = albumIndex == section.albums.lastIndex,
            useArtistStyle = true
        )
    }
}

fun LazyListScope.AlbumSection(
    section: SectionData.AlbumSection,
    onSongClick: (Song) -> Unit,
    stablePlayerState: StablePlayerState,
    onMoreOptionsClick: (Song) -> Unit
) {
    AlbumSectionItems(
        album = section.album,
        keyPrefix = section.id, // Pass section ID (unique for album view)
        onSongClick = onSongClick,
        stablePlayerState = stablePlayerState,
        onMoreOptionsClick = onMoreOptionsClick,
        isLastAlbumInSection = true,
        useArtistStyle = false
    )
}

fun LazyListScope.AlbumSectionItems(
    album: AlbumData,
    keyPrefix: String, // Added prefix for uniqueness
    onSongClick: (Song) -> Unit,
    stablePlayerState: StablePlayerState,
    onMoreOptionsClick: (Song) -> Unit,
    isLastAlbumInSection: Boolean,
    useArtistStyle: Boolean
) {
    item(key = "${keyPrefix}_album_header_${album.name}") {
        val containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
        val shape = if (useArtistStyle) {
            RectangleShape
        } else {
             AbsoluteSmoothCornerShape(
                cornerRadiusTR = 24.dp, smoothnessAsPercentTR = 60,
                cornerRadiusTL = 24.dp, smoothnessAsPercentTL = 60,
                cornerRadiusBR = 0.dp, smoothnessAsPercentBR = 0,
                cornerRadiusBL = 0.dp, smoothnessAsPercentBL = 0
            )
        }
        
        Box(
             modifier = Modifier
                .fillMaxWidth()
                .background(containerColor, shape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmartImage(
                    model = album.artUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = album.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${album.songs.size} songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        if(album.songs.isNotEmpty()) onSongClick(album.songs.first())
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = "Play Album")
                }
            }
        }
    }
    
    // Spacing Item
    item(key = "${keyPrefix}_album_spacer_${album.name}") {
         Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f))
        )
    }

    // Songs
    val songCount = album.songs.size
    itemsIndexed(
        items = album.songs,
        key = { index, song -> song.id }
    ) { index, song ->
        val isLastSong = index == songCount - 1
        
        // Shape for the SONG ITEM itself (visual ripple/highlight shape)
        val songItemShape = when {
            songCount == 1 -> RoundedCornerShape(16.dp)
            index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
            isLastSong -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
            else -> RoundedCornerShape(4.dp)
        }
        
       // Container Shape (The "Card" background)
       // If useArtistStyle:
       //   If isLastAlbumInSection && isLastSong -> BottomRounded.
       //   Else -> Rect.
       // If Standalone:
       //   If isLastSong -> BottomRounded.
       //   Else -> Rect.
       
       val containerShape = if (isLastSong && isLastAlbumInSection) {
            AbsoluteSmoothCornerShape(
                cornerRadiusTR = 0.dp, smoothnessAsPercentTR = 0,
                cornerRadiusTL = 0.dp, smoothnessAsPercentTL = 0,
                cornerRadiusBR = 24.dp, smoothnessAsPercentBR = 60,
                cornerRadiusBL = 24.dp, smoothnessAsPercentBL = 60
            ) 
       } else {
           RectangleShape
       }
       
       Box(
           modifier = Modifier
               .fillMaxWidth()
               .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f), containerShape)
               // Padding internal to the card logic
               .padding(horizontal = 8.dp) 
               .padding(bottom = if (isLastSong && !isLastAlbumInSection && useArtistStyle) 8.dp else 0.dp) // Add spacing if there are more albums? No, Divider handles it.
       ) {
            // Extra spacing logic handled by `Arrangement.spacedBy(2.dp)` in original column.
            // Here we are flat items. Content padding?
            // Original: Column(verticalArrangement = Arrangement.spacedBy(2.dp))
            // So we need 2.dp padding between songs?
            // We can add padding to top of items except first one?
            
            Column {
                if (index > 0) Spacer(Modifier.height(2.dp))
                
                EnhancedSongListItem(
                     song = song,
                     isPlaying = stablePlayerState.isPlaying,
                     isCurrentSong = stablePlayerState.currentSong?.id == song.id,
                     showAlbumArt = false,
                     customShape = songItemShape,
                     onClick = { onSongClick(song) },
                     onMoreOptionsClick = onMoreOptionsClick
                 )
                 
                 // Bottom spacing for the very last item in the album to push it off the edge of the card?
                 if (isLastSong) Spacer(Modifier.height(8.dp))
            }
       }
    }
}
