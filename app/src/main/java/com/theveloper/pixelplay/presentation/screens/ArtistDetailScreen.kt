@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.theveloper.pixelplay.presentation.screens

import com.theveloper.pixelplay.presentation.navigation.navigateSafely
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.theveloper.pixelplay.ui.theme.LocalPixelPlayDarkTheme
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.theveloper.pixelplay.data.model.Artist
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.presentation.components.ExpressiveScrollBar
import com.theveloper.pixelplay.presentation.components.MiniPlayerHeight
import com.theveloper.pixelplay.presentation.components.NavBarContentHeight
import com.theveloper.pixelplay.presentation.components.PlaylistBottomSheet
import com.theveloper.pixelplay.presentation.components.SmartImage
import com.theveloper.pixelplay.presentation.components.SongInfoBottomSheet
import com.theveloper.pixelplay.presentation.navigation.Screen
import com.theveloper.pixelplay.presentation.viewmodel.ArtistDetailViewModel
import com.theveloper.pixelplay.presentation.viewmodel.ArtistAlbumSection
import com.theveloper.pixelplay.presentation.viewmodel.PlayerViewModel
import com.theveloper.pixelplay.presentation.viewmodel.PlaylistViewModel
import com.theveloper.pixelplay.utils.shapes.RoundedStarShape
import kotlinx.coroutines.launch
import com.theveloper.pixelplay.presentation.components.subcomps.EnhancedSongListItem
import kotlin.math.roundToInt
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

private const val HeaderVisualOverscan = 1.03f
private val HeaderGradientLift = 10.dp

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistDetailScreen(
    artistId: String,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    viewModel: ArtistDetailViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stablePlayerState by playerViewModel.stablePlayerState.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val favoriteIds by playerViewModel.favoriteSongIds.collectAsStateWithLifecycle()
    var showSongInfoBottomSheet by remember { mutableStateOf(false) }
    val selectedSongForInfo by playerViewModel.selectedSongForInfo.collectAsStateWithLifecycle()
    val systemNavBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomBarHeightDp = NavBarContentHeight + systemNavBarInset
    var showPlaylistBottomSheet by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val isDarkTheme = LocalPixelPlayDarkTheme.current
    val baseColorScheme = MaterialTheme.colorScheme

    // --- Dynamic color palette from pre-warmed ViewModel state ---
    // artistColorScheme is set by the ViewModel BEFORE isLoading becomes false,
    // so the very first composition already has the correct palette — no flash.
    val artistColorSchemePair by viewModel.artistColorScheme.collectAsStateWithLifecycle()
    val artistColorScheme = remember(artistColorSchemePair, isDarkTheme) {
        artistColorSchemePair?.let { pair -> if (isDarkTheme) pair.dark else pair.light }
            ?: baseColorScheme
    }

    // --- Image picker for custom artist image ---
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.setCustomImage(it) }
    }

    LaunchedEffect(Unit) {
        playerViewModel.collapsePlayerSheet()
    }

    // --- Lógica del Header Colapsable ---
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val minTopBarHeight = 64.dp + statusBarHeight
    val maxTopBarHeight = 300.dp

    val minTopBarHeightPx = with(density) { minTopBarHeight.toPx() }
    val maxTopBarHeightPx = with(density) { maxTopBarHeight.toPx() }

    val topBarHeight = remember { Animatable(maxTopBarHeightPx) }
    val collapseFraction by remember {
        derivedStateOf {
            1f - ((topBarHeight.value - minTopBarHeightPx) / (maxTopBarHeightPx - minTopBarHeightPx)).coerceIn(0f, 1f)
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val isScrollingDown = delta < 0

                // Si estamos scrolleando hacia arriba y no estamos en el tope de la lista,
                // el scroll es para la lista, no para la TopBar.
                if (!isScrollingDown && (lazyListState.firstVisibleItemIndex > 0 || lazyListState.firstVisibleItemScrollOffset > 0)) {
                    return Offset.Zero
                }

                val previousHeight = topBarHeight.value
                val newHeight = (previousHeight + delta).coerceIn(minTopBarHeightPx, maxTopBarHeightPx)
                val consumed = newHeight - previousHeight

                if (consumed.roundToInt() != 0) {
                    coroutineScope.launch {
                        topBarHeight.snapTo(newHeight)
                    }
                }

                // Si estamos en el tope y scrolleamos hacia arriba, la lista no debe moverse.
                val canConsumeScroll = !(isScrollingDown && newHeight == minTopBarHeightPx)
                return if (canConsumeScroll) Offset(0f, consumed) else Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                return super.onPostFling(consumed, available)
            }
        }
    }

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            val shouldExpand = topBarHeight.value > (minTopBarHeightPx + maxTopBarHeightPx) / 2
            val canExpand = lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0

            val targetValue = if (shouldExpand && canExpand) {
                maxTopBarHeightPx
            } else {
                minTopBarHeightPx
            }

            if (topBarHeight.value != targetValue) {
                coroutineScope.launch {
                    topBarHeight.animateTo(targetValue, spring(stiffness = Spring.StiffnessMedium))
                }
            }
        }
    }
    // --- Fin de la lógica del Header ---

    // Wrap in dynamic theme derived from the artist's image
    MaterialTheme(
        colorScheme = artistColorScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes
    ) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.nestedScroll(nestedScrollConnection)) {
            when {
                uiState.isLoading && uiState.artist == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ContainedLoadingIndicator()
                    }
                }
                uiState.error != null && uiState.artist == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                uiState.artist != null -> {
                    val artist = uiState.artist!!
                    val songs = uiState.songs
                    val currentTopBarHeightDp = with(density) { topBarHeight.value.toDp() }

                    val albumSections = uiState.albumSections
                    val expandedSections = remember { mutableStateMapOf<String, Boolean>() }
                    LaunchedEffect(albumSections) {
                        val currentKeys = albumSections.map { it.collapseKey() }.toSet()
                        currentKeys.forEach { key ->
                            if (expandedSections[key] == null) {
                                expandedSections[key] = true
                            }
                        }
                        expandedSections.keys
                            .filterNot { it in currentKeys }
                            .forEach { staleKey -> expandedSections.remove(staleKey) }
                    }

                    val showScrollBar by remember {
                        derivedStateOf {
                            collapseFraction > 0.95f &&
                                (lazyListState.canScrollForward || lazyListState.canScrollBackward)
                        }
                    }

                    LazyColumn(
                        state = lazyListState,
                        contentPadding = PaddingValues(
                            top = currentTopBarHeightDp,
                            start = 16.dp,
                            end = if (showScrollBar) 24.dp else 16.dp,
                            bottom = MiniPlayerHeight + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 8.dp
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        albumSections.forEachIndexed { index, section ->
                            if (section.songs.isEmpty()) return@forEachIndexed

                            val sectionKey = section.collapseKey()
                            val isExpanded = expandedSections[sectionKey] ?: true

                            item(key = "${sectionKey}_header") {
                                CollapsibleAlbumSectionHeader(
                                    section = section,
                                    isExpanded = isExpanded,
                                    onToggleExpanded = {
                                        expandedSections[sectionKey] = !isExpanded
                                    },
                                    onPlayAlbum = {
                                        section.songs.firstOrNull()?.let { firstSong ->
                                            playerViewModel.showAndPlaySong(firstSong, section.songs)
                                        }
                                    }
                                )
                            }

                            item(key = "${sectionKey}_song_group_spacer") {
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically(animationSpec = tween(durationMillis = 260)) + fadeIn(animationSpec = tween(durationMillis = 180)),
                                    exit = shrinkVertically(animationSpec = tween(durationMillis = 220)) + fadeOut(animationSpec = tween(durationMillis = 140))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f))
                                    )
                                }
                            }

                            itemsIndexed(
                                items = section.songs,
                                key = { songIndex, song -> "${sectionKey}_song_${song.id}_$songIndex" }
                            ) { songIndex, song ->
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically(animationSpec = tween(durationMillis = 280)) + fadeIn(animationSpec = tween(durationMillis = 200)),
                                    exit = shrinkVertically(animationSpec = tween(durationMillis = 240)) + fadeOut(animationSpec = tween(durationMillis = 150))
                                ) {
                                    ArtistAlbumSectionSongItem(
                                        song = song,
                                        songIndex = songIndex,
                                        songCount = section.songs.size,
                                        isCurrentSong = stablePlayerState.currentSong?.id == song.id,
                                        isPlaying = stablePlayerState.isPlaying,
                                        onSongClick = {
                                            playerViewModel.showAndPlaySong(song, section.songs)
                                        },
                                        onMoreOptionsClick = {
                                            playerViewModel.selectSongForInfo(song)
                                            showSongInfoBottomSheet = true
                                        }
                                    )
                                }
                            }

                            item(key = "${sectionKey}_footer") {
                                Spacer(
                                    modifier = Modifier.height(
                                        if (index == albumSections.lastIndex) 24.dp else 16.dp
                                    )
                                )
                            }
                        }


                    }

                    if (collapseFraction > 0.95f) {
                        ExpressiveScrollBar(
                            listState = lazyListState,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(
                                    top = currentTopBarHeightDp + 12.dp,
                                    bottom = MiniPlayerHeight + systemNavBarInset + 8.dp
                                )
                        )
                    }

                    CustomCollapsingTopBar(
                        artist = artist,
                        effectiveImageUrl = uiState.effectiveImageUrl,
                        hasCustomImage = !artist.customImageUri.isNullOrBlank(),
                        songsCount = songs.size,
                        collapseFraction = collapseFraction,
                        headerHeight = currentTopBarHeightDp,
                        onBackPressed = { navController.popBackStack() },
                        onPlayClick = {
                            if (songs.isNotEmpty()) {
                                playerViewModel.playSongsShuffled(songs, artist.name, startAtZero = true)
                            }
                        },
                        onChangeImage = { imagePickerLauncher.launch("image/*") },
                        onClearCustomImage = { viewModel.clearCustomImage() }
                    )
                }
            }
        }
    } // End Surface

    // Bottom sheets inherit the artist's dynamic color palette — same approach as AlbumDetailScreen
    if (showSongInfoBottomSheet && selectedSongForInfo != null) {
        val currentSong = selectedSongForInfo
        val isFavorite = remember(currentSong?.id, favoriteIds) {
            currentSong?.let { favoriteIds.contains(it.id) } ?: false
        }

        if (currentSong != null) {
            val removeFromListTrigger = remember(uiState.songs) {
                {
                    viewModel.removeSongFromAlbumSection(currentSong.id)
                }
            }
            SongInfoBottomSheet(
                song = currentSong,
                isFavorite = isFavorite,
                onToggleFavorite = {
                    playerViewModel.toggleFavoriteSpecificSong(currentSong)
                },
                onDismiss = { showSongInfoBottomSheet = false },
                onPlaySong = {
                    playerViewModel.showAndPlaySong(currentSong)
                    showSongInfoBottomSheet = false
                },
                onAddToQueue = {
                    playerViewModel.addSongToQueue(currentSong)
                    showSongInfoBottomSheet = false
                },
                onAddNextToQueue = {
                    playerViewModel.addSongNextToQueue(currentSong)
                    showSongInfoBottomSheet = false
                },
                onAddToPlayList = {
                    showPlaylistBottomSheet = true;
                },
                onDeleteFromDevice = playerViewModel::deleteFromDevice,
                onNavigateToAlbum = {
                    navController.navigateSafely(Screen.AlbumDetail.createRoute(currentSong.albumId))
                    showSongInfoBottomSheet = false
                },
                onNavigateToArtist = {
                    navController.navigateSafely(Screen.ArtistDetail.createRoute(currentSong.artistId))
                    showSongInfoBottomSheet = false
                },
                onEditSong = { newTitle, newArtist, newAlbum, newGenre, newLyrics, newTrackNumber, coverArtUpdate ->
                    playerViewModel.editSongMetadata(currentSong, newTitle, newArtist, newAlbum, newGenre, newLyrics, newTrackNumber, coverArtUpdate)
                },
                generateAiMetadata = { fields ->
                    playerViewModel.generateAiMetadata(currentSong, fields)
                },
                removeFromListTrigger = removeFromListTrigger
            )
            if (showPlaylistBottomSheet) {
                val playlistUiState by playlistViewModel.uiState.collectAsStateWithLifecycle()

                PlaylistBottomSheet(
                    playlistUiState = playlistUiState,
                    songs = listOf(currentSong),
                    onDismiss = { showPlaylistBottomSheet = false },
                    bottomBarHeight = bottomBarHeightDp,
                    playerViewModel = playerViewModel,
                )
            }
        }
    }
    } // End MaterialTheme
}

private fun ArtistAlbumSection.collapseKey(): String = "artist_album_${albumId}_${title}"

@Composable
private fun CollapsibleAlbumSectionHeader(
    section: ArtistAlbumSection,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    modifier: Modifier = Modifier,
    onPlayAlbum: () -> Unit
) {
    val expandIconRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 260),
        label = "ArtistAlbumExpandRotation"
    )
    val songsText = stringResource(id = com.theveloper.pixelplay.R.string.common_songs_count, section.songs.size)
    val subtitle = remember(section.year, section.songs.size, songsText) {
        buildString {
            section.year?.takeIf { it > 0 }?.let {
                append(it.toString())
                append(" • ")
            }
            append(songsText)
        }
    }

    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
    val shape = if (isExpanded) {
        AbsoluteSmoothCornerShape(
            cornerRadiusTR = 24.dp, smoothnessAsPercentTR = 60,
            cornerRadiusTL = 24.dp, smoothnessAsPercentTL = 60,
            cornerRadiusBR = 0.dp, smoothnessAsPercentBR = 0,
            cornerRadiusBL = 0.dp, smoothnessAsPercentBL = 0
        )
    } else {
        AbsoluteSmoothCornerShape(24.dp, 60)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor, shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .clickable(onClick = onToggleExpanded)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmartImage(
                model = section.albumArtUriString,
                contentDescription = section.title,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            FilledTonalIconButton(
                onClick = onPlayAlbum,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(id = com.theveloper.pixelplay.R.string.artist_play, section.title))
            }
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = if (isExpanded) stringResource(id = com.theveloper.pixelplay.R.string.artist_collapse, section.title) else stringResource(id = com.theveloper.pixelplay.R.string.artist_expand, section.title),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.graphicsLayer {
                    rotationZ = expandIconRotation
                }
            )
        }
    }
}

@Composable
private fun ArtistAlbumSectionSongItem(
    song: Song,
    songIndex: Int,
    songCount: Int,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    onSongClick: () -> Unit,
    onMoreOptionsClick: () -> Unit
) {
    val isLastSong = songIndex == songCount - 1

    val songItemShape = when {
        songCount == 1 -> RoundedCornerShape(16.dp)
        songIndex == 0 -> RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp
        )
        isLastSong -> RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
        else -> RoundedCornerShape(4.dp)
    }

    val containerShape = if (isLastSong) {
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
            .padding(horizontal = 8.dp)
    ) {
        Column {
            if (songIndex > 0) {
                Spacer(modifier = Modifier.height(2.dp))
            }

            EnhancedSongListItem(
                modifier = Modifier.fillMaxWidth(),
                song = song,
                isCurrentSong = isCurrentSong,
                isPlaying = isPlaying,
                showAlbumArt = false,
                customShape = songItemShape,
                onMoreOptionsClick = { onMoreOptionsClick() },
                onClick = onSongClick
            )

            if (isLastSong) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CustomCollapsingTopBar(
    artist: Artist,
    effectiveImageUrl: String?,
    hasCustomImage: Boolean,
    songsCount: Int,
    collapseFraction: Float, // 0.0 = expandido, 1.0 = colapsado
    headerHeight: Dp,
    onBackPressed: () -> Unit,
    onPlayClick: () -> Unit,
    onChangeImage: () -> Unit,
    onClearCustomImage: () -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val statusBarColor = if (LocalPixelPlayDarkTheme.current) Color.Black.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.4f)

    // --- Animation Values ---
    val fabScale = 1f - collapseFraction
    val backgroundAlpha = collapseFraction
    val headerContentAlpha = 1f - (collapseFraction * 2).coerceAtMost(1f)

    // Title animation
    val titleScale = lerp(1f, 0.75f, collapseFraction)
    val titlePaddingStart = lerp(24.dp, 58.dp, collapseFraction)
    val titleMaxLines = if(collapseFraction < 0.5f) 2 else 1
    val titleVerticalBias = lerp(1f, -1f, collapseFraction)
    val animatedTitleAlignment = BiasAlignment(horizontalBias = -1f, verticalBias = titleVerticalBias)
    val titleContainerHeight = lerp(88.dp, 56.dp, collapseFraction)
    val yOffsetCorrection = lerp( (titleContainerHeight / 2) - 64.dp, 0.dp, collapseFraction)


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
            .clipToBounds()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .background(surfaceColor.copy(alpha = backgroundAlpha))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = HeaderVisualOverscan
                        scaleY = HeaderVisualOverscan
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
            ) {
                // --- Contenido del Header (visible cuando está expandido) ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = headerContentAlpha }
                ) {
                    // Artist artwork or fallback pattern
                    val displayUrl = effectiveImageUrl?.takeIf { it.isNotBlank() }
                    if (!displayUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(displayUrl)
                                .size(600, 600)
                                .crossfade(true)
                                .build(),
                            contentDescription = artist.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        MusicIconPattern(
                            modifier = Modifier.fillMaxSize(),
                            collapseFraction = collapseFraction
                        )
                    }

                    // Gradient overlay for text readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithCache {
                                val liftPx = HeaderGradientLift.toPx()
                                val brush = Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.22f to Color.Transparent,
                                        0.56f to surfaceColor.copy(alpha = 0.30f),
                                        0.80f to surfaceColor.copy(alpha = 0.90f),
                                        0.91f to surfaceColor,
                                        1f to surfaceColor
                                    ),
                                    startY = -liftPx,
                                    endY = size.height - liftPx
                                )
                                onDrawBehind { drawRect(brush = brush) }
                            }
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Brush.verticalGradient(colors = listOf(statusBarColor, Color.Transparent)))
                        .align(Alignment.TopCenter)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                FilledIconButton(
                    modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 4.dp),
                    onClick = onBackPressed,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = com.theveloper.pixelplay.R.string.common_back))
                }

                // Image edit button (visible only when header is mostly expanded)
                if (collapseFraction < 0.5f) {
                    var showImageMenu by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 12.dp, top = 4.dp)
                            .graphicsLayer { alpha = 1f - (collapseFraction * 4).coerceAtMost(1f) }
                    ) {
                        SmallFloatingActionButton(
                            onClick = { showImageMenu = true },
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = stringResource(id = com.theveloper.pixelplay.R.string.artist_edit_image_cd))
                        }
                        DropdownMenu(
                            expanded = showImageMenu,
                            onDismissRequest = { showImageMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = com.theveloper.pixelplay.R.string.artist_change_photo)) },
                                leadingIcon = { Icon(Icons.Rounded.AddAPhoto, contentDescription = null) },
                                onClick = {
                                    showImageMenu = false
                                    onChangeImage()
                                }
                            )
                            if (hasCustomImage) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = com.theveloper.pixelplay.R.string.artist_reset_image)) },
                                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                                    onClick = {
                                        showImageMenu = false
                                        onClearCustomImage()
                                    }
                                )
                            }
                        }
                    }
                }

                // Box contenedor para el título
                Box(
                    modifier = Modifier
                        .align(animatedTitleAlignment)
                        .height(titleContainerHeight)
                        .fillMaxWidth()
                        .offset(y = yOffsetCorrection)
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = titlePaddingStart, end = 120.dp)
                            .graphicsLayer {
                                scaleX = titleScale
                                scaleY = titleScale
                            },
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = artist.name,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 26.sp,
                                textGeometricTransform = TextGeometricTransform(scaleX = 1.2f),
                            ),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = titleMaxLines,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = stringResource(id = com.theveloper.pixelplay.R.string.common_songs_count, songsCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Botón de Play
                LargeExtendedFloatingActionButton(
                    onClick = onPlayClick,
                    shape = RoundedStarShape(sides = 8, curve = 0.05, rotation = 0f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .graphicsLayer {
                            scaleX = fabScale
                            scaleY = fabScale
                            alpha = fabScale
                        }
                ) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = stringResource(id = com.theveloper.pixelplay.R.string.artist_shuffle))
                }
            }
        }
    }
}

@Composable
private fun MusicIconPattern(modifier: Modifier = Modifier, collapseFraction: Float) {
    val color1 = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
    val color2 = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)

    Box(modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer)) {
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null, tint = color1,
            modifier = Modifier.align(Alignment.TopStart).offset(x = lerp(60.dp, 100.dp, collapseFraction), y = lerp(100.dp, 10.dp, collapseFraction)).size(60.dp).graphicsLayer { rotationZ = lerp(-15f, 30f, collapseFraction); scaleX = 1f - collapseFraction; scaleY = 1f - collapseFraction }
        )
        Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null, tint = color1,
            modifier = Modifier.align(Alignment.CenterStart).offset(x = lerp(20.dp,
                (-40).dp, collapseFraction), y = lerp(50.dp, 90.dp, collapseFraction)).size(50.dp).graphicsLayer { rotationZ = lerp(5f, 45f, collapseFraction); scaleX = 1f - collapseFraction; scaleY = 1f - collapseFraction }
        )
        Icon(
            imageVector = Icons.Rounded.Album,
            contentDescription = null, tint = color2,
            modifier = Modifier.align(Alignment.CenterEnd).offset(x = lerp((-40).dp, 20.dp, collapseFraction), y = lerp(-50.dp, -90.dp, collapseFraction)).size(70.dp).graphicsLayer { rotationZ = lerp(20f, -10f, collapseFraction); scaleX = 1f - collapseFraction; scaleY = 1f - collapseFraction }
        )
        Icon(
            imageVector = Icons.Rounded.Mic,
            contentDescription = null, tint = color1,
            modifier = Modifier.align(Alignment.BottomCenter).offset(x = lerp(20.dp, 10.dp, collapseFraction),y = lerp((-40).dp, 20.dp, collapseFraction)).size(60.dp).graphicsLayer { rotationZ = lerp(-5f, 35f, collapseFraction); scaleX = 1f - collapseFraction; scaleY = 1f - collapseFraction }
        )
        Icon(
            imageVector = Icons.Rounded.SurroundSound,
            contentDescription = null, tint = color2,
            modifier = Modifier.align(Alignment.TopCenter).offset(y = lerp(60.dp, 10.dp, collapseFraction), x = lerp(0.dp, -50.dp, collapseFraction)).size(80.dp).graphicsLayer { rotationZ = lerp(-10f, 20f, collapseFraction); scaleX = 1f - collapseFraction; scaleY = 1f - collapseFraction }
        )
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null, tint = color1,
            modifier = Modifier.align(Alignment.BottomEnd).offset(x = lerp((-30).dp, (-10).dp, collapseFraction), y = lerp(-120.dp, -150.dp, collapseFraction)).size(45.dp).graphicsLayer { rotationZ = lerp(15f, -30f, collapseFraction); scaleX = 1f - collapseFraction; scaleY = 1f - collapseFraction }
        )
        Icon(
            imageVector = Icons.Rounded.Headphones,
            contentDescription = null, tint = color2,
            modifier = Modifier.align(Alignment.Center).offset(x = lerp(60.dp, 80.dp, collapseFraction), y = lerp(20.dp, 60.dp, collapseFraction)).size(45.dp).graphicsLayer { rotationZ = lerp(-25f, 15f, collapseFraction); scaleX = 1f - collapseFraction; scaleY = 1f - collapseFraction }
        )
    }
}
