@file:kotlin.OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.theveloper.pixelplay.presentation.components

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.theveloper.pixelplay.ui.theme.LocalPixelPlayDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import coil.size.Size
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.presentation.components.scoped.PlayerArtistNavigationEffect
import com.theveloper.pixelplay.presentation.components.scoped.PlayerSheetPredictiveBackHandler
import com.theveloper.pixelplay.presentation.components.scoped.QueueSheetRuntimeEffects
import com.theveloper.pixelplay.presentation.components.scoped.miniPlayerDismissHorizontalGesture
import com.theveloper.pixelplay.presentation.components.scoped.playerSheetVerticalDragGesture
import com.theveloper.pixelplay.presentation.components.scoped.rememberCastSheetState
import com.theveloper.pixelplay.presentation.components.scoped.rememberFullPlayerVisualState
import com.theveloper.pixelplay.presentation.components.scoped.rememberMiniPlayerDismissGestureHandler
import com.theveloper.pixelplay.presentation.components.scoped.rememberPrewarmFullPlayer
import com.theveloper.pixelplay.presentation.components.scoped.rememberQueueSheetState
import com.theveloper.pixelplay.presentation.components.scoped.rememberSheetBackAndDragState
import com.theveloper.pixelplay.presentation.components.scoped.rememberSheetInteractionState
import com.theveloper.pixelplay.presentation.components.scoped.rememberSheetModalOverlayController
import com.theveloper.pixelplay.presentation.components.scoped.rememberSheetOverlayState
import com.theveloper.pixelplay.presentation.components.scoped.rememberSheetActionHandlers
import com.theveloper.pixelplay.presentation.components.scoped.rememberSheetThemeState
import com.theveloper.pixelplay.presentation.components.scoped.rememberSheetVisualState
import com.theveloper.pixelplay.presentation.components.scoped.SheetMotionController
import com.theveloper.pixelplay.presentation.viewmodel.PlayerSheetState
import com.theveloper.pixelplay.presentation.viewmodel.PlayerViewModel
import com.theveloper.pixelplay.presentation.viewmodel.StablePlayerState
import com.theveloper.pixelplay.ui.theme.GoogleSansRounded
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs

internal val LocalMaterialTheme = staticCompositionLocalOf<ColorScheme> { error("No ColorScheme provided") }

val MiniPlayerHeight = 64.dp
const val ANIMATION_DURATION_MS = 255

private data class PlayerUiSheetSlice(
    val currentPlaybackQueue: kotlinx.collections.immutable.ImmutableList<Song> = persistentListOf(),
    val currentQueueSourceName: String = "",
    val preparingSongId: String? = null
)

val MiniPlayerBottomSpacer = 8.dp

@OptIn(UnstableApi::class)
@Composable
fun UnifiedPlayerSheet(
    playerViewModel: PlayerViewModel,
    sheetCollapsedTargetY: Float,
    containerHeight: Dp,
    collapsedStateHorizontalPadding: Dp = 12.dp,
    navController: NavHostController,
    hideMiniPlayer: Boolean = false,
    isNavBarHidden: Boolean = false
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        playerViewModel.toastEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val infrequentPlayerStateReference = playerViewModel.stablePlayerStateInfrequent.collectAsState()
    val infrequentPlayerState = infrequentPlayerStateReference.value

    val currentPositionState = playerViewModel.currentPlaybackPosition.collectAsState()

    val remotePositionState = playerViewModel.remotePosition.collectAsState()
    // We observe isRemotePlaybackActive directly as switching modes is a major event
    val isRemotePlaybackActive by playerViewModel.isRemotePlaybackActive.collectAsState()
    
    // Position Provider: Reads state inside the lambda to prevent recomposition of UnifiedPlayerSheet
    val positionToDisplayProvider = remember(isRemotePlaybackActive) {
        {
            if (isRemotePlaybackActive) remotePositionState.value
            else currentPositionState.value
        }
    }
    
    val isFavorite by playerViewModel.isCurrentSongFavorite.collectAsState()
    val playerUiSheetSlice by remember {
        playerViewModel.playerUiState
            .map { state ->
                PlayerUiSheetSlice(
                    currentPlaybackQueue = state.currentPlaybackQueue,
                    currentQueueSourceName = state.currentQueueSourceName,
                    preparingSongId = state.preparingSongId
                )
            }
            .distinctUntilChanged()
    }.collectAsState(initial = PlayerUiSheetSlice())

    val currentPlaybackQueue = playerUiSheetSlice.currentPlaybackQueue
    val currentQueueSourceName = playerUiSheetSlice.currentQueueSourceName
    val preparingSongId = playerUiSheetSlice.preparingSongId

    val currentSheetContentState by playerViewModel.sheetState.collectAsState()
    val predictiveBackCollapseProgress by playerViewModel.predictiveBackCollapseFraction.collectAsState()
    var predictiveBackSwipeEdge by remember { mutableStateOf<Int?>(null) }
    val prewarmFullPlayer = rememberPrewarmFullPlayer(infrequentPlayerState.currentSong?.id)

    val navBarCornerRadius by playerViewModel.navBarCornerRadius.collectAsState()
    val navBarStyle by playerViewModel.navBarStyle.collectAsState()
    val carouselStyle by playerViewModel.carouselStyle.collectAsState()
    val fullPlayerLoadingTweaks by playerViewModel.fullPlayerLoadingTweaks.collectAsState()
    val tapBackgroundClosesPlayer by playerViewModel.tapBackgroundClosesPlayer.collectAsState()
    val useSmoothCorners by playerViewModel.useSmoothCorners.collectAsState()
    val playerThemePreference by playerViewModel.playerThemePreference.collectAsState()

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()

    val offsetAnimatable = remember { Animatable(0f) }

    val screenWidthPx =
        remember(configuration, density) { with(density) { configuration.screenWidthDp.dp.toPx() } }
    val dismissThresholdPx = remember(screenWidthPx) { screenWidthPx * 0.4f }

    val swipeDismissProgress by remember(dismissThresholdPx) {
        derivedStateOf {
            if (dismissThresholdPx == 0f) 0f
            else (abs(offsetAnimatable.value) / dismissThresholdPx).coerceIn(0f, 1f)
        }
    }

    val screenHeightPx = remember(
        configuration,
        density
    ) { with(density) { configuration.screenHeightDp.dp.toPx() } }
    val miniPlayerContentHeightPx = remember { with(density) { MiniPlayerHeight.toPx() } }

    val isCastConnecting by playerViewModel.isCastConnecting.collectAsState()

    val showPlayerContentArea by remember(infrequentPlayerState.currentSong, isCastConnecting) {
        derivedStateOf { infrequentPlayerState.currentSong != null || isCastConnecting }
    }

    val playerContentExpansionFraction = playerViewModel.playerContentExpansionFraction
    val visualOvershootScaleY = remember { Animatable(1f) }
    val initialFullPlayerOffsetY = remember(density) { with(density) { 24.dp.toPx() } }
    val sheetAnimationSpec = remember {
        tween<Float>(
            durationMillis = ANIMATION_DURATION_MS,
            easing = FastOutSlowInEasing
        )
    }
    val sheetAnimationMutex = remember { MutatorMutex() }

    val sheetExpandedTargetY = 0f

    val initialY =
        if (currentSheetContentState == PlayerSheetState.COLLAPSED) sheetCollapsedTargetY else sheetExpandedTargetY
    val currentSheetTranslationY = remember { Animatable(initialY) }
    val sheetMotionController = remember(
        currentSheetTranslationY,
        playerContentExpansionFraction,
        sheetAnimationMutex,
        sheetAnimationSpec
    ) {
        SheetMotionController(
            translationY = currentSheetTranslationY,
            expansionFraction = playerContentExpansionFraction,
            mutex = sheetAnimationMutex,
            defaultAnimationSpec = sheetAnimationSpec,
            expandedY = sheetExpandedTargetY
        )
    }

    PlayerArtistNavigationEffect(
        navController = navController,
        sheetCollapsedTargetY = sheetCollapsedTargetY,
        sheetMotionController = sheetMotionController,
        playerViewModel = playerViewModel
    )

    val fullPlayerVisualState = rememberFullPlayerVisualState(
        expansionFraction = playerContentExpansionFraction.value,
        initialOffsetY = initialFullPlayerOffsetY
    )
    val fullPlayerContentAlpha = fullPlayerVisualState.contentAlpha
    val fullPlayerTranslationY = fullPlayerVisualState.translationY

    suspend fun animatePlayerSheet(
        targetExpanded: Boolean,
        animationSpec: androidx.compose.animation.core.AnimationSpec<Float> = sheetAnimationSpec,
        initialVelocity: Float = 0f
    ) {
        sheetMotionController.animateTo(
            targetExpanded = targetExpanded,
            canExpand = showPlayerContentArea,
            collapsedY = sheetCollapsedTargetY,
            animationSpec = animationSpec,
            initialVelocity = initialVelocity
        )
    }

    LaunchedEffect(sheetCollapsedTargetY) {
        sheetMotionController.syncToExpansion(sheetCollapsedTargetY)
    }

    var previousSheetState by remember { mutableStateOf(currentSheetContentState) }
    LaunchedEffect(showPlayerContentArea, currentSheetContentState) {
        val targetExpanded = showPlayerContentArea && currentSheetContentState == PlayerSheetState.EXPANDED
        val shouldBounceCollapse =
            showPlayerContentArea &&
                previousSheetState == PlayerSheetState.EXPANDED &&
                currentSheetContentState == PlayerSheetState.COLLAPSED

        previousSheetState = currentSheetContentState

        animatePlayerSheet(targetExpanded = targetExpanded)

        if (showPlayerContentArea) {
            scope.launch {
                visualOvershootScaleY.snapTo(1f)
                if (targetExpanded) {
                    visualOvershootScaleY.animateTo(
                        targetValue = 1f,
                        animationSpec = keyframes {
                            durationMillis = 50
                            1.0f at 0
                            1.05f at 125
                            1.0f at 250
                        }
                    )
                } else if (shouldBounceCollapse) {
                    visualOvershootScaleY.snapTo(0.96f)
                    visualOvershootScaleY.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                } else {
                    visualOvershootScaleY.snapTo(1f)
                }
            }
        } else {
            scope.launch { visualOvershootScaleY.snapTo(1f) }
        }
    }

    val sheetVisualState = rememberSheetVisualState(
        showPlayerContentArea = showPlayerContentArea,
        collapsedStateHorizontalPadding = collapsedStateHorizontalPadding,
        predictiveBackCollapseProgress = predictiveBackCollapseProgress,
        predictiveBackSwipeEdge = predictiveBackSwipeEdge,
        currentSheetContentState = currentSheetContentState,
        playerContentExpansionFraction = playerContentExpansionFraction,
        containerHeight = containerHeight,
        currentSheetTranslationY = currentSheetTranslationY,
        sheetCollapsedTargetY = sheetCollapsedTargetY,
        navBarStyle = navBarStyle,
        navBarCornerRadiusDp = navBarCornerRadius.dp,
        isNavBarHidden = isNavBarHidden,
        isPlaying = infrequentPlayerState.isPlaying,
        hasCurrentSong = infrequentPlayerState.currentSong != null,
        swipeDismissProgress = swipeDismissProgress
    )
    val currentBottomPadding = sheetVisualState.currentBottomPadding
    val playerContentAreaHeightDp = sheetVisualState.playerContentAreaHeightDp
    val visualSheetTranslationY = sheetVisualState.visualSheetTranslationY
    val overallSheetTopCornerRadius = sheetVisualState.overallSheetTopCornerRadius
    val playerContentActualBottomRadius = sheetVisualState.playerContentActualBottomRadius
    val currentHorizontalPaddingStart = sheetVisualState.currentHorizontalPaddingStart
    val currentHorizontalPaddingEnd = sheetVisualState.currentHorizontalPaddingEnd

    val queueSheetState = rememberQueueSheetState(
        scope = scope,
        screenHeightPx = screenHeightPx,
        density = density,
        currentBottomPadding = currentBottomPadding,
        showPlayerContentArea = showPlayerContentArea,
        currentSheetContentState = currentSheetContentState
    )
    val showQueueSheet = queueSheetState.showQueueSheet
    val allowQueueSheetInteraction = queueSheetState.allowQueueSheetInteraction
    val queueSheetOffset = queueSheetState.queueSheetOffset
    val queueSheetHeightPx = queueSheetState.queueSheetHeightPx
    val queueHiddenOffsetPx = queueSheetState.queueHiddenOffsetPx
    val queueSheetController = queueSheetState.queueSheetController
    val onQueueSheetHeightPxChange = queueSheetState.onQueueSheetHeightPxChange

    val castSheetState = rememberCastSheetState()
    val sheetBackAndDragState = rememberSheetBackAndDragState(
        showPlayerContentArea = showPlayerContentArea,
        currentSheetContentState = currentSheetContentState
    )
    val velocityTracker = remember { VelocityTracker() }
    val sheetModalOverlayController = rememberSheetModalOverlayController(
        scope = scope,
        queueSheetController = queueSheetController,
        animationDurationMs = ANIMATION_DURATION_MS,
        onCollapsePlayerSheet = { playerViewModel.collapsePlayerSheet() }
    )
    val pendingSaveQueueOverlay = sheetModalOverlayController.pendingSaveQueueOverlay
    val selectedSongForInfo = sheetModalOverlayController.selectedSongForInfo
    val sheetActionHandlers = rememberSheetActionHandlers(
        scope = scope,
        navController = navController,
        playerViewModel = playerViewModel,
        sheetMotionController = sheetMotionController,
        queueSheetController = queueSheetController,
        sheetModalOverlayController = sheetModalOverlayController,
        sheetCollapsedTargetY = sheetCollapsedTargetY
    )

    val hapticFeedback = LocalHapticFeedback.current
    val miniDismissGestureHandler = rememberMiniPlayerDismissGestureHandler(
        scope = scope,
        density = density,
        hapticFeedback = hapticFeedback,
        offsetAnimatable = offsetAnimatable,
        screenWidthPx = screenWidthPx,
        onDismissPlaylistAndShowUndo = { playerViewModel.dismissPlaylistAndShowUndo() }
    )

    QueueSheetRuntimeEffects(
        queueSheetController = queueSheetController,
        queueSheetOffset = queueSheetOffset,
        queueHiddenOffsetPx = queueHiddenOffsetPx,
        showQueueSheet = showQueueSheet,
        allowQueueSheetInteraction = allowQueueSheetInteraction,
        onTopEdgeReached = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    )

    PlayerSheetPredictiveBackHandler(
        enabled = sheetBackAndDragState.predictiveBackEnabled,
        playerViewModel = playerViewModel,
        sheetCollapsedTargetY = sheetCollapsedTargetY,
        sheetExpandedTargetY = sheetExpandedTargetY,
        sheetMotionController = sheetMotionController,
        animationDurationMs = ANIMATION_DURATION_MS,
        onSwipeEdgeChanged = { predictiveBackSwipeEdge = it }
    )

    val sheetOverlayState = rememberSheetOverlayState(
        density = density,
        showPlayerContentArea = showPlayerContentArea,
        hideMiniPlayer = hideMiniPlayer,
        currentSheetContentState = currentSheetContentState,
        hasPendingSaveQueueOverlay = pendingSaveQueueOverlay != null,
        hasSelectedSongForInfo = selectedSongForInfo != null,
        showQueueSheet = showQueueSheet,
        queueHiddenOffsetPx = queueHiddenOffsetPx,
        screenHeightPx = screenHeightPx,
        castSheetOpenFraction = castSheetState.castSheetOpenFraction
    )
    val internalIsKeyboardVisible = sheetOverlayState.internalIsKeyboardVisible
    val actuallyShowSheetContent = sheetOverlayState.actuallyShowSheetContent
    val isQueueVisible = sheetOverlayState.isQueueVisible
    val bottomSheetOpenFraction = sheetOverlayState.bottomSheetOpenFraction
    val queueScrimAlpha = sheetOverlayState.queueScrimAlpha

    val activePlayerSchemePair by playerViewModel.activePlayerColorSchemePair.collectAsState()
    val themedAlbumArtUri by playerViewModel.currentThemedAlbumArtUri.collectAsState()
    val isDarkTheme = LocalPixelPlayDarkTheme.current
    val currentSong = infrequentPlayerState.currentSong
    val sheetThemeState = rememberSheetThemeState(
        activePlayerSchemePair = activePlayerSchemePair,
        isDarkTheme = isDarkTheme,
        playerThemePreference = playerThemePreference,
        currentSong = currentSong,
        themedAlbumArtUri = themedAlbumArtUri,
        preparingSongId = preparingSongId,
        playerContentExpansionFraction = playerContentExpansionFraction,
        systemColorScheme = MaterialTheme.colorScheme
    )
    val albumColorScheme = sheetThemeState.albumColorScheme
    val miniPlayerScheme = sheetThemeState.miniPlayerScheme
    val isPreparingPlayback = sheetThemeState.isPreparingPlayback
    val miniReadyAlpha = sheetThemeState.miniReadyAlpha
    val miniAppearScale = sheetThemeState.miniAppearScale
    val playerAreaBackground = sheetThemeState.playerAreaBackground
    val effectivePlayerAreaElevation = sheetThemeState.effectivePlayerAreaElevation
    val miniAlpha = sheetThemeState.miniAlpha
    val visualCardShadowElevation by remember(
        effectivePlayerAreaElevation,
        showQueueSheet,
        playerContentExpansionFraction
    ) {
        derivedStateOf {
            // Keep rich shadow in mini/collapsing states, but drop expensive blur when full player/queue are active.
            if (showQueueSheet || playerContentExpansionFraction.value > 0.18f) 0.dp
            else effectivePlayerAreaElevation
        }
    }

    val sheetInteractionState = rememberSheetInteractionState(
        scope = scope,
        velocityTracker = velocityTracker,
        sheetMotionController = sheetMotionController,
        playerContentExpansionFraction = playerContentExpansionFraction,
        currentSheetTranslationY = currentSheetTranslationY,
        visualOvershootScaleY = visualOvershootScaleY,
        sheetCollapsedTargetY = sheetCollapsedTargetY,
        sheetExpandedTargetY = sheetExpandedTargetY,
        miniPlayerContentHeightPx = miniPlayerContentHeightPx,
        currentSheetContentState = currentSheetContentState,
        showPlayerContentArea = showPlayerContentArea,
        overallSheetTopCornerRadius = overallSheetTopCornerRadius,
        playerContentActualBottomRadius = playerContentActualBottomRadius,
        useSmoothCorners = useSmoothCorners,
        isDragging = sheetBackAndDragState.isDragging,
        onAnimateSheet = { targetExpanded, animationSpec, initialVelocity ->
            if (animationSpec == null) {
                animatePlayerSheet(targetExpanded = targetExpanded)
            } else {
                animatePlayerSheet(
                    targetExpanded = targetExpanded,
                    animationSpec = animationSpec,
                    initialVelocity = initialVelocity
                )
            }
        },
        onExpandSheetState = { playerViewModel.expandPlayerSheet() },
        onCollapseSheetState = { playerViewModel.collapsePlayerSheet() },
        onDraggingChange = sheetBackAndDragState.onDraggingChange,
        onDraggingPlayerAreaChange = sheetBackAndDragState.onDraggingPlayerAreaChange
    )

    if (actuallyShowSheetContent) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, visualSheetTranslationY.roundToInt()) }
                .height(containerHeight),
            shadowElevation = 0.dp,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = currentBottomPadding)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Use granular showDismissUndoBar and undoBarVisibleDuration
                    if (showPlayerContentArea) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .miniPlayerDismissHorizontalGesture(
                                    enabled = currentSheetContentState == PlayerSheetState.COLLAPSED,
                                    handler = miniDismissGestureHandler
                                )
                                .padding(
                                    start = currentHorizontalPaddingStart,
                                    end = currentHorizontalPaddingEnd
                                )
                                .height(playerContentAreaHeightDp)
                                .graphicsLayer {
                                    translationX = offsetAnimatable.value
                                    scaleX = miniAppearScale
                                    scaleY = visualOvershootScaleY.value * miniAppearScale
                                    alpha = miniReadyAlpha
                                    transformOrigin = TransformOrigin(0.5f, 1f)
                                }
                                .then(
                                    if (visualCardShadowElevation > 0.dp) {
                                        Modifier.shadow(
                                            elevation = visualCardShadowElevation,
                                            shape = sheetInteractionState.playerShadowShape,
                                            clip = false
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .background(
                                    color = playerAreaBackground,
                                    shape = sheetInteractionState.playerShadowShape
                                )
                                .clipToBounds()
                                .playerSheetVerticalDragGesture(
                                    enabled = sheetInteractionState.canDragSheet,
                                    handler = sheetInteractionState.sheetVerticalDragGestureHandler
                                )
                                .clickable(
                                    enabled = tapBackgroundClosesPlayer || currentSheetContentState == PlayerSheetState.COLLAPSED,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    playerViewModel.togglePlayerSheetState()
                                }
                        ) {
                            UnifiedPlayerMiniAndFullLayers(
                                currentSong = infrequentPlayerState.currentSong,
                                miniPlayerScheme = miniPlayerScheme,
                                overallSheetTopCornerRadius = overallSheetTopCornerRadius,
                                infrequentPlayerState = infrequentPlayerState,
                                isCastConnecting = isCastConnecting,
                                isPreparingPlayback = isPreparingPlayback,
                                miniAlpha = miniAlpha,
                                playerContentExpansionFraction = playerContentExpansionFraction,
                                albumColorScheme = albumColorScheme,
                                bottomSheetOpenFraction = bottomSheetOpenFraction,
                                fullPlayerContentAlpha = fullPlayerContentAlpha,
                                fullPlayerTranslationY = fullPlayerTranslationY,
                                currentPlaybackQueue = currentPlaybackQueue,
                                currentQueueSourceName = currentQueueSourceName,
                                currentSheetContentState = currentSheetContentState,
                                carouselStyle = carouselStyle,
                                fullPlayerLoadingTweaks = fullPlayerLoadingTweaks,
                                playerViewModel = playerViewModel,
                                currentPositionProvider = positionToDisplayProvider,
                                isFavorite = isFavorite,
                                onShowQueueClicked = sheetActionHandlers.openQueueSheet,
                                onQueueDragStart = sheetActionHandlers.beginQueueDrag,
                                onQueueDrag = sheetActionHandlers.dragQueueBy,
                                onQueueRelease = sheetActionHandlers.endQueueDrag,
                                onShowCastClicked = castSheetState.openCastSheet
                            )
                        }
                    }

                    // Prewarm full player once per track to reduce first-open jank.
                    UnifiedPlayerPrewarmLayer(
                        prewarmFullPlayer = prewarmFullPlayer,
                        currentSong = infrequentPlayerState.currentSong,
                        containerHeight = containerHeight,
                        albumColorScheme = albumColorScheme,
                        currentPlaybackQueue = currentPlaybackQueue,
                        currentQueueSourceName = currentQueueSourceName,
                        infrequentPlayerState = infrequentPlayerState,
                        carouselStyle = carouselStyle,
                        fullPlayerLoadingTweaks = fullPlayerLoadingTweaks,
                        playerViewModel = playerViewModel,
                        currentPositionProvider = positionToDisplayProvider,
                        isCastConnecting = isCastConnecting,
                        isFavorite = isFavorite,
                        onShowQueueClicked = sheetActionHandlers.openQueueSheet,
                        onQueueDragStart = sheetActionHandlers.beginQueueDrag,
                        onQueueDrag = sheetActionHandlers.dragQueueBy,
                        onQueueRelease = sheetActionHandlers.endQueueDrag
                    )
                }

                BackHandler(enabled = isQueueVisible && !internalIsKeyboardVisible) {
                    sheetActionHandlers.animateQueueSheet(false)
                }


                UnifiedPlayerQueueAndSongInfoHost(
                    shouldRenderHost = !internalIsKeyboardVisible || selectedSongForInfo != null,
                    isQueueTelemetryActive = showQueueSheet,
                    albumColorScheme = albumColorScheme,
                    queueScrimAlpha = queueScrimAlpha,
                    showQueueSheet = showQueueSheet,
                    queueHiddenOffsetPx = queueHiddenOffsetPx,
                    queueSheetOffset = queueSheetOffset,
                    queueSheetHeightPx = queueSheetHeightPx,
                    onQueueSheetHeightPxChange = onQueueSheetHeightPxChange,
                    configurationResetKey = configuration,
                    currentPlaybackQueue = currentPlaybackQueue,
                    currentQueueSourceName = currentQueueSourceName,
                    infrequentPlayerState = infrequentPlayerState,
                    playerViewModel = playerViewModel,
                    selectedSongForInfo = selectedSongForInfo,
                    onSelectedSongForInfoChange = sheetActionHandlers.onSelectedSongForInfoChange,
                    onAnimateQueueSheet = sheetActionHandlers.animateQueueSheet,
                    onBeginQueueDrag = sheetActionHandlers.beginQueueDrag,
                    onDragQueueBy = sheetActionHandlers.dragQueueBy,
                    onEndQueueDrag = sheetActionHandlers.endQueueDrag,
                    onLaunchSaveQueueOverlay = sheetActionHandlers.onLaunchSaveQueueOverlay,
                    onNavigateToAlbum = sheetActionHandlers.onNavigateToAlbum,
                    onNavigateToArtist = sheetActionHandlers.onNavigateToArtist
                )

            }
        }

        UnifiedPlayerCastLayer(
            showCastSheet = castSheetState.showCastSheet,
            internalIsKeyboardVisible = internalIsKeyboardVisible,
            albumColorScheme = albumColorScheme,
            playerViewModel = playerViewModel,
            onDismiss = castSheetState.dismissCastSheet,
            onExpansionChanged = castSheetState.onCastExpansionChanged
        )

        UnifiedPlayerSaveQueueLayer(
            pendingOverlay = pendingSaveQueueOverlay,
            onDismissOverlay = { sheetModalOverlayController.dismissSaveQueueOverlay() }
        )
    }
}

@Composable
private fun CastConnectingDialog() {
    BasicAlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier.padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .widthIn(min = 220.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.cast_keep_app_open),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.cast_transfering_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun getNavigationBarHeight(): Dp {
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    return insets.calculateBottomPadding()
}

@Composable
internal fun MiniPlayerContentInternal(
    song: Song,
    isPlaying: Boolean,
    isCastConnecting: Boolean,
    isPreparingPlayback: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    cornerRadiusAlb: Dp,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val controlsEnabled = !isCastConnecting && !isPreparingPlayback

    val interaction = remember { MutableInteractionSource() }
    val indication: Indication = ripple(bounded = false)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .padding(start = 10.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            SmartImage(
                model = song.albumArtUriString,
                contentDescription = stringResource(R.string.album_art_cd, song.title),
                shape = CircleShape,
                targetSize = Size(150, 150),
                modifier = Modifier.size(44.dp)
            )
            if (isCastConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = LocalMaterialTheme.current.onPrimaryContainer
                )
            } else if (isPreparingPlayback) {
                CircularWavyProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            val titleStyle = MaterialTheme.typography.titleSmall.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp,
                fontFamily = GoogleSansRounded,
                color = LocalMaterialTheme.current.onPrimaryContainer
            )
            val artistStyle = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                letterSpacing = 0.sp,
                fontFamily = GoogleSansRounded,
                color = LocalMaterialTheme.current.onPrimaryContainer.copy(alpha = 0.7f)
            )

            AutoScrollingText(
                text = when {
                    isCastConnecting -> stringResource(R.string.mini_player_connecting)
                    isPreparingPlayback -> stringResource(R.string.mini_player_preparing)
                    else -> song.title
                },
                style = titleStyle,
                gradientEdgeColor = LocalMaterialTheme.current.primaryContainer
            )
            AutoScrollingText(
                text = if (isPreparingPlayback) {
                    stringResource(R.string.mini_player_loading_audio)
                } else {
                    song.displayArtist
                },
                style = artistStyle,
                gradientEdgeColor = LocalMaterialTheme.current.primaryContainer
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(LocalMaterialTheme.current.onPrimary)
                .clickable(
                    interactionSource = interaction,
                    indication = indication,
                    enabled = controlsEnabled
                ) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPrevious()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_skip_previous_24),
                contentDescription = stringResource(R.string.mini_player_previous_cd),
                tint = LocalMaterialTheme.current.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(LocalMaterialTheme.current.primary)
                .clickable(
                    interactionSource = interaction,
                    indication = indication,
                    enabled = controlsEnabled
                ) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPlayPause()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = if (isPlaying) painterResource(R.drawable.rounded_pause_24) else painterResource(R.drawable.rounded_play_arrow_24),
                contentDescription = if (isPlaying) {
                    stringResource(R.string.mini_player_pause_cd)
                } else {
                    stringResource(R.string.mini_player_play_cd)
                },
                tint = LocalMaterialTheme.current.onPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(LocalMaterialTheme.current.onPrimary)
                .clickable(
                    interactionSource = interaction,
                    indication = indication,
                    enabled = controlsEnabled
                ) { onNext() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_skip_next_24),
                contentDescription = stringResource(R.string.mini_player_next_cd),
                tint = LocalMaterialTheme.current.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
