@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.theveloper.pixelplay.presentation.screens

import android.os.Trace
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Deselect
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material.icons.automirrored.rounded.ViewList
import com.theveloper.pixelplay.data.model.StorageFilter
import com.theveloper.pixelplay.presentation.components.ToggleSegmentButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.theveloper.pixelplay.ui.theme.LocalPixelPlayDarkTheme
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.presentation.components.ShimmerBox
import com.theveloper.pixelplay.data.model.Album
import com.theveloper.pixelplay.data.model.Artist
import com.theveloper.pixelplay.data.model.MusicFolder
import com.theveloper.pixelplay.data.model.FolderSource
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.model.SortOption
import com.theveloper.pixelplay.presentation.components.MiniPlayerHeight
import com.theveloper.pixelplay.presentation.components.NavBarContentHeight
import com.theveloper.pixelplay.presentation.components.SmartImage
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.res.stringResource
import com.theveloper.pixelplay.presentation.components.PlaylistArtCollage
import com.theveloper.pixelplay.presentation.components.ReorderTabsSheet
import com.theveloper.pixelplay.presentation.components.SongInfoBottomSheet
import com.theveloper.pixelplay.presentation.components.subcomps.LibraryActionRow
import com.theveloper.pixelplay.presentation.navigation.Screen
import com.theveloper.pixelplay.presentation.components.MultiSelectionBottomSheet
import com.theveloper.pixelplay.presentation.components.PlaylistMultiSelectionBottomSheet
import com.theveloper.pixelplay.presentation.components.PlaylistCreationTypeDialog
import com.theveloper.pixelplay.presentation.components.CreateAiPlaylistDialog
import com.theveloper.pixelplay.presentation.components.subcomps.SelectionActionRow
import com.theveloper.pixelplay.presentation.components.subcomps.SelectionCountPill
import com.theveloper.pixelplay.presentation.viewmodel.ColorSchemePair
import com.theveloper.pixelplay.presentation.viewmodel.PlayerUiState
import com.theveloper.pixelplay.presentation.viewmodel.PlayerViewModel
import com.theveloper.pixelplay.presentation.viewmodel.StablePlayerState
import com.theveloper.pixelplay.presentation.viewmodel.PlaylistUiState
import com.theveloper.pixelplay.presentation.viewmodel.PlaylistViewModel
import com.theveloper.pixelplay.data.model.LibraryTabId
import com.theveloper.pixelplay.data.model.toLibraryTabIdOrNull
import com.theveloper.pixelplay.data.preferences.LibraryNavigationMode
import com.theveloper.pixelplay.data.worker.SyncProgress
import com.theveloper.pixelplay.presentation.screens.search.components.GenreTypography
import com.theveloper.pixelplay.presentation.components.SyncProgressBar
import com.theveloper.pixelplay.presentation.viewmodel.LibraryViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import com.theveloper.pixelplay.presentation.components.AutoScrollingTextOnDemand
import com.theveloper.pixelplay.presentation.screens.CreatePlaylistDialog
import com.theveloper.pixelplay.presentation.components.PlaylistBottomSheet
import com.theveloper.pixelplay.presentation.components.PlaylistContainer
import com.theveloper.pixelplay.presentation.components.subcomps.PlayingEqIcon
import com.theveloper.pixelplay.ui.theme.GoogleSansRounded
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.theveloper.pixelplay.data.model.PlaylistShapeType
import kotlinx.coroutines.flow.first
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.LoadState
import com.theveloper.pixelplay.presentation.components.ExpressiveScrollBar
import com.theveloper.pixelplay.presentation.components.LibrarySortBottomSheet
import com.theveloper.pixelplay.presentation.components.subcomps.EnhancedSongListItem
import kotlin.math.abs

val ListExtraBottomGap = 30.dp
val PlayerSheetCollapsedCornerRadius = 32.dp

private data class LibraryScreenPlayerProjection(
    val currentFolder: MusicFolder? = null,
    val folderSourceRootPath: String = "",
    val folderSource: FolderSource = FolderSource.INTERNAL,
    val isFoldersPlaylistView: Boolean = false,
    val currentStorageFilter: StorageFilter = StorageFilter.ALL,
    val currentSongSortOption: SortOption = SortOption.SongTitleAZ,
    val currentAlbumSortOption: SortOption = SortOption.AlbumTitleAZ,
    val currentArtistSortOption: SortOption = SortOption.ArtistNameAZ,
    val currentFavoriteSortOption: SortOption = SortOption.LikedSongDateLiked,
    val currentFolderSortOption: SortOption = SortOption.FolderNameAZ,
    val isAlbumsListView: Boolean = false,
    val isSdCardAvailable: Boolean = false,
    val musicFolders: ImmutableList<MusicFolder> = persistentListOf(),
    val isLoadingLibraryCategories: Boolean = true,
    val isGeneratingAiMetadata: Boolean = false,
    val isSyncingLibrary: Boolean = false,
    val isLoadingInitialSongs: Boolean = true
)

private fun PlayerUiState.toLibraryScreenProjection(): LibraryScreenPlayerProjection =
    LibraryScreenPlayerProjection(
        currentFolder = currentFolder,
        folderSourceRootPath = folderSourceRootPath,
        folderSource = folderSource,
        isFoldersPlaylistView = isFoldersPlaylistView,
        currentStorageFilter = currentStorageFilter,
        currentSongSortOption = currentSongSortOption,
        currentAlbumSortOption = currentAlbumSortOption,
        currentArtistSortOption = currentArtistSortOption,
        currentFavoriteSortOption = currentFavoriteSortOption,
        currentFolderSortOption = currentFolderSortOption,
        isAlbumsListView = isAlbumsListView,
        isSdCardAvailable = isSdCardAvailable,
        musicFolders = musicFolders,
        isLoadingLibraryCategories = isLoadingLibraryCategories,
        isGeneratingAiMetadata = isGeneratingAiMetadata,
        isSyncingLibrary = isSyncingLibrary,
        isLoadingInitialSongs = isLoadingInitialSongs
    )

private fun targetPageForTabIndex(
    currentPage: Int,
    targetTabIndex: Int,
    tabCount: Int,
    compactMode: Boolean
): Int {
    // For now, simply return the target tab index
    // This function can be extended later if there's complex logic for different navigation modes
    return targetTabIndex.coerceIn(0, (tabCount - 1).coerceAtLeast(0))
}

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun LibraryScreen(
    navController: NavController,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel()
) {
    // La recolección de estados de alto nivel se mantiene mínima.
    val context = LocalContext.current // Added context
    val lastTabIndex by playerViewModel.lastLibraryTabIndexFlow.collectAsStateWithLifecycle()
    val favoriteIds by playerViewModel.favoriteSongIds.collectAsStateWithLifecycle() // Reintroducir favoriteIds aquí
    val scope = rememberCoroutineScope() // Mantener si se usa para acciones de UI
    val syncManager = playerViewModel.syncManager
    var isRefreshing by remember { mutableStateOf(false) }
    val isSyncing by syncManager.isSyncing.collectAsStateWithLifecycle(initialValue = false)
    val syncProgress by syncManager.syncProgress.collectAsStateWithLifecycle(initialValue = SyncProgress())

    var showSongInfoBottomSheet by remember { mutableStateOf(false) }
    var showPlaylistBottomSheet by remember { mutableStateOf(false) }
    var playlistSheetSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    val selectedSongForInfo by playerViewModel.selectedSongForInfo.collectAsStateWithLifecycle()
    val tabTitles by playerViewModel.libraryTabsFlow.collectAsStateWithLifecycle()
    val currentTabId by playerViewModel.currentLibraryTabId.collectAsStateWithLifecycle()
    val tabCount = tabTitles.size.coerceAtLeast(1)
    val normalizedLastTabIndex = positiveMod(lastTabIndex, tabCount)
    val pagerState = rememberPagerState(initialPage = normalizedLastTabIndex) { tabCount }
    val currentTabIndex by remember(pagerState, tabTitles) {
        derivedStateOf {
            pagerState.currentPage
        }
    }
    val isSortSheetVisible by playerViewModel.isSortingSheetVisible.collectAsStateWithLifecycle()
    val canNavigateBackInFolders by remember(playerViewModel) {
        playerViewModel.playerUiState
            .map { uiState -> uiState.currentFolder != null && uiState.folderBackGestureNavigationEnabled }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(initialValue = false)
    val hasGeminiApiKey by playerViewModel.hasGeminiApiKey.collectAsStateWithLifecycle()
    val isGeneratingAiPlaylist by playerViewModel.isGeneratingAiPlaylist.collectAsStateWithLifecycle()
    val aiError by playerViewModel.aiError.collectAsStateWithLifecycle()
    val libraryNavigationMode by playerViewModel.libraryNavigationMode.collectAsStateWithLifecycle()
    val isCompactNavigation = libraryNavigationMode == LibraryNavigationMode.COMPACT_PILL
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showPlaylistCreationTypeDialog by remember { mutableStateOf(false) }
    var showCreateAiPlaylistDialog by remember { mutableStateOf(false) }
    var aiGenerationRequestedFromDialog by remember { mutableStateOf(false) }

    val m3uImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { playlistViewModel.importM3u(it) }
    }

    var showReorderTabsSheet by remember { mutableStateOf(false) }
    var showTabSwitcherSheet by remember { mutableStateOf(false) }

    // Multi-selection state
    val multiSelectionState = playerViewModel.multiSelectionStateHolder
    val selectedSongs by multiSelectionState.selectedSongs.collectAsStateWithLifecycle()
    val isSelectionMode by multiSelectionState.isSelectionMode.collectAsStateWithLifecycle()
    val selectedSongIds by multiSelectionState.selectedSongIds.collectAsStateWithLifecycle()
    var showMultiSelectionSheet by remember { mutableStateOf(false) }

    var songsShowLocateButton by remember { mutableStateOf(false) }
    var likedShowLocateButton by remember { mutableStateOf(false) }
    var foldersShowLocateButton by remember { mutableStateOf(false) }
    var songsLocateAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var likedLocateAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var foldersLocateAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Multi-selection callbacks
    val onSongLongPress: (Song) -> Unit = remember(multiSelectionState) {
        { song -> multiSelectionState.toggleSelection(song) }
    }

    val onSongSelectionToggle: (Song) -> Unit = remember(multiSelectionState) {
        { song -> multiSelectionState.toggleSelection(song) }
    }

    // Playlist multi-selection state and callbacks
    val playlistMultiSelectionState = playerViewModel.playlistSelectionStateHolder
    val selectedPlaylists by playlistMultiSelectionState.selectedPlaylists.collectAsState()
    val selectedPlaylistIds by playlistMultiSelectionState.selectedPlaylistIds.collectAsState()
    var showPlaylistMultiSelectionSheet by remember { mutableStateOf(false) }
    var showMergePlaylistDialog by remember { mutableStateOf(false) }
    var pendingMergePlaylistIds by remember { mutableStateOf(emptyList<String>()) }

    val onPlaylistLongPress: (com.theveloper.pixelplay.data.model.Playlist) -> Unit = remember(playlistMultiSelectionState) {
        { playlist ->
            // Only toggle selection, don't show sheet immediately (similar to songs multi-selection)
            playlistMultiSelectionState.toggleSelection(playlist)
            android.util.Log.d("PlaylistMultiSelect", "Toggled: ${playlist.name}, total selected: ${playlistMultiSelectionState.selectedPlaylists.value.size}")
        }
    }

    val onPlaylistSelectionToggle: (com.theveloper.pixelplay.data.model.Playlist) -> Unit = remember(playlistMultiSelectionState) {
        { playlist -> playlistMultiSelectionState.toggleSelection(playlist) }
    }

    val stableOnMoreOptionsClick: (Song) -> Unit = remember {
        { song ->
            playerViewModel.selectSongForInfo(song)
            showSongInfoBottomSheet = true
        }
    }
    // Pull-to-refresh uses incremental sync for speed
    // We enforce a minimum duration of 3.5s for the animation as requested by the user.
    var isMinDelayActive by remember { mutableStateOf(false) }

    val onRefresh: () -> Unit = remember {
        {
            isMinDelayActive = true
            isRefreshing = true
            syncManager.incrementalSync()
            scope.launch {
                kotlinx.coroutines.delay(3500)
                isMinDelayActive = false
                // If sync finished during the delay, the LaunchedEffect blocked the update.
                // We must manually check and turn it off if needed.
                val currentlySyncing = syncManager.isSyncing.first()
                if (!currentlySyncing) {
                    isRefreshing = false
                }
            }
        }
    }

    LaunchedEffect(isSyncing) {
        if (isSyncing) {
            isRefreshing = true
        } else {
            // Only hide refresh indicator if the minimum delay has passed
            if (!isMinDelayActive) {
                isRefreshing = false
            }
        }
    }

    BackHandler(
        enabled =
            currentTabId == LibraryTabId.FOLDERS &&
                    canNavigateBackInFolders &&
                    !isSortSheetVisible
    ) {
        playerViewModel.navigateBackFolder()
    }

    // Feedback for Playlist Creation
    LaunchedEffect(Unit) {
        playlistViewModel.playlistCreationEvent.collect { success ->
            if (success) {
                showCreatePlaylistDialog = false
                Toast.makeText(context, context.getString(R.string.playlist_created_successfully), Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(
        showCreateAiPlaylistDialog,
        aiGenerationRequestedFromDialog,
        isGeneratingAiPlaylist,
        aiError
    ) {
        if (!showCreateAiPlaylistDialog || !aiGenerationRequestedFromDialog || isGeneratingAiPlaylist) {
            return@LaunchedEffect
        }

        if (aiError == null) {
            showCreateAiPlaylistDialog = false
            playerViewModel.clearAiPlaylistError()
        }
        aiGenerationRequestedFromDialog = false
    }

    LaunchedEffect(hasGeminiApiKey, showCreateAiPlaylistDialog) {
        if (!hasGeminiApiKey && showCreateAiPlaylistDialog) {
            showCreateAiPlaylistDialog = false
            aiGenerationRequestedFromDialog = false
            playerViewModel.clearAiPlaylistError()
        }
    }
    // La lógica de carga diferida (lazy loading) se mantiene.
    LaunchedEffect(Unit) {
        Trace.beginSection("LibraryScreen.InitialTabLoad")
        playerViewModel.onLibraryTabSelected(normalizedLastTabIndex)
        Trace.endSection()
    }

    LaunchedEffect(currentTabIndex) {
        Trace.beginSection("LibraryScreen.PageChangeTabLoad")
        playerViewModel.onLibraryTabSelected(currentTabIndex)
        Trace.endSection()

        // Clear selection when switching tabs
        multiSelectionState.clearSelection()
    }

    val fabState by remember { derivedStateOf { currentTabIndex } } // UI sin cambios
    val transition = updateTransition(
        targetState = fabState,
        label = "Action Button Icon Transition"
    ) // UI sin cambios

    val systemNavBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomBarHeightDp = NavBarContentHeight + systemNavBarInset

    val dm = LocalPixelPlayDarkTheme.current

    val iconRotation by transition.animateFloat(
        label = "Action Button Icon Rotation",
        transitionSpec = {
            tween(durationMillis = 300, easing = FastOutSlowInEasing)
        }
    ) { page ->
        when (tabTitles.getOrNull(page)?.toLibraryTabIdOrNull()) {
            LibraryTabId.PLAYLISTS -> 0f // Playlist icon (PlaylistAdd) usually doesn't rotate
            else -> 360f // Shuffle icon animates
        }
    }

    val gradientColorsDark = listOf(
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        Color.Transparent
    ).toImmutableList()

    val gradientColorsLight = listOf(
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
        Color.Transparent
    ).toImmutableList()

    val gradientColors = if (dm) gradientColorsDark else gradientColorsLight

    val gradientBrush = remember(gradientColors) {
        Brush.verticalGradient(colors = gradientColors)
    }

    val currentTab = tabTitles.getOrNull(currentTabIndex)?.toLibraryTabIdOrNull() ?: currentTabId
    val currentTabTitle = currentTab.displayTitle()

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.background(brush = gradientBrush),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                title = {
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(R.string.nav_library),
                        fontFamily = GoogleSansRounded,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    FilledIconButton(
                        modifier = Modifier.padding(end = 14.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onClick = {
                            navController.navigate(Screen.Settings.route)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_settings_24),
                            contentDescription = stringResource(R.string.nav_settings)
                        )
                    }
                }
            )
        }
    ) { innerScaffoldPadding ->
        val playerUiState by remember(playerViewModel) {
            playerViewModel.playerUiState
                .map { uiState -> uiState.toLibraryScreenProjection() }
                .distinctUntilChanged()
        }.collectAsStateWithLifecycle(initialValue = LibraryScreenPlayerProjection())
        val isLibraryContentEmpty by remember(playerViewModel) {
            combine(
                playerViewModel.allSongsFlow,
                playerViewModel.albumsFlow,
                playerViewModel.artistsFlow
            ) { allSongs, albums, artists ->
                allSongs.isEmpty() && albums.isEmpty() && artists.isEmpty()
            }.distinctUntilChanged()
        }.collectAsStateWithLifecycle(initialValue = true)

        Box( // Box para permitir superposición del indicador de carga
            modifier = Modifier
                .padding(top = innerScaffoldPadding.calculateTopPadding())
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                SecondaryScrollableTabRow(
                    selectedTabIndex = currentTabIndex,
                    containerColor = Color.Transparent,
                    edgePadding = 12.dp,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(currentTabIndex),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    divider = {}
                ) {
                    tabTitles.forEachIndexed { index, rawId ->
                        val tabId = rawId.toLibraryTabIdOrNull() ?: LibraryTabId.SONGS
                        TabAnimation(
                            index = index,
                            title = tabId.storageKey,
                            selectedIndex = currentTabIndex,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        ) {
                            Text(
                                text = stringResource(tabId.titleResId),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (currentTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                    TabAnimation(
                        index = -1, // A non-matching index to keep it unselected
                        title = stringResource(R.string.library_tab_edit),
                        selectedIndex = currentTabIndex,
                        onClick = { showReorderTabsSheet = true }
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.library_tab_reorder_cd),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 0.dp, vertical = 0.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shape = AbsoluteSmoothCornerShape(
                        cornerRadiusTL = 24.dp,
                        smoothnessAsPercentBL = 60,
                        cornerRadiusBL = 0.dp,
                        smoothnessAsPercentBR = 60,
                        cornerRadiusBR = 0.dp,
                        smoothnessAsPercentTR = 60,
                        cornerRadiusTR = 24.dp,
                        smoothnessAsPercentTL = 60
                    )
                ) {
                    Column(Modifier.fillMaxSize()) {
                        // OPTIMIZACIÓN: La lógica de ordenamiento ahora es más eficiente.
                        val availableSortOptions by playerViewModel.availableSortOptions.collectAsStateWithLifecycle()
                        val sanitizedSortOptions = remember(availableSortOptions, currentTabId) {
                            val cleaned = availableSortOptions.filterIsInstance<SortOption>()
                            val ensured = if (cleaned.any { option ->
                                    option.storageKey == currentTabId.defaultSort.storageKey
                                }
                            ) {
                                cleaned
                            } else {
                                buildList {
                                    add(currentTabId.defaultSort)
                                    addAll(cleaned)
                                }
                            }

                            val distinctByKey = ensured.distinctBy { it.storageKey }
                            distinctByKey.ifEmpty { listOf(currentTabId.defaultSort) }
                        }

                        val playlistUiState by playlistViewModel.uiState.collectAsStateWithLifecycle()
                        val stablePlayerState by playerViewModel.stablePlayerState.collectAsStateWithLifecycle()
                        val isPlaylistSelectionMode by playlistMultiSelectionState.isSelectionMode.collectAsState()

                        val favoritePagingItems = libraryViewModel.favoritesPagingFlow.collectAsLazyPagingItems()

                        val currentSelectedSortOption: SortOption? = when (currentTabId) {
                            LibraryTabId.SONGS -> playerUiState.currentSongSortOption
                            LibraryTabId.ALBUMS -> playerUiState.currentAlbumSortOption
                            LibraryTabId.ARTISTS -> playerUiState.currentArtistSortOption
                            LibraryTabId.PLAYLISTS -> playlistUiState.currentPlaylistSortOption
                            LibraryTabId.LIKED -> playerUiState.currentFavoriteSortOption
                            LibraryTabId.FOLDERS -> playerUiState.currentFolderSortOption
                        }

                        val showLocateButton = when (currentTabId) {
                            LibraryTabId.SONGS -> songsShowLocateButton
                            LibraryTabId.LIKED -> likedShowLocateButton
                            LibraryTabId.FOLDERS -> foldersShowLocateButton
                            else -> false
                        }
                        val locateAction = when (currentTabId) {
                            LibraryTabId.SONGS -> songsLocateAction
                            LibraryTabId.LIKED -> likedLocateAction
                            LibraryTabId.FOLDERS -> foldersLocateAction
                            else -> null
                        }

                        val onSortOptionChanged: (SortOption) -> Unit = remember(playerViewModel, playlistViewModel, currentTabId) {
                            { option ->
                                when (currentTabId) {
                                    LibraryTabId.SONGS -> playerViewModel.sortSongs(option)
                                    LibraryTabId.ALBUMS -> playerViewModel.sortAlbums(option)
                                    LibraryTabId.ARTISTS -> playerViewModel.sortArtists(option)
                                    LibraryTabId.PLAYLISTS -> playlistViewModel.sortPlaylists(option)
                                    LibraryTabId.LIKED -> playerViewModel.sortFavoriteSongs(option)
                                    LibraryTabId.FOLDERS -> playerViewModel.sortFolders(option)
                                }
                            }
                        }

                        // Switch between normal action row and selection action row
                        AnimatedContent(
                            targetState = isSelectionMode || isPlaylistSelectionMode,
                            label = "ActionRowModeSwitch",
                            transitionSpec = {
                                (slideInHorizontally { -it } + fadeIn()) togetherWith
                                        (slideOutHorizontally { it } + fadeOut())
                            },
                            modifier = Modifier
                                .padding(
                                    top = 6.dp,
                                    start = 10.dp,
                                    end = 10.dp
                                )
                                .heightIn(min = 56.dp)
                        ) { inSelectionMode ->
                            if (inSelectionMode) {
                                // Check if PLAYLISTS is in selection mode
                                if (currentTabId == LibraryTabId.PLAYLISTS && isPlaylistSelectionMode) {
                                    // Playlist selection row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left side: Select All + Deselect
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            FilledTonalButton(
                                                onClick = {
                                                    playerViewModel.playlistSelectionStateHolder.selectAll(playlistUiState.playlists)
                                                },
                                                shape = AbsoluteSmoothCornerShape(
                                                    cornerRadiusTL = 12.dp,
                                                    cornerRadiusBL = 12.dp,
                                                    cornerRadiusTR = 8.dp,
                                                    cornerRadiusBR = 8.dp,
                                                    smoothnessAsPercentTL = 60,
                                                    smoothnessAsPercentBL = 60,
                                                    smoothnessAsPercentTR = 60,
                                                    smoothnessAsPercentBR = 60
                                                ),
                                                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                ),
                                                contentPadding = PaddingValues(horizontal = 14.dp),
                                                modifier = Modifier.height(40.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.SelectAll,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = stringResource(id = R.string.selection_all),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Medium,
                                                    fontFamily = GoogleSansRounded
                                                )
                                            }

                                            FilledTonalButton(
                                                onClick = { playerViewModel.playlistSelectionStateHolder.clearSelection() },
                                                shape = AbsoluteSmoothCornerShape(
                                                    cornerRadiusTL = 8.dp,
                                                    cornerRadiusBL = 8.dp,
                                                    cornerRadiusTR = 12.dp,
                                                    cornerRadiusBR = 12.dp,
                                                    smoothnessAsPercentTL = 60,
                                                    smoothnessAsPercentBL = 60,
                                                    smoothnessAsPercentTR = 60,
                                                    smoothnessAsPercentBR = 60
                                                ),
                                                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.secondary,
                                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                                ),
                                                contentPadding = PaddingValues(horizontal = 14.dp),
                                                modifier = Modifier.height(40.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Deselect,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = stringResource(id = R.string.selection_deselect),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Medium,
                                                    fontFamily = GoogleSansRounded
                                                )
                                            }
                                        }

                                        // Right side: Options button
                                        FilledTonalButton(
                                            onClick = { showPlaylistMultiSelectionSheet = true },
                                            shape = AbsoluteSmoothCornerShape(
                                                cornerRadiusTL = 12.dp,
                                                cornerRadiusBL = 12.dp,
                                                cornerRadiusTR = 12.dp,
                                                cornerRadiusBR = 12.dp,
                                                smoothnessAsPercentTL = 60,
                                                smoothnessAsPercentBL = 60,
                                                smoothnessAsPercentTR = 60,
                                                smoothnessAsPercentBR = 60
                                            ),
                                            colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            contentPadding = PaddingValues(horizontal = 16.dp),
                                            modifier = Modifier.height(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.MoreHoriz,
                                                contentDescription = stringResource(id = R.string.selection_more_options_cd),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(id = R.string.selection_options),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = GoogleSansRounded
                                            )
                                        }
                                    }
                                } else {
                                    // Song selection row
                                    SelectionActionRow(
                                        selectedCount = selectedSongs.size,
                                        onSelectAll = {
                                            val songsToSelect = when (tabTitles.getOrNull(currentTabIndex)?.toLibraryTabIdOrNull()) {
                                                LibraryTabId.LIKED -> favoritePagingItems.itemSnapshotList.items
                                                LibraryTabId.FOLDERS -> {
                                                    // If we are deep in a folder, select songs of that folder.
                                                    // If we are at root, there are no songs to select.
                                                    playerViewModel.playerUiState.value.currentFolder?.songs ?: emptyList()
                                                }
                                                // For SONGS and others fallback to all songs?
                                                // Actually ALBUMS/ARTISTS don't show songs list directly, they show items.
                                                // Selection mode is likely disabled there or not reachable.
                                                // But for SONGS tab:
                                                LibraryTabId.SONGS -> playerViewModel.allSongsFlow.value
                                                else -> emptyList()
                                            }
                                            multiSelectionState.selectAll(songsToSelect)
                                        },
                                        onDeselect = { multiSelectionState.clearSelection() },
                                        onOptionsClick = { showMultiSelectionSheet = true }
                                    )
                                }
                            } else {
                                LibraryActionRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 4.dp),
                                    onMainActionClick = {
                                        when (tabTitles.getOrNull(currentTabIndex)?.toLibraryTabIdOrNull()) {
                                            LibraryTabId.PLAYLISTS -> showPlaylistCreationTypeDialog = true
                                            LibraryTabId.LIKED -> playerViewModel.shuffleFavoriteSongs()
                                            LibraryTabId.ALBUMS -> playerViewModel.shuffleRandomAlbum()
                                            LibraryTabId.ARTISTS -> playerViewModel.shuffleRandomArtist()
                                            else -> playerViewModel.shuffleAllSongs()
                                        }
                                    },
                                    iconRotation = iconRotation,
                                    showSortButton = sanitizedSortOptions.isNotEmpty(),
                                    showLocateButton = showLocateButton,
                                    onSortClick = { playerViewModel.showSortingSheet() },
                                    onLocateClick = { locateAction?.invoke() },
                                    isPlaylistTab = currentTabId == LibraryTabId.PLAYLISTS,
                                    isFoldersTab = currentTabId == LibraryTabId.FOLDERS && (!playerUiState.isFoldersPlaylistView || playerUiState.currentFolder != null),
                                    onImportM3uClick = { m3uImportLauncher.launch("audio/x-mpegurl") },
                                    currentFolder = playerUiState.currentFolder,
                                    folderRootPath = playerUiState.folderSourceRootPath.ifBlank {
                                        Environment.getExternalStorageDirectory().path
                                    },
                                    folderRootLabel = when (playerUiState.folderSource) {
                                        FolderSource.INTERNAL -> stringResource(R.string.folder_source_internal)
                                        FolderSource.SD_CARD -> stringResource(R.string.folder_source_sd_card)
                                    },
                                    onFolderClick = { playerViewModel.navigateToFolder(it) },
                                    onNavigateBack = { playerViewModel.navigateBackFolder() },
                                    isShuffleEnabled = stablePlayerState.isShuffleEnabled,
                                    showStorageFilterButton = true,
                                    currentStorageFilter = playerUiState.currentStorageFilter,
                                    onStorageFilterClick = { playerViewModel.toggleStorageFilter() }
                                )
                            }
                        }

                        if (isSortSheetVisible && sanitizedSortOptions.isNotEmpty()) {
                            val currentSelectionKey = currentSelectedSortOption?.storageKey
                            val selectedOptionForSheet = sanitizedSortOptions.firstOrNull { option ->
                                option.storageKey == currentSelectionKey
                            }
                                ?: sanitizedSortOptions.firstOrNull { option ->
                                    option.storageKey == currentTabId.defaultSort.storageKey
                                }
                                ?: sanitizedSortOptions.first()


                            val isAlbumTab = currentTabId == LibraryTabId.ALBUMS
                            val isFoldersTab = currentTabId == LibraryTabId.FOLDERS

                            LibrarySortBottomSheet(
                                title = stringResource(R.string.library_sort_title),
                                options = sanitizedSortOptions,
                                selectedOption = selectedOptionForSheet,
                                onDismiss = { playerViewModel.hideSortingSheet() },
                                onOptionSelected = { option ->
                                    onSortOptionChanged(option)
                                    playerViewModel.hideSortingSheet()
                                },
                                showViewToggle = isFoldersTab,
                                viewToggleChecked = playerUiState.isFoldersPlaylistView,
                                onViewToggleChange = { isChecked ->
                                    playerViewModel.setFoldersPlaylistView(isChecked)
                                },
                                viewToggleContent = if (isAlbumTab) {
                                    {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val isList = playerUiState.isAlbumsListView
                                            val primaryColor = MaterialTheme.colorScheme.tertiaryContainer
                                            val onPrimaryColor = MaterialTheme.colorScheme.onTertiaryContainer
                                            val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
                                            val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant

                                            // Grid Item
                                            ToggleSegmentButton(
                                                modifier = Modifier.weight(1f),
                                                active = !isList,
                                                activeColor = MaterialTheme.colorScheme.primary,
                                                inactiveColor = MaterialTheme.colorScheme.surfaceVariant,
                                                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                activeCornerRadius = 32.dp,
                                                onClick = { playerViewModel.setAlbumsListView(false) },
                                                text = stringResource(R.string.library_view_grid),
                                                imageVector = Icons.Rounded.ViewModule
                                            )

                                            // List Item
                                            ToggleSegmentButton(
                                                modifier = Modifier.weight(1f),
                                                active = isList,
                                                activeColor = MaterialTheme.colorScheme.primary,
                                                inactiveColor = MaterialTheme.colorScheme.surfaceVariant,
                                                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                activeCornerRadius = 32.dp,
                                                onClick = { playerViewModel.setAlbumsListView(true) },
                                                text = stringResource(R.string.library_view_list),
                                                imageVector = Icons.AutoMirrored.Rounded.ViewList
                                            )
                                        }
                                    }
                                } else null,
                                sourceToggleContent = if (isFoldersTab) {
                                    {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            val isSdAvailable = playerUiState.isSdCardAvailable
                                            ToggleSegmentButton(
                                                modifier = Modifier.weight(1f),
                                                active = playerUiState.folderSource == FolderSource.INTERNAL,
                                                activeColor = MaterialTheme.colorScheme.primary,
                                                inactiveColor = MaterialTheme.colorScheme.surfaceVariant,
                                                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                activeCornerRadius = 32.dp,
                                                onClick = { playerViewModel.setFoldersSource(FolderSource.INTERNAL) },
                                                text = stringResource(R.string.storage_internal)
                                            )
                                            ToggleSegmentButton(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .alpha(if (isSdAvailable) 1f else 0.5f),
                                                active = playerUiState.folderSource == FolderSource.SD_CARD,
                                                activeColor = MaterialTheme.colorScheme.primary,
                                                inactiveColor = MaterialTheme.colorScheme.surfaceVariant,
                                                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                activeCornerRadius = 32.dp,
                                                onClick = {
                                                    if (isSdAvailable) {
                                                        playerViewModel.setFoldersSource(FolderSource.SD_CARD)
                                                    }
                                                },
                                                text = stringResource(R.string.storage_sd_card)
                                            )
                                        }
                                        if (!playerUiState.isSdCardAvailable) {
                                            Text(
                                                text = stringResource(R.string.storage_sd_card_unavailable),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 8.dp, start = 2.dp)
                                            )
                                        }
                                    }
                                } else null
                            )
                        }

                        // Box wrapper to allow floating SelectionCountPill overlay
                        Box(modifier = Modifier.fillMaxSize()) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 8.dp),
                                pageSpacing = 0.dp,
                                beyondViewportPageCount = 1, // Pre-load adjacent tabs to reduce lag when switching
                                key = { it }
                            ) { tabIndex ->
                                when (tabTitles.getOrNull(tabIndex)?.toLibraryTabIdOrNull()) {
                                    LibraryTabId.SONGS -> {
                                        // Use Paging 3 flow from LibraryStateHolder
                                        val allSongsLazyPagingItems = libraryViewModel.songsPagingFlow.collectAsLazyPagingItems()
                                        // We can use libraryViewModel.isLoadingLibrary or similar if needed for global loading state
                                        val isLibraryLoading by libraryViewModel.isLoadingLibrary.collectAsStateWithLifecycle()

                                        LibrarySongsTab(
                                            songs = allSongsLazyPagingItems,
                                            isLoading = isLibraryLoading,
                                            stablePlayerState = stablePlayerState,
                                            playerViewModel = playerViewModel,
                                            bottomBarHeight = bottomBarHeightDp,
                                            onMoreOptionsClick = stableOnMoreOptionsClick,
                                            isRefreshing = isRefreshing,
                                            onRefresh = {
                                                onRefresh()
                                                allSongsLazyPagingItems.refresh()
                                            },
                                            isSelectionMode = isSelectionMode,
                                            selectedSongIds = selectedSongIds,
                                            onSongLongPress = onSongLongPress,
                                            onSongSelectionToggle = onSongSelectionToggle,
                                            onLocateCurrentSongVisibilityChanged = { songsShowLocateButton = it },
                                            onRegisterLocateCurrentSongAction = { songsLocateAction = it }
                                        )
                                    }
                                    LibraryTabId.ALBUMS -> {
                                        val albums by playerViewModel.albumsFlow.collectAsStateWithLifecycle()
                                        val isLoading = playerUiState.isLoadingLibraryCategories

                                        val stableOnAlbumClick: (Long) -> Unit = remember(navController) {
                                            { albumId: Long ->
                                                navController.navigate(Screen.AlbumDetail.createRoute(albumId))
                                            }
                                        }
                                        LibraryAlbumsTab(
                                            albums = albums,
                                            isLoading = isLoading,
                                            playerViewModel = playerViewModel,
                                            bottomBarHeight = bottomBarHeightDp,
                                            onAlbumClick = stableOnAlbumClick,
                                            isRefreshing = isRefreshing,
                                            onRefresh = onRefresh
                                        )
                                    }

                                    LibraryTabId.ARTISTS -> {
                                        val artists by playerViewModel.artistsFlow.collectAsStateWithLifecycle()
                                        val isLoading = playerUiState.isLoadingLibraryCategories

                                        LibraryArtistsTab(
                                            artists = artists,
                                            isLoading = isLoading,
                                            playerViewModel = playerViewModel,
                                            bottomBarHeight = bottomBarHeightDp,
                                            onArtistClick = { artistId ->
                                                navController.navigate(
                                                    Screen.ArtistDetail.createRoute(
                                                        artistId
                                                    )
                                                )
                                            },
                                            isRefreshing = isRefreshing,
                                            onRefresh = onRefresh
                                        )
                                    }

                                    LibraryTabId.PLAYLISTS -> {
                                        val currentPlaylistUiState by playlistViewModel.uiState.collectAsStateWithLifecycle()
                                        LibraryPlaylistsTab(
                                            playlistUiState = currentPlaylistUiState,
                                            navController = navController,
                                            playerViewModel = playerViewModel,
                                            bottomBarHeight = bottomBarHeightDp,
                                            isRefreshing = isRefreshing,
                                            onRefresh = onRefresh,
                                            // Playlist multi-selection
                                            isSelectionMode = isPlaylistSelectionMode,
                                            selectedPlaylistIds = selectedPlaylistIds,
                                            onPlaylistLongPress = onPlaylistLongPress,
                                            onPlaylistSelectionToggle = onPlaylistSelectionToggle,
                                            onPlaylistOptionsClick = { showPlaylistMultiSelectionSheet = true }
                                        )
                                    }

                                    LibraryTabId.LIKED -> {
                                        LibraryFavoritesTab(
                                            favoriteSongs = favoritePagingItems,
                                            playerViewModel = playerViewModel,
                                            bottomBarHeight = bottomBarHeightDp,
                                            onMoreOptionsClick = stableOnMoreOptionsClick,
                                            isRefreshing = isRefreshing,
                                            onRefresh = {
                                                onRefresh()
                                                favoritePagingItems.refresh()
                                            },
                                            isSelectionMode = isSelectionMode,
                                            selectedSongIds = selectedSongIds,
                                            onSongLongPress = onSongLongPress,
                                            onSongSelectionToggle = onSongSelectionToggle,
                                            getSelectionIndex = playerViewModel.multiSelectionStateHolder::getSelectionIndex,
                                            onLocateCurrentSongVisibilityChanged = { likedShowLocateButton = it },
                                            onRegisterLocateCurrentSongAction = { likedLocateAction = it }
                                        )
                                    }

                                    LibraryTabId.FOLDERS -> {
                                        val context = LocalContext.current
                                        var hasPermission by remember { mutableStateOf(Environment.isExternalStorageManager()) }
                                        val launcher = rememberLauncherForActivityResult(
                                            ActivityResultContracts.StartActivityForResult()
                                        ) {
                                            hasPermission = Environment.isExternalStorageManager()
                                        }

                                        if (hasPermission) {
                                            val folders = playerUiState.musicFolders
                                            val currentFolder = playerUiState.currentFolder
                                            val isLoading = playerUiState.isLoadingLibraryCategories

                                            val defaultFolderName = stringResource(R.string.common_folder)
                                            LibraryFoldersTab(
                                                folders = folders,
                                                currentFolder = currentFolder,
                                                isLoading = isLoading,
                                                bottomBarHeight = bottomBarHeightDp,
                                                stablePlayerState = stablePlayerState,
                                                onNavigateBack = { playerViewModel.navigateBackFolder() },
                                                onFolderClick = { folderPath -> playerViewModel.navigateToFolder(folderPath) },
                                                onFolderAsPlaylistClick = { folder ->
                                                    val encodedPath = Uri.encode(folder.path)
                                                    navController.navigate(
                                                        Screen.PlaylistDetail.createRoute(
                                                            "${PlaylistViewModel.FOLDER_PLAYLIST_PREFIX}$encodedPath"
                                                        )
                                                    )
                                                },
                                                onPlaySong = { song, queue ->
                                                    playerViewModel.showAndPlaySong(song, queue, currentFolder?.name ?: defaultFolderName)
                                                },
                                                onMoreOptionsClick = stableOnMoreOptionsClick,
                                                isPlaylistView = playerUiState.isFoldersPlaylistView,
                                                currentSortOption = playerUiState.currentFolderSortOption,
                                                isRefreshing = isRefreshing,
                                                onRefresh = onRefresh,
                                                isSelectionMode = isSelectionMode,
                                                selectedSongIds = selectedSongIds,
                                                onSongLongPress = onSongLongPress,
                                                onSongSelectionToggle = onSongSelectionToggle,
                                                getSelectionIndex = playerViewModel.multiSelectionStateHolder::getSelectionIndex,
                                                onLocateCurrentSongVisibilityChanged = { foldersShowLocateButton = it },
                                                onRegisterLocateCurrentSongAction = { foldersLocateAction = it }
                                            )
                                        } else {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(stringResource(R.string.browse_folders_permission_required))
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(onClick = {
                                                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                                    intent.data = Uri.fromParts("package", context.packageName, null)
                                                    launcher.launch(intent)
                                                }) {
                                                    Text(stringResource(R.string.grant_permission_button))
                                                }
                                            }
                                        }
                                    }

                                    null -> Unit
                                }
                            }

                            // Floating selection count pill overlay
                            SelectionCountPill(
                                selectedCount = selectedSongs.size,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .zIndex(1f)
                            )
                        }
                    }
                }
                if (playerUiState.isGeneratingAiMetadata) {
                    Surface( // Fondo semitransparente para el indicador
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                LoadingIndicator(modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.library_generating_metadata_ai),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                } else if (
                    playerUiState.isSyncingLibrary ||
                    (
                        (playerUiState.isLoadingInitialSongs || playerUiState.isLoadingLibraryCategories) &&
                            isLibraryContentEmpty
                        )
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                if (syncProgress.hasProgress && syncProgress.isRunning) {
                                    // Show progress bar with file count when we have progress info
                                    SyncProgressBar(
                                        syncProgress = syncProgress,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    // Show indeterminate loading indicator when scanning starts
                                    LoadingIndicator(modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = stringResource(R.string.syncing_library),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
            //Grad box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(170.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.2f to Color.Transparent,
                                0.8f to MaterialTheme.colorScheme.surfaceContainerLowest,
                                1.0f to MaterialTheme.colorScheme.surfaceContainerLowest
                            )
                        )
                    )
            ) {

            }
        }
    }



    PlaylistCreationTypeDialog(
        visible = showPlaylistCreationTypeDialog,
        onDismiss = { showPlaylistCreationTypeDialog = false },
        onManualSelected = {
            showPlaylistCreationTypeDialog = false
            showCreatePlaylistDialog = true
        },
        onAiSelected = {
            if (hasGeminiApiKey) {
                showPlaylistCreationTypeDialog = false
                playerViewModel.clearAiPlaylistError()
                showCreateAiPlaylistDialog = true
            } else {
                Toast.makeText(context, context.getString(R.string.gemini_api_key_required), Toast.LENGTH_SHORT).show()
            }
        },
        isAiEnabled = hasGeminiApiKey,
        onSetupAiClick = {
            navController.navigate(Screen.SettingsCategory.createRoute("ai"))
        }
    )

    val allSongsForPlaylistDialog by playerViewModel.allSongsFlow.collectAsStateWithLifecycle()
    CreatePlaylistDialog(
        visible = showCreatePlaylistDialog,
        allSongs = allSongsForPlaylistDialog,
        onDismiss = { showCreatePlaylistDialog = false },
        onGenerateClick = {
            // AI-generated playlists would be handled here
        },
        onCreate = { name, imageUri, color, icon, songIds, cropScale, cropPanX, cropPanY, shapeType, d1, d2, d3, d4 ->
            playlistViewModel.createPlaylist(
                name = name,
                coverImageUri = imageUri,
                coverColor = color,
                coverIcon = icon,
                songIds = songIds,
                cropScale = cropScale,
                cropPanX = cropPanX,
                cropPanY = cropPanY,
                isAiGenerated = false,
                isQueueGenerated = false,
                coverShapeType = shapeType,
                coverShapeDetail1 = d1,
                coverShapeDetail2 = d2,
                coverShapeDetail3 = d3,
                coverShapeDetail4 = d4
            )
        }
    )

    CreateAiPlaylistDialog(
        visible = showCreateAiPlaylistDialog && hasGeminiApiKey,
        isGenerating = isGeneratingAiPlaylist,
        error = aiError,
        onDismiss = {
            showCreateAiPlaylistDialog = false
            aiGenerationRequestedFromDialog = false
            playerViewModel.clearAiPlaylistError()
        },
        onGenerate = { playlistName, prompt, minLength, maxLength ->
            aiGenerationRequestedFromDialog = true
            playerViewModel.generateAiPlaylist(
                prompt = prompt,
                minLength = minLength,
                maxLength = maxLength,
                saveAsPlaylist = true,
                playlistName = playlistName
            )
        }
    )

    if (showSongInfoBottomSheet && selectedSongForInfo != null) {
        val currentSong = selectedSongForInfo
        val isFavorite = remember(currentSong?.id, favoriteIds) { derivedStateOf { currentSong?.let {
            favoriteIds.contains(
                it.id)
        } } }.value ?: false

        if (currentSong != null) {
            SongInfoBottomSheet(
                song = currentSong,
                isFavorite = isFavorite,
                onToggleFavorite = {
                    // Directly use PlayerViewModel's method to toggle, which should handle UserPreferencesRepository
                    playerViewModel.toggleFavoriteSpecificSong(currentSong) // Assumes such a method exists or will be added to PlayerViewModel
                },
                onDismiss = { showSongInfoBottomSheet = false },
                onPlaySong = {
                    playerViewModel.showAndPlaySong(currentSong)
                    showSongInfoBottomSheet = false
                },
                onAddToQueue = {
                    playerViewModel.addSongToQueue(currentSong) // Assumes such a method exists or will be added
                    showSongInfoBottomSheet = false
                    playerViewModel.sendToast(context.getString(R.string.playlist_detail_toast_added_to_queue))
                },
                onAddNextToQueue = {
                    playerViewModel.addSongNextToQueue(currentSong)
                    showSongInfoBottomSheet = false
                    playerViewModel.sendToast(context.getString(R.string.playlist_detail_toast_play_next))
                },
                onAddToPlayList = {
                    playlistSheetSongs = listOf(currentSong)
                    showPlaylistBottomSheet = true
                },
                onDeleteFromDevice = playerViewModel::deleteFromDevice,
                onNavigateToAlbum = {
                    navController.navigate(Screen.AlbumDetail.createRoute(currentSong.albumId))
                    showSongInfoBottomSheet = false
                },
                onNavigateToArtist = {
                    navController.navigate(Screen.ArtistDetail.createRoute(currentSong.artistId))
                    showSongInfoBottomSheet = false
                },
                onEditSong = { newTitle, newArtist, newAlbum, newGenre, newLyrics, newTrackNumber, coverArtUpdate ->
                    playerViewModel.editSongMetadata(currentSong, newTitle, newArtist, newAlbum, newGenre, newLyrics, newTrackNumber, coverArtUpdate)
                },
                generateAiMetadata = { fields ->
                    playerViewModel.generateAiMetadata(currentSong, fields)
                },
                removeFromListTrigger = {}
            )
        }
    }

    if (showPlaylistBottomSheet) {
        val playlistUiState by playlistViewModel.uiState.collectAsStateWithLifecycle()

        PlaylistBottomSheet(
            playlistUiState = playlistUiState,
            songs = playlistSheetSongs,
            onDismiss = { showPlaylistBottomSheet = false },
            bottomBarHeight = bottomBarHeightDp,
            playerViewModel = playerViewModel,
        )
    }

    // Multi-Selection Bottom Sheet
    if (showMultiSelectionSheet && selectedSongs.isNotEmpty()) {
        val activity = context as? android.app.Activity

        MultiSelectionBottomSheet(
            selectedSongs = selectedSongs,
            favoriteSongIds = favoriteIds,
            onDismiss = { showMultiSelectionSheet = false },
            onPlayAll = {
                playerViewModel.playSelectedSongs(selectedSongs)
                showMultiSelectionSheet = false
            },
            onAddToQueue = {
                playerViewModel.addSelectedToQueue(selectedSongs)
                showMultiSelectionSheet = false
            },
            onPlayNext = {
                playerViewModel.addSelectedAsNext(selectedSongs)
                showMultiSelectionSheet = false
            },
            onAddToPlaylist = {
                playlistSheetSongs = selectedSongs
                showMultiSelectionSheet = false
                showPlaylistBottomSheet = true
            },
            onToggleLikeAll = { shouldLike ->
                if (shouldLike) {
                    playerViewModel.likeSelectedSongs(selectedSongs)
                } else {
                    playerViewModel.unlikeSelectedSongs(selectedSongs)
                }
                showMultiSelectionSheet = false
            },
            onShareAll = {
                playerViewModel.shareSelectedAsZip(selectedSongs)
                showMultiSelectionSheet = false
            },
            onDeleteAll = { _, onComplete ->
                activity?.let {
                    playerViewModel.deleteSelectedFromDevice(it, selectedSongs) {
                        showMultiSelectionSheet = false
                        onComplete(true)
                    }
                }
            }
        )
    }

    // Playlist Multi-Selection Bottom Sheet
    if (showPlaylistMultiSelectionSheet && selectedPlaylists.isNotEmpty()) {
        val activity = context as? android.app.Activity

        PlaylistMultiSelectionBottomSheet(
            selectedPlaylists = selectedPlaylists,
            onDismiss = {
                showPlaylistMultiSelectionSheet = false
                playlistMultiSelectionState.clearSelection()
            },
            onDeleteAll = {
                playlistViewModel.deletePlaylistsInBatch(selectedPlaylistIds.toList())
                showPlaylistMultiSelectionSheet = false
                playlistMultiSelectionState.clearSelection()
            },
            onExportAll = {
                playlistViewModel.exportPlaylistsAsM3u(selectedPlaylistIds.toList())
                showPlaylistMultiSelectionSheet = false
                playlistMultiSelectionState.clearSelection()
            },
            onMergeAll = {
                pendingMergePlaylistIds = selectedPlaylistIds.toList()
                showMergePlaylistDialog = true
                showPlaylistMultiSelectionSheet = false
            },
            onShareAll = {
                activity?.let {
                    playlistViewModel.shareSelectedPlaylistsAsZip(selectedPlaylistIds.toList(), it)
                }
                showPlaylistMultiSelectionSheet = false
                playlistMultiSelectionState.clearSelection()
            }
        )
    }

    if (showTabSwitcherSheet) {
        LibraryTabSwitcherSheet(
            tabs = tabTitles,
            currentIndex = currentTabIndex,
            onTabSelected = { index ->
                scope.launch {
                    pagerState.animateScrollToPage(
                        targetPageForTabIndex(
                            currentPage = pagerState.currentPage,
                            targetTabIndex = index,
                            tabCount = tabTitles.size,
                            compactMode = isCompactNavigation
                        )
                    )
                }
                showTabSwitcherSheet = false
            },
            onEditClick = {
                showTabSwitcherSheet = false
                showReorderTabsSheet = true
            },
            onDismiss = { showTabSwitcherSheet = false }
        )
    }

    if (showReorderTabsSheet) {
        // Create a mapping of tab IDs to localized display names
        val tabDisplayNames = mutableMapOf<String, String>()
        tabTitles.forEach { tabId ->
            tabDisplayNames[tabId] = when (tabId) {
                "SONGS" -> stringResource(R.string.library_tab_songs)
                "ALBUMS" -> stringResource(R.string.library_tab_albums)
                "ARTIST" -> stringResource(R.string.library_tab_artists)
                "PLAYLISTS" -> stringResource(R.string.library_tab_playlists)
                "FOLDERS" -> stringResource(R.string.library_tab_folders)
                "LIKED" -> stringResource(R.string.library_tab_liked)
                else -> tabId
            }
        }
        
        ReorderTabsSheet(
            tabs = tabTitles,
            tabDisplayNames = tabDisplayNames,
            onReorder = { newOrder ->
                playerViewModel.saveLibraryTabsOrder(newOrder)
            },
            onReset = {
                playerViewModel.resetLibraryTabsOrder()
            },
            onDismiss = { showReorderTabsSheet = false }
        )
    }

    // Merge Playlists Dialog
    if (showMergePlaylistDialog && pendingMergePlaylistIds.isNotEmpty()) {
        var mergePlaylistName by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { 
                showMergePlaylistDialog = false
                pendingMergePlaylistIds = emptyList()
                mergePlaylistName = ""
            },
            title = { Text(stringResource(R.string.merge_playlists_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.merge_playlists_enter_name))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = mergePlaylistName,
                        onValueChange = { mergePlaylistName = it },
                        placeholder = { Text(stringResource(R.string.merge_playlists_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.merge_playlists_description, pendingMergePlaylistIds.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (mergePlaylistName.isNotEmpty()) {
                            playlistViewModel.mergePlaylistsIntoOne(
                                pendingMergePlaylistIds,
                                mergePlaylistName
                            )
                            playlistMultiSelectionState.clearSelection()
                            showMergePlaylistDialog = false
                            pendingMergePlaylistIds = emptyList()
                            mergePlaylistName = ""
                        }
                    }
                ) {
                    Text(stringResource(R.string.common_merge))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showMergePlaylistDialog = false
                    pendingMergePlaylistIds = emptyList()
                    mergePlaylistName = ""
                }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun CompactLibraryPagerIndicator(
    currentIndex: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    if (pageCount <= 1) return

    val safeIndex = positiveMod(currentIndex, pageCount)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val selected = index == safeIndex
            val width by animateDpAsState(
                targetValue = if (selected) 22.dp else 10.dp,
                label = "LibraryCompactPagerIndicatorWidth"
            )
            val alpha by animateFloatAsState(
                targetValue = if (selected) 1f else 0.35f,
                label = "LibraryCompactPagerIndicatorAlpha"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(4.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LibraryNavigationPill(
    title: String,
    isExpanded: Boolean,
    iconRes: Int,
    pageIndex: Int,
    onClick: () -> Unit,
    onArrowClick: () -> Unit
) {
    data class PillState(val pageIndex: Int, val iconRes: Int, val title: String)

    val pillRadius = 26.dp
    val innerRadius = 4.dp
    // Radio para cuando está expandido/seleccionado (totalmente redondo)
    val expandedRadius = 60.dp

    // Animación Esquina Flecha (Interna):
    // Depende de 'isExpanded':
    // - true: Se vuelve redonda (expandedRadius/pillRadius) separándose visualmente.
    // - false: Se mantiene recta (innerRadius) pareciendo unida al título.
    val animatedArrowCorner by animateDpAsState(
        targetValue = if (isExpanded) pillRadius else innerRadius,
        label = "ArrowCornerAnimation"
    )

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ArrowRotation"
    )

    // IntrinsicSize.Min en el Row + fillMaxHeight en los hijos asegura misma altura
    Row(
        modifier = Modifier
            .padding(start = 4.dp)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = pillRadius,
                bottomStart = pillRadius,
                topEnd = innerRadius,
                bottomEnd = innerRadius
            ),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .fillMaxHeight()
                .clip(
                    RoundedCornerShape(
                        topStart = pillRadius,
                        bottomStart = pillRadius,
                        topEnd = innerRadius,
                        bottomEnd = innerRadius
                    )
                )
                .clickable(onClick = onClick)
        ) {
            Box(
                modifier = Modifier.padding(start = 18.dp, end = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                AnimatedContent(
                    targetState = PillState(pageIndex = pageIndex, iconRes = iconRes, title = title),
                    transitionSpec = {
                        val direction = targetState.pageIndex.compareTo(initialState.pageIndex).coerceIn(-1, 1)
                        val slideIn = slideInHorizontally { fullWidth -> if (direction >= 0) fullWidth else -fullWidth } + fadeIn()
                        val slideOut = slideOutHorizontally { fullWidth -> if (direction >= 0) -fullWidth else fullWidth } + fadeOut()
                        slideIn.togetherWith(slideOut)
                    },
                    label = "LibraryPillTitle"
                ) { targetState ->
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = targetState.iconRes),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = targetState.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // --- PARTE 2: FLECHA (Cambia de forma según estado) ---
        Surface(
            shape = RoundedCornerShape(
                topStart = animatedArrowCorner, // Anima entre 4.dp y 26.dp
                bottomStart = animatedArrowCorner, // Anima entre 4.dp y 26.dp
                topEnd = pillRadius,
                bottomEnd = pillRadius
            ),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .fillMaxHeight()
                .clip(
                    RoundedCornerShape(
                        topStart = animatedArrowCorner, // Anima entre 4.dp y 26.dp
                        bottomStart = animatedArrowCorner, // Anima entre 4.dp y 26.dp
                        topEnd = pillRadius,
                        bottomEnd = pillRadius
                    )
                )
                .clickable(
                    indication = ripple(),
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onArrowClick
                )
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .width(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.rotate(arrowRotation),
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Expandir menú",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryTabSwitcherSheet(
    tabs: List<String>,
    currentIndex: Int,
    onTabSelected: (Int) -> Unit,
    onEditClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.library_tabs_title),
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = GoogleSansRounded
            )
            Text(
                text = stringResource(R.string.library_tabs_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
            ) {
                itemsIndexed(
                    items = tabs,
                    key = { index, tab -> "$tab-$index" },
                    contentType = { _, _ -> "library_tab_item" }
                ) { index, rawId ->
                    val tabId = rawId.toLibraryTabIdOrNull() ?: return@itemsIndexed
                    LibraryTabGridItem(
                        tabId = tabId,
                        isSelected = index == currentIndex,
                        onClick = { onTabSelected(index) }
                    )
                }

                item(
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "reorder_tabs_action"
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 46.dp, max = 60.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onEditClick,
                            shape = CircleShape,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            modifier = Modifier
                                .fillMaxHeight()
                                .align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(stringResource(R.string.library_reorder_tabs))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryTabGridItem(
    tabId: LibraryTabId,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    val iconContainer = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = containerColor,
        tonalElevation = if (isSelected) 6.dp else 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(iconContainer.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = tabId.iconRes()),
                    contentDescription = stringResource(tabId.titleResId),
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Text(
                text = tabId.displayTitle(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

private fun positiveMod(value: Int, mod: Int): Int {
    if (mod <= 0) return 0
    return ((value % mod) + mod) % mod
}

private fun LibraryTabId.iconRes(): Int = when (this) {
    LibraryTabId.SONGS -> R.drawable.rounded_music_note_24
    LibraryTabId.ALBUMS -> R.drawable.rounded_album_24
    LibraryTabId.ARTISTS -> R.drawable.rounded_artist_24
    LibraryTabId.PLAYLISTS -> R.drawable.rounded_playlist_play_24
    LibraryTabId.FOLDERS -> R.drawable.rounded_folder_24
    LibraryTabId.LIKED -> R.drawable.rounded_favorite_24
}

@Composable
private fun LibraryTabId.displayTitle(): String = stringResource(titleResId)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LibraryFoldersTab(
    folders: ImmutableList<MusicFolder>,
    currentFolder: MusicFolder?,
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onFolderClick: (String) -> Unit,
    onFolderAsPlaylistClick: (MusicFolder) -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    stablePlayerState: StablePlayerState,
    bottomBarHeight: Dp,
    onMoreOptionsClick: (Song) -> Unit,
    isPlaylistView: Boolean = false,
    currentSortOption: SortOption = SortOption.FolderNameAZ,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    isSelectionMode: Boolean = false,
    selectedSongIds: Set<String> = emptySet(),
    onSongLongPress: (Song) -> Unit = {},
    onSongSelectionToggle: (Song) -> Unit = {},
    getSelectionIndex: (String) -> Int? = { null },
    onLocateCurrentSongVisibilityChanged: (Boolean) -> Unit = {},
    onRegisterLocateCurrentSongAction: ((() -> Unit)?) -> Unit = {}
) {
    // List state moved inside AnimatedContent to prevent state sharing issues during transitions


    AnimatedContent(
        targetState = Pair(isPlaylistView, currentFolder?.path ?: "root"),
        label = "FolderNavigation",
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            (slideInHorizontally { width -> width } + fadeIn())
                .togetherWith(slideOutHorizontally { width -> -width } + fadeOut())
        }
    ) { (playlistMode, targetPath) ->
        // Each navigation destination gets its own independant ListState
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        val visibilityCallback by rememberUpdatedState(onLocateCurrentSongVisibilityChanged)
        val registerActionCallback by rememberUpdatedState(onRegisterLocateCurrentSongAction)

        val flattenedFolders = remember(folders, currentSortOption) {
            sortMusicFoldersByOption(flattenFolders(folders), currentSortOption)
        }

        val isRoot = targetPath == "root"
        val activeFolder = if (isRoot) null else currentFolder
        val showPlaylistCards = playlistMode && activeFolder == null
        val itemsToShow = remember(activeFolder, folders, flattenedFolders, currentSortOption) {
            when {
                showPlaylistCards -> flattenedFolders
                activeFolder != null -> sortMusicFoldersByOption(activeFolder.subFolders, currentSortOption)
                else -> sortMusicFoldersByOption(folders, currentSortOption)
            }
        }.toImmutableList()

        val songsToShow = remember(activeFolder, currentSortOption) {
            sortSongsForFolderView(activeFolder?.songs ?: emptyList(), currentSortOption)
        }.toImmutableList()
        val currentSongId = stablePlayerState.currentSong?.id
        val currentSongIndexInSongs = remember(songsToShow, currentSongId) {
            currentSongId?.let { songId -> songsToShow.indexOfFirst { it.id == songId } } ?: -1
        }
        val currentSongListIndex = remember(itemsToShow.size, currentSongIndexInSongs) {
            if (currentSongIndexInSongs < 0) -1 else itemsToShow.size + currentSongIndexInSongs
        }
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

        LaunchedEffect(currentSongListIndex, itemsToShow, songsToShow, listState) {
            if (currentSongListIndex < 0 || songsToShow.isEmpty()) {
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

        val shouldShowLoading = isLoading && itemsToShow.isEmpty() && songsToShow.isEmpty() && isRoot

        Column(modifier = Modifier.fillMaxSize()) {
            when {
                shouldShowLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                }

                itemsToShow.isEmpty() && songsToShow.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_folder),
                                contentDescription = null,
                                Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                stringResource(R.string.library_empty_no_folders),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    val foldersPullToRefreshState = rememberPullToRefreshState()
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        state = foldersPullToRefreshState,
                        modifier = Modifier.fillMaxSize(),
                        indicator = {
                            PullToRefreshDefaults.LoadingIndicator(
                                state = foldersPullToRefreshState,
                                isRefreshing = isRefreshing,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier
                                    .padding(start = 12.dp, end = if (listState.canScrollForward || listState.canScrollBackward) 22.dp else 12.dp)
                                    .fillMaxSize()
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
                                contentPadding = PaddingValues(
                                    bottom = bottomBarHeight + MiniPlayerHeight + ListExtraBottomGap,
                                    top = 0.dp                            )
                            ) {
                                if (showPlaylistCards) {
                                    items(itemsToShow, key = { it.path }) { folder ->
                                        FolderPlaylistItem(
                                            folder = folder,
                                            onClick = { onFolderAsPlaylistClick(folder) }
                                        )
                                    }
                                } else {
                                    items(itemsToShow, key = { it.path }) { folder ->
                                        FolderListItem(
                                            folder = folder,
                                            onClick = { onFolderClick(folder.path) }
                                        )
                                    }
                                }

                                items(songsToShow, key = { it.id }) { song ->
                                    EnhancedSongListItem(
                                        song = song,
                                        isPlaying = stablePlayerState.currentSong?.id == song.id && stablePlayerState.isPlaying,
                                        isCurrentSong = stablePlayerState.currentSong?.id == song.id,
                                        onMoreOptionsClick = { onMoreOptionsClick(song) },
                                        isSelected = selectedSongIds.contains(song.id),
                                        selectionIndex = if (isSelectionMode) getSelectionIndex(song.id) else null,
                                        isSelectionMode = isSelectionMode,
                                        onLongPress = { onSongLongPress(song) },
                                        onClick = {
                                            if (isSelectionMode) {
                                                onSongSelectionToggle(song)
                                            } else {
                                                val songIndex = songsToShow.indexOf(song)
                                                if (songIndex != -1) {
                                                    val songsToPlay =
                                                        songsToShow.subList(songIndex, songsToShow.size)
                                                            .toList()
                                                    onPlaySong(song, songsToPlay)
                                                }
                                            }
                                        }
                                    )
                                }
                            }

                            // ScrollBar Overlay
                            val bottomPadding = if (stablePlayerState.currentSong != null && stablePlayerState.currentSong != Song.emptySong())
                                bottomBarHeight + MiniPlayerHeight + 16.dp
                            else
                                bottomBarHeight + 16.dp

                            ExpressiveScrollBar(
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
}

@Composable
fun FolderPlaylistItem(folder: MusicFolder, onClick: () -> Unit) {
    val previewSongs = remember(folder) { folder.collectAllSongs().take(9) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaylistArtCollage(
                songs = previewSongs,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    folder.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = GoogleSansRounded),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${folder.totalSongCount} Songs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FolderListItem(folder: MusicFolder, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_folder),
                contentDescription = "Folder",
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(folder.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${folder.totalSongCount} Songs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun flattenFolders(folders: List<MusicFolder>): List<MusicFolder> {
    return folders.flatMap { folder ->
        val current = if (folder.songs.isNotEmpty()) listOf(folder) else emptyList()
        current + flattenFolders(folder.subFolders)
    }
}

private fun sortMusicFoldersByOption(folders: List<MusicFolder>, sortOption: SortOption): List<MusicFolder> {
    return when (sortOption) {
        SortOption.FolderNameAZ -> folders.sortedBy { it.name.lowercase() }
        SortOption.FolderNameZA -> folders.sortedByDescending { it.name.lowercase() }
        SortOption.FolderSongCountAsc -> folders.sortedWith(
            compareBy<MusicFolder> { it.totalSongCount }.thenBy { it.name.lowercase() }
        )
        SortOption.FolderSongCountDesc -> folders.sortedWith(
            compareByDescending<MusicFolder> { it.totalSongCount }.thenBy { it.name.lowercase() }
        )
        SortOption.FolderSubdirCountAsc -> folders.sortedWith(
            compareBy<MusicFolder> { it.totalSubFolderCount }.thenBy { it.name.lowercase() }
        )
        SortOption.FolderSubdirCountDesc -> folders.sortedWith(
            compareByDescending<MusicFolder> { it.totalSubFolderCount }.thenBy { it.name.lowercase() }
        )
        else -> folders.sortedBy { it.name.lowercase() }
    }
}

private fun sortSongsForFolderView(songs: List<Song>, sortOption: SortOption): List<Song> {
    return when (sortOption) {
        SortOption.FolderNameZA -> songs.sortedByDescending { it.title.lowercase() }
        else -> songs.sortedBy { it.title.lowercase() }
    }
}

private fun MusicFolder.collectAllSongs(): List<Song> {
    return songs + subFolders.flatMap { it.collectAllSongs() }
}

// NUEVA Pestaña para Favoritos
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun LibraryFavoritesTab(
    favoriteSongs: androidx.paging.compose.LazyPagingItems<Song>,
    playerViewModel: PlayerViewModel,
    bottomBarHeight: Dp,
    onMoreOptionsClick: (Song) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    isSelectionMode: Boolean = false,
    selectedSongIds: Set<String> = emptySet(),
    onSongLongPress: (Song) -> Unit = {},
    onSongSelectionToggle: (Song) -> Unit = {},
    getSelectionIndex: (String) -> Int? = { null },
    onLocateCurrentSongVisibilityChanged: (Boolean) -> Unit = {},
    onRegisterLocateCurrentSongAction: ((() -> Unit)?) -> Unit = {}
) {
    val stablePlayerState by playerViewModel.stablePlayerState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val visibilityCallback by rememberUpdatedState(onLocateCurrentSongVisibilityChanged)
    val registerActionCallback by rememberUpdatedState(onRegisterLocateCurrentSongAction)
    val currentSongId = stablePlayerState.currentSong?.id

    // For paging items, find current song index in loaded snapshot
    val currentSongListIndex = remember(favoriteSongs.itemCount, currentSongId) {
        if (currentSongId == null) -1
        else {
            val items = favoriteSongs.itemSnapshotList
            items.indexOfFirst { it?.id == currentSongId }
        }
    }
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

    LaunchedEffect(currentSongListIndex, favoriteSongs.itemCount, listState) {
        if (currentSongListIndex < 0 || favoriteSongs.itemCount == 0) {
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

    if (favoriteSongs.itemCount == 0 && favoriteSongs.loadState.refresh !is androidx.paging.LoadState.Loading) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.FavoriteBorder, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.library_empty_no_liked), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.library_empty_no_liked_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    } else {
        Box(modifier = Modifier
            .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val songsPullToRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = songsPullToRefreshState,
                modifier = Modifier.fillMaxSize(),
                indicator = {
                    PullToRefreshDefaults.LoadingIndicator(
                        state = songsPullToRefreshState,
                        isRefreshing = isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
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
                        items(
                            count = favoriteSongs.itemCount,
                            key = { index -> favoriteSongs.peek(index)?.id ?: index },
                            contentType = { "song" }
                        ) { index ->
                            val song = favoriteSongs[index]
                            if (song != null) {
                                val isPlayingThisSong =
                                    song.id == stablePlayerState.currentSong?.id && stablePlayerState.isPlaying
                                EnhancedSongListItem(
                                    song = song,
                                    isCurrentSong = stablePlayerState.currentSong?.id == song.id,
                                    isPlaying = isPlayingThisSong,
                                    onMoreOptionsClick = { onMoreOptionsClick(song) },
                                    isSelected = selectedSongIds.contains(song.id),
                                    selectionIndex = if (isSelectionMode) getSelectionIndex(song.id) else null,
                                    isSelectionMode = isSelectionMode,
                                    onLongPress = { onSongLongPress(song) },
                                    onClick = {
                                        if (isSelectionMode) {
                                            onSongSelectionToggle(song)
                                        } else {
                                            playerViewModel.showAndPlaySong(
                                                song,
                                                favoriteSongs.itemSnapshotList.items,
                                                "Liked Songs"
                                            )
                                        }
                                    }
                                )
                            } else {
                                // Placeholder while loading
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

                    ExpressiveScrollBar(
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


/**
 * Paginated version of LibrarySongsTab using Paging 3 for efficient memory usage.
 * Displays songs in pages, loading only what's needed for the current viewport.
 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun LibrarySongsTabPaginated(
    paginatedSongs: androidx.paging.compose.LazyPagingItems<Song>,
    stablePlayerState: StablePlayerState,
    playerViewModel: PlayerViewModel,
    bottomBarHeight: Dp,
    onMoreOptionsClick: (Song) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()

    // Handle different loading states
    when {
        paginatedSongs.loadState.refresh is LoadState.Loading && paginatedSongs.itemCount == 0 -> {
            // Initial loading - show skeleton placeholders
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
                    )
                    .fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = bottomBarHeight + MiniPlayerHeight + ListExtraBottomGap)
            ) {
                items(12, key = { "skeleton_song_$it" }) { // Show 12 skeleton items to fill the screen
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
        paginatedSongs.loadState.refresh is LoadState.Error && paginatedSongs.itemCount == 0 -> {
            // Error state
            val error = (paginatedSongs.loadState.refresh as LoadState.Error).error
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
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
                    Button(onClick = { paginatedSongs.retry() }) {
                        Text("Retry")
                    }
                }
            }
        }
        paginatedSongs.itemCount == 0 && paginatedSongs.loadState.refresh is LoadState.NotLoading -> {
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
            // Songs loaded - show paginated list
            Box(modifier = Modifier.fillMaxSize()) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        onRefresh()
                        paginatedSongs.refresh()
                    },
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
                            item(
                                key = "songs_top_spacer",
                                contentType = "songs_top_spacer"
                            ) { Spacer(Modifier.height(0.dp)) }

                            items(
                                count = paginatedSongs.itemCount,
                                key = { index -> paginatedSongs.peek(index)?.id ?: "paged_song_$index" },
                                contentType = paginatedSongs.itemContentType { "song" }
                            ) { index ->
                                val song = paginatedSongs[index]
                                if (song != null) {
                                    val isPlayingThisSong = song.id == stablePlayerState.currentSong?.id && stablePlayerState.isPlaying

                                    val rememberedOnMoreOptionsClick: (Song) -> Unit = remember(onMoreOptionsClick) {
                                        { songFromListItem -> onMoreOptionsClick(songFromListItem) }
                                    }
                                    val rememberedOnClick: () -> Unit = remember(song) {
                                        { playerViewModel.showAndPlaySong(song) }
                                    }

                                    EnhancedSongListItem(
                                        song = song,
                                        isPlaying = isPlayingThisSong,
                                        isCurrentSong = stablePlayerState.currentSong?.id == song.id,
                                        isLoading = false,
                                        onMoreOptionsClick = rememberedOnMoreOptionsClick,
                                        onClick = rememberedOnClick
                                    )
                                } else {
                                    // Placeholder while loading
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

                            // Loading indicator for appending more items
                            if (paginatedSongs.loadState.append is LoadState.Loading) {
                                item(contentType = "songs_append_loading") {
                                    Box(
                                        Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        LoadingIndicator(modifier = Modifier.size(32.dp))
                                    }
                                }
                            }
                        }

                        // ScrollBar Overlay
                        val bottomPadding = if (stablePlayerState.currentSong != null && stablePlayerState.currentSong != Song.emptySong())
                            bottomBarHeight + MiniPlayerHeight + 16.dp
                        else
                            bottomBarHeight + 16.dp

                        ExpressiveScrollBar(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp, top = 16.dp, bottom = bottomPadding),
                            listState = listState
                        )
                    }
                }
                // Top gradient fade effect
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface, Color.Transparent
                                )
                            )
                        )
                )
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun LibraryAlbumsTab(
    albums: ImmutableList<Album>,
    isLoading: Boolean,
    playerViewModel: PlayerViewModel,
    bottomBarHeight: Dp,
    onAlbumClick: (Long) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState() // New state for list view
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    // Collect view mode preference
    val playerUiState by playerViewModel.playerUiState.collectAsStateWithLifecycle()
    val isListView = playerUiState.isAlbumsListView

    // Scroll to top when sort option changes
    LaunchedEffect(playerUiState.currentAlbumSortOption) {
        if (isListView) {
            listState.scrollToItem(0)
        } else {
            gridState.scrollToItem(0)
        }
    }

    // Prefetching logic for LibraryAlbumsTab
    LaunchedEffect(albums, gridState, listState, isListView) {
        if (isListView) {
            // Prefetch logic for List View
            snapshotFlow { listState.layoutInfo }
                .distinctUntilChanged()
                .collect { layoutInfo ->
                    val visibleItemsInfo = layoutInfo.visibleItemsInfo
                    if (visibleItemsInfo.isNotEmpty() && albums.isNotEmpty()) {
                        val lastVisibleItemIndex = visibleItemsInfo.last().index
                        val totalItemsCount = albums.size
                        val prefetchThreshold = 5
                        val prefetchCount = 10

                        if (totalItemsCount > lastVisibleItemIndex + 1 && lastVisibleItemIndex + prefetchThreshold >= totalItemsCount - prefetchCount) {
                            val startIndexToPrefetch = lastVisibleItemIndex + 1
                            val endIndexToPrefetch = (startIndexToPrefetch + prefetchCount).coerceAtMost(totalItemsCount)

                            (startIndexToPrefetch until endIndexToPrefetch).forEach { indexToPrefetch ->
                                val album = albums.getOrNull(indexToPrefetch)
                                album?.albumArtUriString?.let { uri ->
                                    val request = ImageRequest.Builder(context)
                                        .data(uri)
                                        .size(Size(256, 256))
                                        .build()
                                    imageLoader.enqueue(request)
                                }
                            }
                        }
                    }
                }
        } else {
            // Prefetch logic for Grid View
            snapshotFlow { gridState.layoutInfo }
                .distinctUntilChanged()
                .collect { layoutInfo ->
                    val visibleItemsInfo = layoutInfo.visibleItemsInfo
                    if (visibleItemsInfo.isNotEmpty() && albums.isNotEmpty()) {
                        val lastVisibleItemIndex = visibleItemsInfo.last().index
                        val totalItemsCount = albums.size
                        val prefetchThreshold = 5
                        val prefetchCount = 10

                        if (totalItemsCount > lastVisibleItemIndex + 1 && lastVisibleItemIndex + prefetchThreshold >= totalItemsCount - prefetchCount) {
                            val startIndexToPrefetch = lastVisibleItemIndex + 1
                            val endIndexToPrefetch = (startIndexToPrefetch + prefetchCount).coerceAtMost(totalItemsCount)

                            (startIndexToPrefetch until endIndexToPrefetch).forEach { indexToPrefetch ->
                                val album = albums.getOrNull(indexToPrefetch)
                                album?.albumArtUriString?.let { uri ->
                                    val request = ImageRequest.Builder(context)
                                        .data(uri)
                                        .size(Size(256, 256))
                                        .build()
                                    imageLoader.enqueue(request)
                                }
                            }
                        }
                    }
                }
        }
    }

    if (isLoading && albums.isEmpty()) {
        // Show skeleton grid during loading
        if (isListView) {
            LazyColumn(
                modifier = Modifier
                    .padding(start = 14.dp, end = 14.dp, bottom = 6.dp)
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
                contentPadding = PaddingValues(bottom = bottomBarHeight + MiniPlayerHeight + ListExtraBottomGap + 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(8, key = { "skeleton_album_list_$it" }) {
                    AlbumListItem(
                        album = Album.empty(),
                        albumColorSchemePairFlow = MutableStateFlow(null),
                        onClick = {},
                        isLoading = true
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(start = 14.dp, end = 14.dp, bottom = 6.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 26.dp,
                            topEnd = 26.dp,
                            bottomStart = PlayerSheetCollapsedCornerRadius,
                            bottomEnd = PlayerSheetCollapsedCornerRadius
                        )
                    )
                    .fillMaxSize(),
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = bottomBarHeight + MiniPlayerHeight + ListExtraBottomGap + 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(8, key = { "skeleton_album_grid_$it" }) { // Show 8 skeleton items (4 rows x 2 columns)
                    AlbumGridItemRedesigned(
                        album = Album.empty(),
                        albumColorSchemePairFlow = MutableStateFlow(null),
                        onClick = {},
                        isLoading = true
                    )
                }
            }
        }
    } else if (albums.isEmpty() && !isLoading) { // canLoadMore removed
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Album, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text(stringResource(R.string.library_empty_no_albums), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        // Songs loaded
        Box(modifier = Modifier.fillMaxSize()) {
            val albumsPullToRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = albumsPullToRefreshState,
                modifier = Modifier.fillMaxSize(),
                indicator = {
                    PullToRefreshDefaults.LoadingIndicator(
                        state = albumsPullToRefreshState,
                        isRefreshing = isRefreshing,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isListView) {

                        LazyColumn(
                            modifier = Modifier
                                .padding(start = 14.dp, end = if (listState.canScrollForward || listState.canScrollBackward) 24.dp else 14.dp, bottom = 6.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 26.dp,
                                        topEnd = 26.dp,
                                        bottomStart = PlayerSheetCollapsedCornerRadius,
                                        bottomEnd = PlayerSheetCollapsedCornerRadius
                                    )
                                ),
                            state = listState,
                            contentPadding = PaddingValues(bottom = bottomBarHeight + MiniPlayerHeight + ListExtraBottomGap + 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(albums, key = { it.id }) { album ->
                                val albumSpecificColorSchemeFlow =
                                    playerViewModel.themeStateHolder.getAlbumColorSchemeFlow(album.albumArtUriString ?: "")
                                val rememberedOnClick = remember(album.id) { { onAlbumClick(album.id) } }
                                AlbumListItem(
                                    album = album,
                                    albumColorSchemePairFlow = albumSpecificColorSchemeFlow,
                                    onClick = rememberedOnClick,
                                    isLoading = isLoading && albums.isEmpty()
                                )
                            }
                        }
                        // ScrollBar Overlay for List
                        val stablePlayerState by playerViewModel.stablePlayerState.collectAsStateWithLifecycle()
                        val bottomPadding = if (stablePlayerState.currentSong != null && stablePlayerState.currentSong != Song.emptySong())
                            bottomBarHeight + MiniPlayerHeight + 16.dp
                        else
                            bottomBarHeight + 16.dp

                        ExpressiveScrollBar(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp, top = 16.dp, bottom = bottomPadding),
                            listState = listState
                        )
                    } else {
                        LazyVerticalGrid(
                            modifier = Modifier
                                .padding(start = 14.dp, end = if (gridState.canScrollForward || gridState.canScrollBackward) 24.dp else 14.dp, bottom = 6.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 26.dp,
                                        topEnd = 26.dp,
                                        bottomStart = PlayerSheetCollapsedCornerRadius,
                                        bottomEnd = PlayerSheetCollapsedCornerRadius
                                    )
                                ),
                            state = gridState,
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(bottom = bottomBarHeight + MiniPlayerHeight + ListExtraBottomGap + 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            items(albums, key = { it.id }) { album ->
                                val albumSpecificColorSchemeFlow =
                                    playerViewModel.themeStateHolder.getAlbumColorSchemeFlow(album.albumArtUriString ?: "")
                                val rememberedOnClick = remember(album.id) { { onAlbumClick(album.id) } }
                                AlbumGridItemRedesigned(
                                    album = album,
                                    albumColorSchemePairFlow = albumSpecificColorSchemeFlow,
                                    onClick = rememberedOnClick,
                                    isLoading = isLoading && albums.isEmpty() // Shimmer solo si está cargando Y la lista está vacía
                                )
                            }
                        }

                        // ScrollBar Overlay for Grid
                        val stablePlayerState by playerViewModel.stablePlayerState.collectAsStateWithLifecycle()
                        val bottomPadding = if (stablePlayerState.currentSong != null && stablePlayerState.currentSong != Song.emptySong())
                            bottomBarHeight + MiniPlayerHeight + 16.dp
                        else
                            bottomBarHeight + 16.dp

                        ExpressiveScrollBar(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp, top = 16.dp, bottom = bottomPadding),
                            gridState = gridState
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumGridItemRedesigned(
    album: Album,
    albumColorSchemePairFlow: StateFlow<ColorSchemePair?>,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    val albumColorSchemePair by albumColorSchemePairFlow.collectAsStateWithLifecycle()
    val systemIsDark = LocalPixelPlayDarkTheme.current

    // 1. Obtén el colorScheme del tema actual aquí, en el scope Composable.
    val currentMaterialColorScheme = MaterialTheme.colorScheme

    val itemDesignColorScheme = remember(albumColorSchemePair, systemIsDark, currentMaterialColorScheme) {
        // 2. Ahora, currentMaterialColorScheme es una variable estable que puedes usar.
        albumColorSchemePair?.let { pair ->
            if (systemIsDark) pair.dark else pair.light
        } ?: currentMaterialColorScheme // Usa la variable capturada
    }

    val gradientBaseColor = itemDesignColorScheme.primaryContainer
    val onGradientColor = itemDesignColorScheme.onPrimaryContainer
    val cardCornerRadius = 20.dp

    if (isLoading) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(cardCornerRadius)
                )
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .aspectRatio(3f / 2f)
                        .fillMaxSize()
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    } else {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(cardCornerRadius),
            //elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = itemDesignColorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.background(
                    color = gradientBaseColor,
                    shape = RoundedCornerShape(cardCornerRadius)
                )
            ) {
                Box(contentAlignment = Alignment.BottomStart) {
                    var isLoadingImage by remember { mutableStateOf(true) }
                    SmartImage(
                        model = album.albumArtUriString,
                        contentDescription = "Carátula de ${album.title}",
                        contentScale = ContentScale.Crop,
                        // Reducido el tamaño para mejorar el rendimiento del scroll, como se sugiere en el informe.
                        // ContentScale.Crop se encargará de ajustar la imagen al aspect ratio.
                        targetSize = Size(256, 256),
                        modifier = Modifier
                            .aspectRatio(3f / 2f)
                            .fillMaxSize(),
                        onState = { state ->
                            isLoadingImage = state is AsyncImagePainter.State.Loading
                        }
                    )
                    if (isLoadingImage) {
                        ShimmerBox(
                            modifier = Modifier
                                .aspectRatio(3f / 2f)
                                .fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(3f / 2f)
                            .background(
                                remember(gradientBaseColor) { // Recordar el Brush
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent, gradientBaseColor
                                        )
                                    )
                                })
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        album.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = onGradientColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(album.artist, style = MaterialTheme.typography.bodySmall, color = onGradientColor.copy(alpha = 0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${album.songCount} Songs", style = MaterialTheme.typography.bodySmall, color = onGradientColor.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun LibraryArtistsTab(
    artists: ImmutableList<Artist>,
    isLoading: Boolean, // This now represents the loading state for all artists
    // canLoadMore: Boolean, // Removed
    playerViewModel: PlayerViewModel,
    bottomBarHeight: Dp,
    onArtistClick: (Long) -> Unit,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val listState = rememberLazyListState()
    val playerUiState by playerViewModel.playerUiState.collectAsStateWithLifecycle()

    // Scroll to top when sort option changes
    LaunchedEffect(playerUiState.currentArtistSortOption) {
        listState.scrollToItem(0)
    }

    if (isLoading && artists.isEmpty()) {
        // Show skeleton list during loading
        LazyColumn(
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
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
            item(key = "skeleton_top_spacer") { Spacer(Modifier.height(4.dp)) }
            items(10, key = { "skeleton_artist_$it" }) { // Show 10 skeleton items
                ArtistListItem(
                    artist = Artist.empty(),
                    onClick = {},
                    isLoading = true
                )
            }
        }
    }
    else if (artists.isEmpty() && !isLoading) { /* ... No artists ... */ } // canLoadMore removed
    else {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val genresPullToRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = genresPullToRefreshState,
                modifier = Modifier.fillMaxSize(),
                indicator = {
                    PullToRefreshDefaults.LoadingIndicator(
                        state = genresPullToRefreshState,
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
                        contentPadding = PaddingValues(bottom = bottomBarHeight + MiniPlayerHeight + ListExtraBottomGap)
                    ) {
//                        item(key = "artists_top_spacer") {
//                            Spacer(Modifier.height(4.dp))
//                        }
                        items(artists, key = { it.id }) { artist ->
                            val rememberedOnClick = remember(artist) { { onArtistClick(artist.id) } }
                            ArtistListItem(artist = artist, onClick = rememberedOnClick)
                        }
                        // "Load more" indicator removed as all artists are loaded at once
                        // if (isLoading && artists.isNotEmpty()) {
                        //     item { Box(Modifier
                        //         .fillMaxWidth()
                        //         .padding(16.dp), Alignment.Center) { CircularProgressIndicator() } }
                        // }
                    }

                    // ScrollBar Overlay
                    val stablePlayerState by playerViewModel.stablePlayerState.collectAsStateWithLifecycle()
                    val bottomPadding = if (stablePlayerState.currentSong != null && stablePlayerState.currentSong != Song.emptySong())
                        bottomBarHeight + MiniPlayerHeight + 16.dp
                    else
                        bottomBarHeight + 16.dp

                    ExpressiveScrollBar(
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

@Composable
fun ArtistListItem(artist: Artist, onClick: () -> Unit, isLoading: Boolean = false) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                // Skeleton loading state
                ShimmerBox(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!artist.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(artist.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = artist.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.rounded_artist_24),
                            contentDescription = "Artista",
                            modifier = Modifier.padding(8.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(artist.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${artist.songCount} Songs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun LibraryPlaylistsTab(
    playlistUiState: PlaylistUiState,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    bottomBarHeight: Dp,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    // Playlist multi-selection parameters
    isSelectionMode: Boolean = false,
    selectedPlaylistIds: Set<String> = emptySet(),
    onPlaylistLongPress: (com.theveloper.pixelplay.data.model.Playlist) -> Unit = {},
    onPlaylistSelectionToggle: (com.theveloper.pixelplay.data.model.Playlist) -> Unit = {},
    onPlaylistOptionsClick: () -> Unit = {}
) {
    PlaylistContainer(
        playlistUiState = playlistUiState,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        bottomBarHeight = bottomBarHeight,
        navController = navController,
        playerViewModel = playerViewModel,
        // Playlist multi-selection parameters
        isSelectionMode = isSelectionMode,
        selectedPlaylistIds = selectedPlaylistIds,
        onPlaylistLongPress = onPlaylistLongPress,
        onPlaylistSelectionToggle = onPlaylistSelectionToggle
    )
}

@Composable
fun AlbumListItem(
    album: Album,
    albumColorSchemePairFlow: StateFlow<ColorSchemePair?>,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    val albumColorSchemePair by albumColorSchemePairFlow.collectAsStateWithLifecycle()
    val systemIsDark = LocalPixelPlayDarkTheme.current
    val currentMaterialColorScheme = MaterialTheme.colorScheme

    val itemDesignColorScheme = remember(albumColorSchemePair, systemIsDark, currentMaterialColorScheme) {
        albumColorSchemePair?.let { pair ->
            if (systemIsDark) pair.dark else pair.light
        } ?: currentMaterialColorScheme
    }

    val gradientBaseColor = itemDesignColorScheme.primaryContainer
    val onGradientColor = itemDesignColorScheme.onPrimaryContainer
    val cardCornerRadius = 16.dp

    if (isLoading) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                ShimmerBox(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxHeight()
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    } else {
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp),
            shape = RoundedCornerShape(cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = itemDesignColorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                // LEFT: Album Art
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxHeight()
                ) {
                    var isLoadingImage by remember { mutableStateOf(true) }
                    SmartImage(
                        model = album.albumArtUriString,
                        contentDescription = "Carátula de ${album.title}",
                        contentScale = ContentScale.Crop,
                        targetSize = Size(256, 256),
                        modifier = Modifier.fillMaxSize(),
                        onState = { state ->
                            isLoadingImage = state is AsyncImagePainter.State.Loading
                        }
                    )
                    if (isLoadingImage) {
                        ShimmerBox(modifier = Modifier.fillMaxSize())
                    }

                    // Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        gradientBaseColor
                                    )
                                )
                            )
                    )
                }

                // MIDDLE: Solid Background
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(gradientBaseColor)
                ) {
                    // Text on top of the gradient/solid background
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        val variableTextStyle = remember(album.id, album.title) {
                            GenreTypography.getGenreStyle(album.id.toString(), album.title)
                        }

                        Text(
                            album.title,
                            style = variableTextStyle.copy(fontSize = 22.sp),
                            color = onGradientColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )
                        Text(
                            album.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = onGradientColor.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${album.songCount} Songs",
                            style = MaterialTheme.typography.bodySmall,
                            color = onGradientColor.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
