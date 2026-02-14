package com.theveloper.pixelplay.presentation.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.format.Formatter
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ClearAll
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.backup.model.BackupHistoryEntry
import com.theveloper.pixelplay.data.backup.model.BackupOperationType
import com.theveloper.pixelplay.data.backup.model.BackupSection
import com.theveloper.pixelplay.data.backup.model.BackupTransferProgressUpdate
import com.theveloper.pixelplay.data.backup.model.ModuleRestoreDetail
import com.theveloper.pixelplay.data.backup.model.RestorePlan
import com.theveloper.pixelplay.data.preferences.AppThemeMode
import com.theveloper.pixelplay.data.preferences.CarouselStyle
import com.theveloper.pixelplay.data.preferences.LaunchTab
import com.theveloper.pixelplay.data.preferences.LibraryNavigationMode
import com.theveloper.pixelplay.data.preferences.NavBarStyle
import com.theveloper.pixelplay.data.preferences.ThemePreference
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.data.model.LyricsSourcePreference
import com.theveloper.pixelplay.presentation.components.ExpressiveTopBarContent
import com.theveloper.pixelplay.presentation.components.FileExplorerDialog
import com.theveloper.pixelplay.presentation.components.MiniPlayerHeight
import com.theveloper.pixelplay.presentation.model.SettingsCategory
import com.theveloper.pixelplay.presentation.navigation.Screen
import com.theveloper.pixelplay.presentation.viewmodel.LyricsRefreshProgress
import com.theveloper.pixelplay.presentation.viewmodel.PlayerViewModel
import com.theveloper.pixelplay.presentation.viewmodel.SettingsViewModel
import com.theveloper.pixelplay.ui.theme.GoogleSansRounded

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsCategoryScreen(
    categoryId: String,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    statsViewModel: com.theveloper.pixelplay.presentation.viewmodel.StatsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val category = SettingsCategory.fromId(categoryId) ?: return
    val context = LocalContext.current
    
    // State Collection (Duplicated from SettingsScreen for now to ensure functionality)
    val uiState by settingsViewModel.uiState.collectAsState()
    val geminiApiKey by settingsViewModel.geminiApiKey.collectAsState()
    val geminiModel by settingsViewModel.geminiModel.collectAsState()
    val geminiSystemPrompt by settingsViewModel.geminiSystemPrompt.collectAsState()
    val currentPath by settingsViewModel.currentPath.collectAsState()
    val directoryChildren by settingsViewModel.currentDirectoryChildren.collectAsState()
    val availableStorages by settingsViewModel.availableStorages.collectAsState()
    val selectedStorageIndex by settingsViewModel.selectedStorageIndex.collectAsState()
    val isLoadingDirectories by settingsViewModel.isLoadingDirectories.collectAsState()
    val isExplorerPriming by settingsViewModel.isExplorerPriming.collectAsState()
    val isExplorerReady by settingsViewModel.isExplorerReady.collectAsState()
    val isSyncing by settingsViewModel.isSyncing.collectAsState()
    val syncProgress by settingsViewModel.syncProgress.collectAsState()
    val dataTransferProgress by settingsViewModel.dataTransferProgress.collectAsState()
    val allSongs by playerViewModel.allSongsFlow.collectAsState()
    val explorerRoot = settingsViewModel.explorerRoot()
    val rebuildDatabaseIndicatorLabel = stringResource(R.string.settings_rebuild_database_label)
    val rebuildDatabaseToast = stringResource(R.string.settings_rebuild_database_toast)
    val dailyMixRegenerateStartedToast = stringResource(R.string.settings_regenerate_daily_mix_started)
    val statsRegenerateStartedToast = stringResource(R.string.settings_regenerate_stats_started)
    val librarySyncFinishedToast = stringResource(R.string.toast_library_sync_finished)

    // Local State
    var showExplorerSheet by remember { mutableStateOf(false) }
    var refreshRequested by remember { mutableStateOf(false) }
    var syncRequestObservedRunning by remember { mutableStateOf(false) }
    var syncIndicatorLabel by remember { mutableStateOf<String?>(null) }
    var showClearLyricsDialog by remember { mutableStateOf(false) }
    var showRebuildDatabaseWarning by remember { mutableStateOf(false) }
    var showRegenerateDailyMixDialog by remember { mutableStateOf(false) }
    var showRegenerateStatsDialog by remember { mutableStateOf(false) }
    var showExportDataDialog by remember { mutableStateOf(false) }
    var showImportFlow by remember { mutableStateOf(false) }
    var exportSections by remember { mutableStateOf(BackupSection.defaultSelection) }
    var importFileUri by remember { mutableStateOf<Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            settingsViewModel.exportAppData(uri, exportSections)
        }
    }

    val importFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            importFileUri = uri
            settingsViewModel.inspectBackupFile(uri)
        }
    }

    LaunchedEffect(Unit) {
        settingsViewModel.dataTransferEvents.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(isSyncing, refreshRequested) {
        if (!refreshRequested) return@LaunchedEffect

        if (isSyncing) {
            syncRequestObservedRunning = true
        } else if (syncRequestObservedRunning) {
            Toast.makeText(context, librarySyncFinishedToast, Toast.LENGTH_SHORT).show()
            refreshRequested = false
            syncRequestObservedRunning = false
            syncIndicatorLabel = null
        }
    }

    var showPaletteRegenerateSheet by remember { mutableStateOf(false) }
    var isPaletteRegenerateRunning by remember { mutableStateOf(false) }
    var paletteSongSearchQuery by remember { mutableStateOf("") }
    val paletteRegenerateSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val songsWithAlbumArt = remember(allSongs) {
        allSongs.filter { !it.albumArtUriString.isNullOrBlank() }
    }
    val filteredPaletteSongs = remember(songsWithAlbumArt, paletteSongSearchQuery) {
        val query = paletteSongSearchQuery.trim()
        if (query.isBlank()) {
            songsWithAlbumArt
        } else {
            songsWithAlbumArt.filter { song ->
                song.title.contains(query, ignoreCase = true) ||
                    song.displayArtist.contains(query, ignoreCase = true) ||
                    song.album.contains(query, ignoreCase = true)
            }
        }
    }

    // Fetch models on page load when API key exists and models are not already loaded
    LaunchedEffect(category, geminiApiKey) {
        if (category == SettingsCategory.AI_INTEGRATION && 
            geminiApiKey.isNotBlank() && 
            uiState.availableModels.isEmpty() && 
            !uiState.isLoadingModels) {
            settingsViewModel.fetchAvailableModels(geminiApiKey)
        }
    }

    // TopBar Animations (identical to SettingsScreen)
    // TopBar Animations (identical to SettingsScreen)
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    
    val isLongTitle = stringResource(category.titleResId).length > 13
    
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val minTopBarHeight = 64.dp + statusBarHeight
    val maxTopBarHeight = if (isLongTitle) 200.dp else 180.dp //for 2 lines use 220 and make text use \n

    val minTopBarHeightPx = with(density) { minTopBarHeight.toPx() }
    val maxTopBarHeightPx = with(density) { maxTopBarHeight.toPx() }
    
    val titleMaxLines = if (isLongTitle) 2 else 1

    val topBarHeight = remember(maxTopBarHeightPx) { Animatable(maxTopBarHeightPx) }
    var collapseFraction by remember { mutableStateOf(0f) }

    LaunchedEffect(topBarHeight.value, maxTopBarHeightPx) {
        collapseFraction =
                1f -
                        ((topBarHeight.value - minTopBarHeightPx) /
                                        (maxTopBarHeightPx - minTopBarHeightPx))
                                .coerceIn(0f, 1f)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val isScrollingDown = delta < 0

                if (!isScrollingDown &&
                                (lazyListState.firstVisibleItemIndex > 0 ||
                                        lazyListState.firstVisibleItemScrollOffset > 0)
                ) {
                    return Offset.Zero
                }

                val previousHeight = topBarHeight.value
                val newHeight =
                        (previousHeight + delta).coerceIn(minTopBarHeightPx, maxTopBarHeightPx)
                val consumed = newHeight - previousHeight

                if (consumed.roundToInt() != 0) {
                    coroutineScope.launch { topBarHeight.snapTo(newHeight) }
                }

                val canConsumeScroll = !(isScrollingDown && newHeight == minTopBarHeightPx)
                return if (canConsumeScroll) Offset(0f, consumed) else Offset.Zero
            }
        }
    }

    LaunchedEffect(lazyListState.isScrollInProgress) {
        if (!lazyListState.isScrollInProgress) {
            val shouldExpand = topBarHeight.value > (minTopBarHeightPx + maxTopBarHeightPx) / 2
            val canExpand =
                    lazyListState.firstVisibleItemIndex == 0 &&
                            lazyListState.firstVisibleItemScrollOffset == 0

            val targetValue =
                    if (shouldExpand && canExpand) maxTopBarHeightPx else minTopBarHeightPx

            if (topBarHeight.value != targetValue) {
                coroutineScope.launch {
                    topBarHeight.animateTo(targetValue, spring(stiffness = Spring.StiffnessMedium))
                }
            }
        }
    }

    Box(
        modifier =
            Modifier.nestedScroll(nestedScrollConnection).fillMaxSize()
    ) {
        val currentTopBarHeightDp = with(density) { topBarHeight.value.toDp() }
        
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = currentTopBarHeightDp,
                start = 16.dp,
                end = 16.dp,
                bottom = MiniPlayerHeight + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
            )
        ) {
            item {
               // Use a simple Column for now, or ExpressiveSettingsGroup if preferred strictly for items
               Column(
                    modifier = Modifier.background(Color.Transparent)
               ) {
                    when (category) {
                        SettingsCategory.LIBRARY -> {
                            SettingsSubsection(title = stringResource(R.string.settings_subsection_library_structure)) {
                                SettingsItem(
                                    title = stringResource(R.string.settings_excluded_directories_title),
                                    subtitle = stringResource(R.string.settings_excluded_directories_subtitle),
                                    leadingIcon = { Icon(Icons.Outlined.Folder, null, tint = MaterialTheme.colorScheme.secondary) },
                                    trailingIcon = { Icon(Icons.Rounded.ChevronRight, stringResource(R.string.settings_open), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = {
                                        val hasAllFilesPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            Environment.isExternalStorageManager()
                                        } else true

                                        if (!hasAllFilesPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                            intent.data = "package:${context.packageName}".toUri()
                                            context.startActivity(intent)
                                            return@SettingsItem
                                        }

                                        showExplorerSheet = true
                                        if (!isExplorerReady && !isExplorerPriming) {
                                            settingsViewModel.primeExplorer()
                                        }
                                    }
                                )
                                SettingsItem(
                                    title = stringResource(R.string.settings_artists_title),
                                    subtitle = stringResource(R.string.settings_artists_subtitle),
                                    leadingIcon = { Icon(Icons.Outlined.Person, null, tint = MaterialTheme.colorScheme.secondary) },
                                    trailingIcon = { Icon(Icons.Rounded.ChevronRight, stringResource(R.string.settings_open), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { navController.navigate(Screen.ArtistSettings.route) }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.settings_subsection_sync_scanning)) {
                                RefreshLibraryItem(
                                    isSyncing = isSyncing,
                                    syncProgress = syncProgress,
                                    activeOperationLabel = if (isSyncing) syncIndicatorLabel else null,
                                    onFullSync = {
                                        if (isSyncing) return@RefreshLibraryItem
                                        refreshRequested = true
                                        syncRequestObservedRunning = false
                                        syncIndicatorLabel = context.getString(R.string.settings_full_rescan_running)
                                        Toast.makeText(context, R.string.settings_full_rescan_started, Toast.LENGTH_SHORT).show()
                                        settingsViewModel.fullSyncLibrary()
                                    },
                                    onRebuild = {
                                        if (isSyncing) return@RefreshLibraryItem
                                        showRebuildDatabaseWarning = true
                                    }
                                )
                                SwitchSettingItem(
                                    title = stringResource(R.string.settings_auto_scan_lrc_title),
                                    subtitle = stringResource(R.string.settings_auto_scan_lrc_subtitle),
                                    checked = uiState.autoScanLrcFiles,
                                    onCheckedChange = { settingsViewModel.setAutoScanLrcFiles(it) },
                                    leadingIcon = { Icon(Icons.Outlined.Folder, null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }

                            SettingsSubsection(
                                title = stringResource(R.string.settings_subsection_lyrics_management),
                                addBottomSpace = false
                            ) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.settings_lyrics_source_priority_title),
                                    description = stringResource(R.string.settings_lyrics_source_priority_subtitle),
                                    options = mapOf(
                                        LyricsSourcePreference.EMBEDDED_FIRST.name to stringResource(R.string.settings_lyrics_source_embedded),
                                        LyricsSourcePreference.API_FIRST.name to stringResource(R.string.settings_lyrics_source_api),
                                        LyricsSourcePreference.LOCAL_FIRST.name to stringResource(R.string.settings_lyrics_source_local)
                                    ),
                                    selectedKey = uiState.lyricsSourcePreference.name,
                                    onSelectionChanged = { key ->
                                        settingsViewModel.setLyricsSourcePreference(
                                            LyricsSourcePreference.fromName(key)
                                        )
                                    },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_lyrics_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SettingsItem(
                                    title = stringResource(R.string.settings_reset_lyrics_title),
                                    subtitle = stringResource(R.string.settings_reset_lyrics_subtitle),
                                    leadingIcon = { Icon(Icons.Outlined.ClearAll, null, tint = MaterialTheme.colorScheme.secondary) },
                                    onClick = { showClearLyricsDialog = true }
                                )
                            }
                        }
                        SettingsCategory.APPEARANCE -> {
                            val useSmoothCorners by settingsViewModel.useSmoothCorners.collectAsState()

                            SettingsSubsection(title = stringResource(R.string.settings_subsection_global_theme)) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.settings_app_theme_title),
                                    description = stringResource(R.string.settings_app_theme_subtitle),
                                    options = mapOf(
                                        AppThemeMode.LIGHT to stringResource(R.string.settings_app_theme_light),
                                        AppThemeMode.DARK to stringResource(R.string.settings_app_theme_dark),
                                        AppThemeMode.FOLLOW_SYSTEM to stringResource(R.string.settings_app_theme_system)
                                    ),
                                    selectedKey = uiState.appThemeMode,
                                    onSelectionChanged = { settingsViewModel.setAppThemeMode(it) },
                                    leadingIcon = { Icon(Icons.Outlined.LightMode, null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SwitchSettingItem(
                                    title = stringResource(R.string.settings_smooth_corners_title),
                                    subtitle = stringResource(R.string.settings_smooth_corners_subtitle),
                                    checked = useSmoothCorners,
                                    onCheckedChange = settingsViewModel::setUseSmoothCorners,
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_rounded_corner_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.settings_subsection_now_playing)) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.settings_player_theme_title),
                                    description = stringResource(R.string.settings_player_theme_subtitle),
                                    options = mapOf(
                                        ThemePreference.ALBUM_ART to stringResource(R.string.settings_player_theme_album),
                                        ThemePreference.DYNAMIC to stringResource(R.string.settings_player_theme_dynamic)
                                    ),
                                    selectedKey = uiState.playerThemePreference,
                                    onSelectionChanged = { settingsViewModel.setPlayerThemePreference(it) },
                                    leadingIcon = { Icon(Icons.Outlined.PlayCircle, null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SwitchSettingItem(
                                    title = stringResource(R.string.settings_show_file_info_title),
                                    subtitle = stringResource(R.string.settings_show_file_info_subtitle),
                                    checked = uiState.showPlayerFileInfo,
                                    onCheckedChange = { settingsViewModel.setShowPlayerFileInfo(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_attach_file_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SettingsItem(
                                    title = stringResource(R.string.settings_palette_style_title),
                                    subtitle = stringResource(R.string.settings_palette_style_subtitle_format, uiState.albumArtPaletteStyle.label),
                                    leadingIcon = { Icon(Icons.Outlined.Style, null, tint = MaterialTheme.colorScheme.secondary) },
                                    trailingIcon = { Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { navController.navigate(Screen.PaletteStyle.route) }
                                )
                                ThemeSelectorItem(
                                    label = stringResource(R.string.settings_carousel_style_title),
                                    description = stringResource(R.string.settings_carousel_style_subtitle),
                                    options = mapOf(
                                        CarouselStyle.NO_PEEK to stringResource(R.string.settings_carousel_style_no_peek),
                                        CarouselStyle.ONE_PEEK to stringResource(R.string.settings_carousel_style_one_peek)
                                    ),
                                    selectedKey = uiState.carouselStyle,
                                    onSelectionChanged = { settingsViewModel.setCarouselStyle(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_view_carousel_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.settings_subsection_navigation_bar)) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.settings_navbar_style_title),
                                    description = stringResource(R.string.settings_navbar_style_subtitle),
                                    options = mapOf(
                                        NavBarStyle.DEFAULT to stringResource(R.string.settings_navbar_style_default),
                                        NavBarStyle.FULL_WIDTH to stringResource(R.string.settings_navbar_style_full)
                                    ),
                                    selectedKey = uiState.navBarStyle,
                                    onSelectionChanged = { settingsViewModel.setNavBarStyle(it) },
                                    leadingIcon = { Icon(Icons.Outlined.Style, null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SettingsItem(
                                    title = stringResource(R.string.settings_navbar_radius_title),
                                    subtitle = stringResource(R.string.settings_navbar_radius_subtitle),
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_rounded_corner_24), null, tint = MaterialTheme.colorScheme.secondary) },
                                    trailingIcon = { Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { navController.navigate("nav_bar_corner_radius") }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.settings_subsection_lyrics_screen)) {
                                SwitchSettingItem(
                                    title = stringResource(R.string.settings_immersive_lyrics_title),
                                    subtitle = stringResource(R.string.settings_immersive_lyrics_subtitle),
                                    checked = uiState.immersiveLyricsEnabled,
                                    onCheckedChange = { settingsViewModel.setImmersiveLyricsEnabled(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_lyrics_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )

                                if (uiState.immersiveLyricsEnabled) {
                                    ThemeSelectorItem(
                                        label = stringResource(R.string.settings_autohide_delay_title),
                                        description = stringResource(R.string.settings_autohide_delay_subtitle),
                                        options = mapOf(
                                            "3000" to stringResource(R.string.settings_3s),
                                            "4000" to stringResource(R.string.settings_4s),
                                            "5000" to stringResource(R.string.settings_5s),
                                            "6000" to stringResource(R.string.settings_6s)
                                        ),
                                        selectedKey = uiState.immersiveLyricsTimeout.toString(),
                                        onSelectionChanged = { settingsViewModel.setImmersiveLyricsTimeout(it.toLong()) },
                                        leadingIcon = { Icon(Icons.Rounded.Timer, null, tint = MaterialTheme.colorScheme.secondary) }
                                    )
                                }
                            }

                            SettingsSubsection(
                                title = stringResource(R.string.settings_subsection_app_navigation),
                                addBottomSpace = false
                            ) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.settings_default_tab_title),
                                    description = stringResource(R.string.settings_default_tab_subtitle),
                                    options = mapOf(
                                        LaunchTab.HOME to stringResource(R.string.settings_default_tab_home),
                                        LaunchTab.SEARCH to stringResource(R.string.settings_default_tab_search),
                                        LaunchTab.LIBRARY to stringResource(R.string.settings_default_tab_library),
                                    ),
                                    selectedKey = uiState.launchTab,
                                    onSelectionChanged = { settingsViewModel.setLaunchTab(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.tab_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                ThemeSelectorItem(
                                    label = stringResource(R.string.settings_library_nav_title),
                                    description = stringResource(R.string.settings_library_nav_subtitle),
                                    options = mapOf(
                                        LibraryNavigationMode.TAB_ROW to stringResource(R.string.settings_library_nav_tab_row),
                                        LibraryNavigationMode.COMPACT_PILL to stringResource(R.string.settings_library_nav_compact)
                                    ),
                                    selectedKey = uiState.libraryNavigationMode,
                                    onSelectionChanged = { settingsViewModel.setLibraryNavigationMode(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_library_music_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }
                        }
                        SettingsCategory.PLAYBACK -> {
                            SettingsSubsection(title = stringResource(R.string.settings_subsection_background_playback)) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.settings_keep_playing_title),
                                    description = stringResource(R.string.settings_keep_playing_subtitle),
                                    options = mapOf("true" to stringResource(R.string.settings_on), "false" to stringResource(R.string.settings_off)),
                                    selectedKey = if (uiState.keepPlayingInBackground) "true" else "false",
                                    onSelectionChanged = { settingsViewModel.setKeepPlayingInBackground(it.toBoolean()) },
                                    leadingIcon = { Icon(Icons.Rounded.MusicNote, null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SettingsItem(
                                    title = stringResource(R.string.settings_battery_optimization_title),
                                    subtitle = stringResource(R.string.settings_battery_optimization_subtitle),
                                    onClick = {
                                        val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
                                        if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                                            Toast.makeText(context, R.string.settings_battery_optimization_already_disabled, Toast.LENGTH_SHORT).show()
                                            return@SettingsItem
                                        }
                                        try {
                                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                                data = "package:${context.packageName}".toUri()
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            try {
                                                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                                context.startActivity(fallbackIntent)
                                            } catch (e2: Exception) {
                                                Toast.makeText(context, R.string.settings_could_not_open_battery_settings, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_all_inclusive_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.settings_subsection_cast)) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.settings_cast_autoplay_title),
                                    description = stringResource(R.string.settings_cast_autoplay_subtitle),
                                    options = mapOf("false" to stringResource(R.string.settings_enabled), "true" to stringResource(R.string.settings_disabled)),
                                    selectedKey = if (uiState.disableCastAutoplay) "true" else "false",
                                    onSelectionChanged = { settingsViewModel.setDisableCastAutoplay(it.toBoolean()) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_cast_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.settings_subsection_queue_transitions)) {
                                ThemeSelectorItem(
                                    label = stringResource(R.string.settings_crossfade_title),
                                    description = stringResource(R.string.settings_crossfade_subtitle),
                                    options = mapOf("true" to stringResource(R.string.settings_enabled), "false" to stringResource(R.string.settings_disabled)),
                                    selectedKey = if (uiState.isCrossfadeEnabled) "true" else "false",
                                    onSelectionChanged = { settingsViewModel.setCrossfadeEnabled(it.toBoolean()) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_align_justify_space_even_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                if (uiState.isCrossfadeEnabled) {
                                    SliderSettingsItem(
                                        label = stringResource(R.string.settings_crossfade_duration_title),
                                        value = uiState.crossfadeDuration.toFloat(),
                                        valueRange = 2000f..12000f,
                                        onValueChange = { settingsViewModel.setCrossfadeDuration(it.toInt()) },
                                        valueText = { value -> "${(value / 1000).toInt()}s" }
                                    )
                                }
                                SwitchSettingItem(
                                    title = stringResource(R.string.settings_persistent_shuffle_title),
                                    subtitle = stringResource(R.string.settings_persistent_shuffle_subtitle),
                                    checked = uiState.persistentShuffleEnabled,
                                    onCheckedChange = { settingsViewModel.setPersistentShuffleEnabled(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_shuffle_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                                SwitchSettingItem(
                                    title = stringResource(R.string.settings_show_queue_history_title),
                                    subtitle = stringResource(R.string.settings_show_queue_history_subtitle),
                                    checked = uiState.showQueueHistory,
                                    onCheckedChange = { settingsViewModel.setShowQueueHistory(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_queue_music_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }

                        }
                        SettingsCategory.BEHAVIOR -> {
                            SettingsSubsection(
                                title = stringResource(R.string.settings_subsection_folders)
                            ) {
                                SwitchSettingItem(
                                    title = stringResource(R.string.settings_folder_back_nav_title),
                                    subtitle = stringResource(R.string.settings_folder_back_nav_subtitle),
                                    checked = uiState.folderBackGestureNavigation,
                                    onCheckedChange = { settingsViewModel.setFolderBackGestureNavigation(it) },
                                    leadingIcon = {
                                        Icon(
                                            painterResource(R.drawable.rounded_touch_app_24),
                                            null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                )
                            }
                            SettingsSubsection(
                                title = stringResource(R.string.settings_subsection_player_gestures)
                            ) {
                                SwitchSettingItem(
                                    title = stringResource(R.string.settings_tap_close_player_title),
                                    subtitle = stringResource(R.string.settings_tap_close_player_subtitle),
                                    checked = uiState.tapBackgroundClosesPlayer,
                                    onCheckedChange = { settingsViewModel.setTapBackgroundClosesPlayer(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_touch_app_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }
                            SettingsSubsection(
                                title = stringResource(R.string.settings_subsection_haptics),
                                addBottomSpace = false
                            ) {
                                SwitchSettingItem(
                                    title = stringResource(R.string.settings_haptics_title),
                                    subtitle = stringResource(R.string.settings_haptics_subtitle),
                                    checked = uiState.hapticsEnabled,
                                    onCheckedChange = { settingsViewModel.setHapticsEnabled(it) },
                                    leadingIcon = { Icon(painterResource(R.drawable.rounded_touch_app_24), null, tint = MaterialTheme.colorScheme.secondary) }
                                )
                            }
                        }
                        SettingsCategory.AI_INTEGRATION -> {
                            SettingsSubsection(title = stringResource(R.string.settings_subsection_credentials)) {
                                GeminiApiKeyItem(
                                    apiKey = geminiApiKey,
                                    onApiKeySave = { settingsViewModel.onGeminiApiKeyChange(it) },
                                    title = stringResource(R.string.settings_gemini_api_key_title),
                                    subtitle = stringResource(R.string.settings_gemini_api_key_subtitle)
                                )
                            }

                            if (uiState.availableModels.isNotEmpty()) {
                                SettingsSubsection(title = stringResource(R.string.settings_subsection_model_selection)) {
                                    if (uiState.isLoadingModels) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceContainer,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    strokeWidth = 2.dp
                                                )
                                                Text(
                                                    text = stringResource(R.string.settings_ai_loading_models),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    } else if (uiState.modelsFetchError != null) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = uiState.modelsFetchError ?: stringResource(R.string.settings_ai_models_error),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.padding(16.dp)
                                            )
                                        }
                                    } else if (uiState.availableModels.isNotEmpty()) {
                                        ThemeSelectorItem(
                                            label = stringResource(R.string.settings_ai_model_title),
                                            description = stringResource(R.string.settings_ai_model_subtitle),
                                            options = uiState.availableModels.associate { it.name to it.displayName },
                                            selectedKey = geminiModel.ifEmpty { uiState.availableModels.firstOrNull()?.name ?: "" },
                                            onSelectionChanged = { settingsViewModel.onGeminiModelChange(it) },
                                            leadingIcon = { Icon(Icons.Rounded.Science, null, tint = MaterialTheme.colorScheme.secondary) }
                                        )
                                    }
                                }
                            }

                            SettingsSubsection(
                                title = stringResource(R.string.settings_subsection_prompt_behavior),
                                addBottomSpace = false
                            ) {
                                GeminiSystemPromptItem(
                                    systemPrompt = geminiSystemPrompt,
                                    defaultPrompt = com.theveloper.pixelplay.data.preferences.UserPreferencesRepository.DEFAULT_SYSTEM_PROMPT,
                                    onSystemPromptSave = { settingsViewModel.onGeminiSystemPromptChange(it) },
                                    onReset = { settingsViewModel.resetGeminiSystemPrompt() },
                                    title = stringResource(R.string.settings_system_prompt_title),
                                    subtitle = stringResource(R.string.settings_system_prompt_subtitle)
                                )
                            }
                        }
                        SettingsCategory.BACKUP_RESTORE -> {
                            if (!uiState.backupInfoDismissed) {
                                BackupInfoNoticeCard(
                                    onDismiss = { settingsViewModel.setBackupInfoDismissed(true) }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            SettingsSubsection(title = stringResource(R.string.settings_export_backup_title)) {
                                ActionSettingsItem(
                                    title = stringResource(R.string.settings_export_backup_title),
                                    subtitle = "${buildBackupSelectionSummary(exportSections, context)} ${stringResource(R.string.settings_backup_file_suffix_desc)}",
                                    icon = {
                                        Icon(
                                            painter = painterResource(R.drawable.outline_save_24),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    },
                                    primaryActionLabel = stringResource(R.string.settings_dialog_select_and_export),
                                    onPrimaryAction = { showExportDataDialog = true },
                                    enabled = !uiState.isDataTransferInProgress
                                )
                            }

                            SettingsSubsection(
                                title = stringResource(R.string.settings_import_backup_title),
                                addBottomSpace = false
                            ) {
                                ActionSettingsItem(
                                    title = stringResource(R.string.settings_import_backup_title),
                                    subtitle = stringResource(R.string.settings_import_backup_desc),
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Restore,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    },
                                    primaryActionLabel = stringResource(R.string.settings_dialog_select_and_restore),
                                    onPrimaryAction = { showImportFlow = true },
                                    enabled = !uiState.isDataTransferInProgress
                                )
                            }
                        }
                        SettingsCategory.DEVELOPER -> {
                            SettingsSubsection(title = stringResource(R.string.settings_subsection_experiments)) {
                                SettingsItem(
                                    title = stringResource(R.string.settings_experimental_title),
                                    subtitle = stringResource(R.string.settings_experimental_subtitle),
                                    leadingIcon = { Icon(Icons.Rounded.Science, null, tint = MaterialTheme.colorScheme.secondary) },
                                    trailingIcon = { Icon(Icons.Rounded.ChevronRight, stringResource(R.string.settings_open), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { navController.navigate(Screen.Experimental.route) }
                                )
                                SettingsItem(
                                    title = stringResource(R.string.settings_test_setup_flow_title),
                                    subtitle = stringResource(R.string.settings_test_setup_flow_subtitle),
                                    leadingIcon = { Icon(Icons.Rounded.Science, null, tint = MaterialTheme.colorScheme.tertiary) },
                                    onClick = {
                                        settingsViewModel.resetSetupFlow()
                                    }
                                )
                            }

                            SettingsSubsection(title = stringResource(R.string.settings_subsection_maintenance)) {
                                ActionSettingsItem(
                                    title = stringResource(R.string.settings_regenerate_daily_mix_title),
                                    subtitle = stringResource(R.string.settings_regenerate_daily_mix_subtitle),
                                    icon = { Icon(painterResource(R.drawable.rounded_instant_mix_24), null, tint = MaterialTheme.colorScheme.secondary) },
                                    primaryActionLabel = stringResource(R.string.settings_regenerate_daily_mix_started),
                                    onPrimaryAction = { showRegenerateDailyMixDialog = true }
                                )
                                ActionSettingsItem(
                                    title = stringResource(R.string.settings_regenerate_stats_title),
                                    subtitle = stringResource(R.string.settings_regenerate_stats_subtitle),
                                    icon = { Icon(painterResource(R.drawable.rounded_monitoring_24), null, tint = MaterialTheme.colorScheme.secondary) },
                                    primaryActionLabel = stringResource(R.string.settings_regenerate_stats_started),
                                    onPrimaryAction = { showRegenerateStatsDialog = true }
                                )
                                ActionSettingsItem(
                                    title = stringResource(R.string.settings_regenerate_palette_title),
                                    subtitle = if (songsWithAlbumArt.isEmpty()) {
                                        stringResource(R.string.settings_regenerate_palette_no_songs)
                                    } else {
                                        stringResource(R.string.settings_regenerate_palette_subtitle)
                                    },
                                    icon = { Icon(Icons.Outlined.Style, null, tint = MaterialTheme.colorScheme.secondary) },
                                    primaryActionLabel = stringResource(R.string.settings_choose_song),
                                    onPrimaryAction = { showPaletteRegenerateSheet = true },
                                    enabled = songsWithAlbumArt.isNotEmpty() && !isPaletteRegenerateRunning
                                )
                            }

                            SettingsSubsection(
                                title = stringResource(R.string.settings_subsection_diagnostics),
                                addBottomSpace = false
                            ) {
                                SettingsItem(
                                    title = stringResource(R.string.settings_trigger_crash_title),
                                    subtitle = stringResource(R.string.settings_trigger_crash_subtitle),
                                    leadingIcon = { Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = { settingsViewModel.triggerTestCrash() }
                                )
                            }
                        }
                        SettingsCategory.ABOUT -> {
                            SettingsSubsection(
                                title = stringResource(R.string.settings_subsection_application),
                                addBottomSpace = false
                            ) {
                                SettingsItem(
                                    title = stringResource(R.string.settings_about_pixelplayer_title),
                                    subtitle = stringResource(R.string.settings_about_pixelplayer_subtitle),
                                    leadingIcon = { Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.secondary) },
                                    trailingIcon = { Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { navController.navigate("about") }
                                )
                            }
                        }
                        SettingsCategory.EQUALIZER -> {
                             // Equalizer has its own screen, so this block is unreachable via standard navigation
                             // but required for exhaustiveness.
                        }

                    }
               }
            }

            item {
                // Spacer handled by contentPadding
                Spacer(Modifier.height(1.dp))
            }
        }

        SettingsTopBar(
            collapseFraction = collapseFraction,
            headerHeight = currentTopBarHeightDp,
            onBackPressed = onBackClick,
            title = stringResource(category.titleResId),
            expandedStartPadding = 20.dp,
            collapsedStartPadding = 68.dp,
            maxLines = titleMaxLines
        )

        // Block interaction during transition
        var isTransitioning by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(com.theveloper.pixelplay.presentation.navigation.TRANSITION_DURATION.toLong())
            isTransitioning = false
        }
        
        if (isTransitioning) {
            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                   awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                        }
                    }
                }
            )
        }
    }

    dataTransferProgress?.let { progress ->
        BackupTransferProgressDialog(progress = progress)
    }

    // Dialogs
    FileExplorerDialog(
        visible = showExplorerSheet,
        currentPath = currentPath,
        directoryChildren = directoryChildren,
        availableStorages = availableStorages,
        selectedStorageIndex = selectedStorageIndex,
        isLoading = isLoadingDirectories,
        isAtRoot = settingsViewModel.isAtRoot(),
        rootDirectory = explorerRoot,
        onNavigateTo = settingsViewModel::loadDirectory,
        onNavigateUp = settingsViewModel::navigateUp,
        onNavigateHome = { settingsViewModel.loadDirectory(explorerRoot) },
        onToggleAllowed = settingsViewModel::toggleDirectoryAllowed,
        onRefresh = settingsViewModel::refreshExplorer,
        onStorageSelected = settingsViewModel::selectStorage,
        onDone = {
            settingsViewModel.applyPendingDirectoryRuleChanges()
            showExplorerSheet = false
        },
        onDismiss = {
            settingsViewModel.applyPendingDirectoryRuleChanges()
            showExplorerSheet = false
        }
    )

    if (showPaletteRegenerateSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                if (!isPaletteRegenerateRunning) {
                    showPaletteRegenerateSheet = false
                    paletteSongSearchQuery = ""
                }
            },
            sheetState = paletteRegenerateSheetState
        ) {
            PaletteRegenerateSongSheetContent(
                songs = filteredPaletteSongs,
                isRunning = isPaletteRegenerateRunning,
                searchQuery = paletteSongSearchQuery,
                onSearchQueryChange = { paletteSongSearchQuery = it },
                onClearSearch = { paletteSongSearchQuery = "" },
                onSongClick = { song ->
                    if (isPaletteRegenerateRunning) return@PaletteRegenerateSongSheetContent
                    isPaletteRegenerateRunning = true
                    coroutineScope.launch {
                        val success = playerViewModel.forceRegenerateAlbumPaletteForSong(song)
                        isPaletteRegenerateRunning = false
                        if (success) {
                            showPaletteRegenerateSheet = false
                            paletteSongSearchQuery = ""
                            Toast.makeText(
                                context,
                                context.getString(R.string.settings_palette_regenerated_for, song.title),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.settings_palette_regenerate_failed_for, song.title),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }
    }
    
     // Dialogs logic (copied)
    if (showClearLyricsDialog) {
        AlertDialog(
            icon = { Icon(Icons.Outlined.Warning, null) },
            title = { Text(stringResource(R.string.settings_reset_lyrics_title)) },
            text = { Text(stringResource(R.string.settings_reset_lyrics_message)) },
            onDismissRequest = { showClearLyricsDialog = false },
            confirmButton = { TextButton(onClick = { showClearLyricsDialog = false; playerViewModel.resetAllLyrics() }) { Text(stringResource(R.string.settings_confirm)) } },
            dismissButton = { TextButton(onClick = { showClearLyricsDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    
    if (showRebuildDatabaseWarning) {
        AlertDialog(
            icon = { Icon(Icons.Outlined.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.settings_rebuild_database_title)) },
            text = { Text(stringResource(R.string.settings_rebuild_database_message)) },
            onDismissRequest = { showRebuildDatabaseWarning = false },
            confirmButton = { 
                TextButton(
                    onClick = { 
                        showRebuildDatabaseWarning = false
                        refreshRequested = true
                        syncRequestObservedRunning = false
                        syncIndicatorLabel = rebuildDatabaseIndicatorLabel
                        Toast.makeText(context, rebuildDatabaseToast, Toast.LENGTH_SHORT).show()
                        settingsViewModel.rebuildDatabase() 
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { 
                    Text(stringResource(R.string.settings_rebuild_database_confirm)) 
                } 
            },
            dismissButton = { TextButton(onClick = { showRebuildDatabaseWarning = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showRegenerateDailyMixDialog) {
        AlertDialog(
            icon = { Icon(painterResource(R.drawable.rounded_instant_mix_24), null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(stringResource(R.string.settings_regenerate_daily_mix_title)) },
            text = { Text(stringResource(R.string.settings_regenerate_daily_mix_message)) },
            onDismissRequest = { showRegenerateDailyMixDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRegenerateDailyMixDialog = false
                        playerViewModel.forceUpdateDailyMix()
                        Toast.makeText(context, dailyMixRegenerateStartedToast, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(stringResource(R.string.settings_regenerate_daily_mix_confirm))
                }
            },
            dismissButton = { TextButton(onClick = { showRegenerateDailyMixDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showRegenerateStatsDialog) {
        AlertDialog(
            icon = { Icon(painterResource(R.drawable.rounded_monitoring_24), null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(stringResource(R.string.settings_regenerate_stats_title)) },
            text = { Text(stringResource(R.string.settings_regenerate_stats_message)) },
            onDismissRequest = { showRegenerateStatsDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRegenerateStatsDialog = false
                        statsViewModel.forceRegenerateStats()
                        Toast.makeText(context, statsRegenerateStartedToast, Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(stringResource(R.string.settings_regenerate_stats_confirm))
                }
            },
            dismissButton = { TextButton(onClick = { showRegenerateStatsDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showExportDataDialog) {
        BackupSectionSelectionDialog(
            operation = BackupOperationType.EXPORT,
            title = stringResource(R.string.settings_export_backup_title),
            supportingText = stringResource(R.string.settings_export_backup_message),
            selectedSections = exportSections,
            confirmLabel = stringResource(R.string.settings_export_backup_confirm),
            inProgress = uiState.isDataTransferInProgress,
            onDismiss = { showExportDataDialog = false },
            onSelectionChanged = { exportSections = it },
            onConfirm = {
                showExportDataDialog = false
                val fileName = "PixelPlayer_Backup_${System.currentTimeMillis()}.pxpl"
                exportLauncher.launch(fileName)
            }
        )
    }

    if (showImportFlow) {
        val restorePlan = uiState.restorePlan
        if (restorePlan != null && importFileUri != null) {
            // Step 2: Module selection from inspected backup
            ImportModuleSelectionDialog(
                plan = restorePlan,
                inProgress = uiState.isDataTransferInProgress,
                onDismiss = {
                    showImportFlow = false
                    importFileUri = null
                    settingsViewModel.clearRestorePlan()
                },
                onBack = {
                    importFileUri = null
                    settingsViewModel.clearRestorePlan()
                },
                onSelectionChanged = { settingsViewModel.updateRestorePlanSelection(it) },
                onConfirm = {
                    settingsViewModel.restoreFromPlan(importFileUri!!)
                    showImportFlow = false
                    importFileUri = null
                }
            )
        } else {
            // Step 1: File selection with backup history
            ImportFileSelectionDialog(
                backupHistory = uiState.backupHistory,
                isInspecting = uiState.isInspectingBackup,
                onDismiss = {
                    showImportFlow = false
                    importFileUri = null
                    settingsViewModel.clearRestorePlan()
                },
                onBrowseFile = { importFilePicker.launch("*/*") },
                onHistoryItemSelected = { entry ->
                    val uri = entry.uri.toUri()
                    importFileUri = uri
                    settingsViewModel.inspectBackupFile(uri)
                },
                onRemoveHistoryEntry = { settingsViewModel.removeBackupHistoryEntry(it) }
            )
        }
    }
}

private fun buildBackupSelectionSummary(selected: Set<BackupSection>, context: android.content.Context): String {
    if (selected.isEmpty()) return context.getString(R.string.settings_backup_selection_summary_none)
    val total = BackupSection.entries.size
    return if (selected.size == total) {
        context.getString(R.string.settings_backup_selection_summary_all)
    } else {
        context.getString(R.string.settings_backup_selection_summary_format, selected.size, total)
    }
}

private fun backupSectionIconRes(section: BackupSection): Int {
    return section.iconRes
}

@Composable
private fun BackupInfoNoticeCard(
    onDismiss: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_upload_file_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(20.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_backup_how_works_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.settings_backup_how_works_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_close_24),
                    contentDescription = stringResource(R.string.dismiss),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BackupSectionSelectionDialog(
    operation: BackupOperationType,
    title: String,
    supportingText: String,
    selectedSections: Set<BackupSection>,
    confirmLabel: String,
    inProgress: Boolean,
    onDismiss: () -> Unit,
    onSelectionChanged: (Set<BackupSection>) -> Unit,
    onConfirm: () -> Unit
) {
    val localContext = LocalContext.current
    val listState = rememberLazyListState()
    val selectedCount = selectedSections.size
    val totalCount = BackupSection.entries.size
    val transitionState = remember { MutableTransitionState(false) }
    var shouldShowDialog by remember { mutableStateOf(true) }
    var onDialogHiddenAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    transitionState.targetState = shouldShowDialog

    fun closeDialog(afterClose: () -> Unit) {
        if (!shouldShowDialog) return
        onDialogHiddenAction = afterClose
        shouldShowDialog = false
    }

    LaunchedEffect(transitionState.currentState, transitionState.targetState) {
        if (!transitionState.currentState && !transitionState.targetState) {
            onDialogHiddenAction?.let { action ->
                onDialogHiddenAction = null
                action()
            }
        }
    }

    if (transitionState.currentState || transitionState.targetState) {
        Dialog(
            onDismissRequest = { closeDialog(onDismiss) },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            AnimatedVisibility(
                visibleState = transitionState,
                enter = slideInVertically(initialOffsetY = { it / 6 }) + fadeIn(animationSpec = tween(220)),
                exit = slideOutVertically(targetOffsetY = { it / 6 }) + fadeOut(animationSpec = tween(200)),
                label = "backup_section_dialog"
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest
                ) {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentWindowInsets = WindowInsets.systemBars,
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 24.sp,
                                            textGeometricTransform = TextGeometricTransform(scaleX = 1.2f),
                                        ),
                                        fontFamily = GoogleSansRounded,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                navigationIcon = {
                                    FilledIconButton(
                                        modifier = Modifier.padding(start = 6.dp),
                                        onClick = { closeDialog(onDismiss) },
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = stringResource(R.string.dismiss)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                )
                            )
                        },
                        bottomBar = {
                            BottomAppBar(
                                windowInsets = WindowInsets.navigationBars,
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilledIconButton(
                                            onClick = { onSelectionChanged(BackupSection.entries.toSet()) },
                                            enabled = !inProgress,
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.round_select_all_24),
                                                contentDescription = stringResource(R.string.settings_select_all)
                                            )
                                        }
                                        FilledIconButton(
                                            onClick = { onSelectionChanged(emptySet()) },
                                            enabled = !inProgress,
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.baseline_deselect_24),
                                                contentDescription = stringResource(R.string.settings_clear_selection)
                                            )
                                        }
                                    }

                                    ExtendedFloatingActionButton(
                                        onClick = { closeDialog(onConfirm) },
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        containerColor = if (operation == BackupOperationType.EXPORT) {
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.primaryContainer
                                        },
                                        contentColor = if (operation == BackupOperationType.EXPORT) {
                                            MaterialTheme.colorScheme.onTertiaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        }
                                    ) {
                                        if (inProgress) {
                                            LoadingIndicator(modifier = Modifier.height(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (operation == BackupOperationType.EXPORT) stringResource(R.string.settings_exporting) else stringResource(R.string.settings_importing),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        } else {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(
                                                        if (operation == BackupOperationType.EXPORT) {
                                                            R.drawable.outline_save_24
                                                        } else {
                                                            R.drawable.rounded_upload_file_24
                                                        }
                                                    ),
                                                    contentDescription = confirmLabel
                                                )

                                                Text(
                                                    text = if (operation == BackupOperationType.EXPORT) {
                                                        stringResource(R.string.settings_export_backup_title)
                                                    } else {
                                                        stringResource(R.string.settings_import_backup_title)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                                .padding(horizontal = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = supportingText,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = buildBackupSelectionSummary(selectedSections, localContext),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (inProgress) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            LoadingIndicator(modifier = Modifier.height(24.dp))
                                            Text(
                                                text = stringResource(R.string.settings_transfer_in_progress),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(BackupSection.entries, key = { it.key }) { section ->
                                    val isSelected = section in selectedSections
                                    BackupSectionSelectableCard(
                                        section = section,
                                        selected = isSelected,
                                        enabled = !inProgress,
                                        onToggle = {
                                            onSelectionChanged(
                                                if (isSelected) selectedSections - section else selectedSections + section
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun BackupSectionSelectableCard(
    section: BackupSection,
    selected: Boolean,
    enabled: Boolean,
    detail: ModuleRestoreDetail? = null,
    onToggle: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        } else {
            Color.Transparent
        },
        label = "backup_section_border"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (selected) 2.5.dp else 1.dp,
        label = "backup_section_border_width"
    )
    val iconContainerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        label = "backup_section_icon_bg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "backup_section_icon_tint"
    )

    Surface(
        onClick = onToggle,
        enabled = enabled,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(width = borderWidth, color = borderColor),
        tonalElevation = if (selected) 2.dp else 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = iconContainerColor,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(backupSectionIconRes(section)),
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val localContext = LocalContext.current
                    Text(
                        text = section.getLabel(localContext),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = GoogleSansRounded
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = section.getDescription(localContext),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (detail != null && detail.entryCount > 0) {
                        Text(
                            text = stringResource(R.string.backup_entry_count_format, detail.entryCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Switch(
                    checked = selected,
                    onCheckedChange = { onToggle() },
                    enabled = enabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BackupTransferProgressDialog(progress: BackupTransferProgressUpdate) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "BackupTransferProgress"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (progress.operation == BackupOperationType.EXPORT) {
                        stringResource(R.string.settings_creating_backup)
                    } else {
                        stringResource(R.string.settings_restoring_backup)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                CircularWavyProgressIndicator(modifier = Modifier.size(44.dp))
                LoadingIndicator(modifier = Modifier.height(24.dp))

                LinearWavyProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )

                Text(
                    text = stringResource(R.string.settings_step_format, progress.step.coerceAtLeast(1), progress.totalSteps),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AnimatedContent(
                    targetState = progress.title,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "BackupStepTitle"
                ) { animatedTitle ->
                    Text(
                        text = animatedTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = progress.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ImportFileSelectionDialog(
    backupHistory: List<BackupHistoryEntry>,
    isInspecting: Boolean,
    onDismiss: () -> Unit,
    onBrowseFile: () -> Unit,
    onHistoryItemSelected: (BackupHistoryEntry) -> Unit,
    onRemoveHistoryEntry: (BackupHistoryEntry) -> Unit
) {
    val context = LocalContext.current
    val transitionState = remember { MutableTransitionState(false) }
    var shouldShowDialog by remember { mutableStateOf(true) }
    var onDialogHiddenAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    transitionState.targetState = shouldShowDialog

    fun closeDialog(afterClose: () -> Unit) {
        if (!shouldShowDialog) return
        onDialogHiddenAction = afterClose
        shouldShowDialog = false
    }

    LaunchedEffect(transitionState.currentState, transitionState.targetState) {
        if (!transitionState.currentState && !transitionState.targetState) {
            onDialogHiddenAction?.let { action ->
                onDialogHiddenAction = null
                action()
            }
        }
    }

    if (transitionState.currentState || transitionState.targetState) {
        Dialog(
            onDismissRequest = { closeDialog(onDismiss) },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            AnimatedVisibility(
                visibleState = transitionState,
                enter = slideInVertically(initialOffsetY = { it / 6 }) + fadeIn(animationSpec = tween(220)),
                exit = slideOutVertically(targetOffsetY = { it / 6 }) + fadeOut(animationSpec = tween(200)),
                label = "import_file_selection_dialog"
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest
                ) {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentWindowInsets = WindowInsets.systemBars,
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = stringResource(R.string.settings_import_backup_title),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 24.sp,
                                            textGeometricTransform = TextGeometricTransform(scaleX = 1.2f),
                                        ),
                                        fontFamily = GoogleSansRounded,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                navigationIcon = {
                                    FilledIconButton(
                                        modifier = Modifier.padding(start = 6.dp),
                                        onClick = { closeDialog(onDismiss) },
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = stringResource(R.string.dismiss)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                )
                            )
                        },
                        bottomBar = {
                            BottomAppBar(
                                windowInsets = WindowInsets.navigationBars,
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ExtendedFloatingActionButton(
                                        onClick = onBrowseFile,
                                        modifier = Modifier.height(48.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ) {
                                        if (isInspecting) {
                                            LoadingIndicator(modifier = Modifier.height(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.settings_inspecting),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        } else {
                                            Icon(
                                                painter = painterResource(R.drawable.rounded_upload_file_24),
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.settings_browse_file),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                                .padding(horizontal = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_import_backup_message_v2),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (backupHistory.isNotEmpty()) {
                                Text(
                                    text = stringResource(R.string.settings_recent_backups),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (backupHistory.isEmpty()) {
                                    item {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                                            shape = RoundedCornerShape(18.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(24.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Restore,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(36.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                                )
                                                Text(
                                                    text = stringResource(R.string.settings_no_recent_backups),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = stringResource(R.string.settings_no_recent_backups_desc),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    items(backupHistory, key = { it.uri }) { entry ->
                                        BackupHistoryCard(
                                            entry = entry,
                                            context = context,
                                            onSelect = { onHistoryItemSelected(entry) },
                                            onRemove = { onRemoveHistoryEntry(entry) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupHistoryCard(
    entry: BackupHistoryEntry,
    context: android.content.Context,
    onSelect: () -> Unit,
    onRemove: () -> Unit
) {
    val dateText = remember(entry.createdAt) {
        java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT).format(java.util.Date(entry.createdAt))
    }
    val sizeText = remember(entry.sizeBytes) {
        if (entry.sizeBytes > 0) Formatter.formatShortFileSize(context, entry.sizeBytes) else ""
    }
    val moduleCount = entry.modules.size

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_upload_file_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = entry.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = GoogleSansRounded
                        ),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildString {
                            append(dateText)
                            if (sizeText.isNotEmpty()) append(" · $sizeText")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.settings_remove_from_history),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_modules_schema_format, moduleCount, entry.appVersion.ifEmpty { "?" }, entry.schemaVersion),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ImportModuleSelectionDialog(
    plan: RestorePlan,
    inProgress: Boolean,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onSelectionChanged: (Set<BackupSection>) -> Unit,
    onConfirm: () -> Unit
) {
    val listState = rememberLazyListState()
    val selectedCount = plan.selectedModules.size
    val availableCount = plan.availableModules.size
    val dateText = remember(plan.manifest.createdAt) {
        java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT).format(java.util.Date(plan.manifest.createdAt))
    }
    val transitionState = remember { MutableTransitionState(false) }
    var shouldShowDialog by remember { mutableStateOf(true) }
    var onDialogHiddenAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    transitionState.targetState = shouldShowDialog

    fun closeDialog(afterClose: () -> Unit) {
        if (!shouldShowDialog) return
        onDialogHiddenAction = afterClose
        shouldShowDialog = false
    }

    LaunchedEffect(transitionState.currentState, transitionState.targetState) {
        if (!transitionState.currentState && !transitionState.targetState) {
            onDialogHiddenAction?.let { action ->
                onDialogHiddenAction = null
                action()
            }
        }
    }

    if (transitionState.currentState || transitionState.targetState) {
        Dialog(
            onDismissRequest = { closeDialog(onDismiss) },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            AnimatedVisibility(
                visibleState = transitionState,
                enter = slideInVertically(initialOffsetY = { it / 6 }) + fadeIn(animationSpec = tween(220)),
                exit = slideOutVertically(targetOffsetY = { it / 6 }) + fadeOut(animationSpec = tween(200)),
                label = "import_module_selection_dialog"
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest
                ) {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentWindowInsets = WindowInsets.systemBars,
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = stringResource(R.string.settings_restore_modules_title),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 24.sp,
                                            textGeometricTransform = TextGeometricTransform(scaleX = 1.2f),
                                        ),
                                        fontFamily = GoogleSansRounded,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                navigationIcon = {
                                    FilledIconButton(
                                        modifier = Modifier.padding(start = 6.dp),
                                        onClick = { closeDialog(onBack) },
                                        colors = IconButtonDefaults.filledIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowBack,
                                            contentDescription = stringResource(R.string.settings_back)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                )
                            )
                        },
                        bottomBar = {
                            BottomAppBar(
                                windowInsets = WindowInsets.navigationBars,
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.padding(start = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilledIconButton(
                                            onClick = { onSelectionChanged(plan.availableModules) },
                                            enabled = !inProgress,
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.round_select_all_24),
                                                contentDescription = stringResource(R.string.settings_select_all)
                                            )
                                        }
                                        FilledIconButton(
                                            onClick = { onSelectionChanged(emptySet()) },
                                            enabled = !inProgress,
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.baseline_deselect_24),
                                                contentDescription = stringResource(R.string.settings_clear_selection)
                                            )
                                        }
                                    }

                                    ExtendedFloatingActionButton(
                                        onClick = { closeDialog(onConfirm) },
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ) {
                                        if (inProgress) {
                                            LoadingIndicator(modifier = Modifier.height(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.settings_restoring),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        } else {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Restore,
                                                    contentDescription = null
                                                )
                                                Text(
                                                    text = stringResource(R.string.settings_restore_selected),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize()
                                .padding(horizontal = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Backup metadata header
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f),
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.settings_backup_details),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = stringResource(R.string.settings_backup_created),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = dateText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = stringResource(R.string.settings_backup_app_version),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = plan.manifest.appVersion.ifEmpty { stringResource(R.string.settings_unknown) },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = stringResource(R.string.settings_backup_schema),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = stringResource(R.string.settings_backup_schema_format, plan.manifest.schemaVersion),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        if (plan.manifest.deviceInfo.model.isNotBlank()) {
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(
                                                    text = stringResource(R.string.settings_backup_device),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = stringResource(R.string.settings_backup_device_format, plan.manifest.deviceInfo.manufacturer, plan.manifest.deviceInfo.model),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = stringResource(R.string.settings_modules_selected_format, selectedCount, availableCount),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    if (inProgress) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            LoadingIndicator(modifier = Modifier.height(24.dp))
                                            Text(
                                                text = "Transfer in progress...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Warnings
                            if (plan.warnings.isNotEmpty()) {
                                plan.warnings.forEach { warning ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Warning,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = warning,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }
                            }

                            // Module list
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(plan.availableModules.toList(), key = { it.key }) { section ->
                                    val isSelected = section in plan.selectedModules
                                    val detail = plan.moduleDetails[section]
                                    BackupSectionSelectableCard(
                                        section = section,
                                        selected = isSelected,
                                        enabled = !inProgress,
                                        detail = detail,
                                        onToggle = {
                                            onSelectionChanged(
                                                if (isSelected) plan.selectedModules - section else plan.selectedModules + section
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun PaletteRegenerateSongSheetContent(
    songs: List<Song>,
    isRunning: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSongClick: (Song) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_regenerate_palette_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.settings_regenerate_palette_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isRunning,
            placeholder = { Text(stringResource(R.string.settings_search_songs_placeholder)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(
                        onClick = onClearSearch,
                        enabled = !isRunning
                    ) {
                        Icon(Icons.Outlined.ClearAll, contentDescription = stringResource(R.string.settings_clear_selection))
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )

        if (isRunning) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = stringResource(R.string.settings_regenerating_palette),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 460.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (songs.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.settings_no_songs_found),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(songs, key = { it.id }) { song ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isRunning) { onSongClick(song) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song.displayArtist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (song.album.isNotBlank()) {
                                Text(
                                    text = song.album,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSubsectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsSubsection(
    title: String,
    addBottomSpace: Boolean = true,
    content: @Composable () -> Unit
) {
    SettingsSubsectionHeader(title)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Transparent),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        content()
    }
    if (addBottomSpace) {
        Spacer(modifier = Modifier.height(10.dp))
    }
}
