package com.theveloper.pixelplay.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

private data class ScrollMetrics(
    val progress: Float,
    val totalItemsCount: Int,
    val maxScrollIndex: Int,
    val scrollableHeight: Float
)

private fun estimateListItemStridePx(visibleItems: List<LazyListItemInfo>): Float {
    val firstVisibleItem = visibleItems.firstOrNull() ?: return 1f
    val strideSamples = visibleItems
        .zipWithNext()
        .mapNotNull { (current, next) ->
            (next.offset - current.offset)
                .takeIf { it > 0 }
                ?.toFloat()
        }

    val averageStride = strideSamples
        .takeIf { it.isNotEmpty() }
        ?.average()
        ?.toFloat()

    return (averageStride ?: firstVisibleItem.size.toFloat()).coerceAtLeast(1f)
}

private fun estimateGridRowMetrics(visibleItems: List<LazyGridItemInfo>): Pair<Int, Float> {
    val firstVisibleItem = visibleItems.firstOrNull() ?: return 1 to 1f
    val firstRowOffset = firstVisibleItem.offset.y
    val itemsInFirstRow = visibleItems.count { it.offset.y == firstRowOffset }.coerceAtLeast(1)
    val nextRowOffset = visibleItems
        .asSequence()
        .map { it.offset.y }
        .filter { it > firstRowOffset }
        .minOrNull()

    val rowStridePx = ((nextRowOffset ?: (firstRowOffset + firstVisibleItem.size.height)) - firstRowOffset)
        .toFloat()
        .coerceAtLeast(1f)

    return itemsInFirstRow to rowStridePx
}

@Composable
fun ExpressiveScrollBar(
    modifier: Modifier = Modifier,
    listState: LazyListState? = null,
    gridState: LazyGridState? = null,
    minHeight: Dp = 48.dp,
    thickness: Dp = 8.dp,
    indicatorExpandedWidth: Dp = 24.dp,
    indicatorExpandedWidthBoost: Dp = 4.dp,
    indicatorRightCornerRadius: Dp = 6.dp,
    paddingEnd: Dp = 4.dp,
    trackGap: Dp = 8.dp
) {
    var isPressed by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(-1f) }
    var pendingScrollIndex by remember { mutableIntStateOf(-1) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.secondaryContainer
    val innerIcon = Icons.Rounded.UnfoldMore
    val expandedIndicatorWidth = (indicatorExpandedWidth + indicatorExpandedWidthBoost).coerceAtLeast(thickness)
    val indicatorRightCornerRadiusPx = with(LocalDensity.current) { indicatorRightCornerRadius.toPx() }

    val isInteracting = isPressed || isDragging
    
    val animatedWidth by animateDpAsState(
        targetValue = if (isInteracting) expandedIndicatorWidth else thickness,
        animationSpec = tween(durationMillis = 200),
        label = "WidthAnimation"
    )
    
    val iconAlpha by animateFloatAsState(
        targetValue = if (isInteracting) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "IconAlpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(expandedIndicatorWidth + paddingEnd)
    ) {
        val density = LocalDensity.current
        val constraintsMaxWidth = maxWidth
        val constraintsMaxHeight = maxHeight

        val canScrollForward by remember { derivedStateOf { listState?.canScrollForward ?: gridState?.canScrollForward ?: false } }
        val canScrollBackward by remember { derivedStateOf { listState?.canScrollBackward ?: gridState?.canScrollBackward ?: false } }
        
        if (!canScrollForward && !canScrollBackward) return@BoxWithConstraints

        fun getScrollStats(): ScrollMetrics {
            val totalItemsCount: Int
            val currentScrollPx: Float
            val totalScrollableContentPx: Float
            val approximateMaxScrollIndex: Int

            if (listState != null) {
                val layoutInfo = listState.layoutInfo
                totalItemsCount = layoutInfo.totalItemsCount
                val itemStridePx = estimateListItemStridePx(layoutInfo.visibleItemsInfo)
                val viewportHeightPx =
                    (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat()
                        .coerceAtLeast(1f)
                val estimatedVisibleItems = (viewportHeightPx / itemStridePx).coerceAtLeast(1f)

                currentScrollPx =
                    (listState.firstVisibleItemIndex * itemStridePx) + listState.firstVisibleItemScrollOffset
                totalScrollableContentPx =
                    ((totalItemsCount * itemStridePx) - viewportHeightPx).coerceAtLeast(1f)
                approximateMaxScrollIndex =
                    (totalItemsCount - estimatedVisibleItems).toInt().coerceAtLeast(1)
            } else if (gridState != null) {
                val layoutInfo = gridState.layoutInfo
                totalItemsCount = layoutInfo.totalItemsCount
                val (itemsPerRow, rowStridePx) = estimateGridRowMetrics(layoutInfo.visibleItemsInfo)
                val viewportHeightPx =
                    (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat()
                        .coerceAtLeast(1f)
                val totalRows = ((totalItemsCount + itemsPerRow - 1) / itemsPerRow).coerceAtLeast(1)
                val estimatedVisibleRows = (viewportHeightPx / rowStridePx).coerceAtLeast(1f)
                val currentRow = gridState.firstVisibleItemIndex / itemsPerRow

                currentScrollPx = (currentRow * rowStridePx) + gridState.firstVisibleItemScrollOffset
                totalScrollableContentPx =
                    ((totalRows * rowStridePx) - viewportHeightPx).coerceAtLeast(1f)
                approximateMaxScrollIndex =
                    (((totalRows - estimatedVisibleRows).toInt().coerceAtLeast(1)) * itemsPerRow)
                        .coerceAtMost((totalItemsCount - 1).coerceAtLeast(1))
            } else {
                return ScrollMetrics(
                    progress = 0f,
                    totalItemsCount = 0,
                    maxScrollIndex = 1,
                    scrollableHeight = 1f
                )
            }

            if (totalItemsCount == 0) {
                return ScrollMetrics(
                    progress = 0f,
                    totalItemsCount = 0,
                    maxScrollIndex = 1,
                    scrollableHeight = 1f
                )
            }

            val forward = listState?.canScrollForward ?: gridState?.canScrollForward ?: false
            val backward = listState?.canScrollBackward ?: gridState?.canScrollBackward ?: false

            val boundedScrollPx = currentScrollPx.coerceIn(0f, totalScrollableContentPx)
            val realProgress = if (!forward && totalItemsCount > 0) {
                1f
            } else if (!backward) {
                0f
            } else {
                (boundedScrollPx / totalScrollableContentPx).coerceIn(0f, 0.999f)
            }

            val availableHeight = with(density) { constraintsMaxHeight.toPx() }
            val handleHeightPx = with(density) { minHeight.toPx() }
            val scrollableHeight = (availableHeight - handleHeightPx).coerceAtLeast(1f)

            return ScrollMetrics(
                progress = realProgress,
                totalItemsCount = totalItemsCount,
                maxScrollIndex = approximateMaxScrollIndex,
                scrollableHeight = scrollableHeight
            )
        }

        fun updateProgressFromTouch(touchY: Float, grabOffset: Float) {
            val stats = getScrollStats()
            val scrollableHeight = stats.scrollableHeight

            val targetHandleTop = touchY - grabOffset
            val newProgress = (targetHandleTop / scrollableHeight).coerceIn(0f, 1f)

            dragProgress = newProgress
            pendingScrollIndex = ((newProgress * stats.maxScrollIndex).toInt())
                .coerceIn(0, (stats.totalItemsCount - 1).coerceAtLeast(0))
        }

        LaunchedEffect(Unit) {
            snapshotFlow { pendingScrollIndex }
                .distinctUntilChanged()
                .collectLatest { index ->
                    if (index >= 0) {
                        listState?.scrollToItem(index)
                        gridState?.scrollToItem(index)
                    }
                }
        }

        val indicatorPath = remember { Path() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            try {
                                awaitRelease()
                            } finally {
                                isPressed = false
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    var grabOffset = 0f

                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true

                            val stats = getScrollStats()
                            val realProgress = stats.progress
                            val scrollableHeight = stats.scrollableHeight
                            val handleHeightPx = with(density) { minHeight.toPx() }

                            val displayProgress = if (isDragging && dragProgress >= 0f) dragProgress else realProgress
                            val handleY = displayProgress * scrollableHeight

                            val isTouchOnHandle = offset.y >= handleY && offset.y <= (handleY + handleHeightPx)

                            if (isTouchOnHandle) {
                                grabOffset = offset.y - handleY
                                dragProgress = realProgress
                            } else {
                                grabOffset = handleHeightPx / 2f
                                updateProgressFromTouch(offset.y, grabOffset)
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            dragProgress = -1f
                            pendingScrollIndex = -1
                        },
                        onDragCancel = {
                            isDragging = false
                            dragProgress = -1f
                            pendingScrollIndex = -1
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            updateProgressFromTouch(change.position.y, grabOffset)
                        }
                    )
                }
        ) {
            val rightAnchorX = with(density) { (constraintsMaxWidth - paddingEnd).toPx() }
            val trackX = rightAnchorX - with(density) { thickness.toPx() / 2 }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val stats = getScrollStats()
                val realProgress = stats.progress
                val scrollableHeight = stats.scrollableHeight

                val displayProgress = if (isDragging && dragProgress >= 0f) dragProgress else realProgress
                val handleY = displayProgress * scrollableHeight
                val handleHeightPx = minHeight.toPx()

                val trackStrokeWidth = thickness.toPx()
                val indicatorWidthPx = animatedWidth.toPx()
                val gapPx = trackGap.toPx()
                val indicatorLeftCornerRadius = indicatorWidthPx / 2f
                val maxAllowedRightCornerRadius = minOf(indicatorWidthPx / 2f, handleHeightPx / 2f)
                val resolvedRightCornerRadius = indicatorRightCornerRadiusPx
                    .coerceIn(0f, maxAllowedRightCornerRadius)

                val currentIndicatorX = rightAnchorX - indicatorWidthPx

                if (handleY > gapPx) {
                    drawLine(
                        color = surfaceVariantColor,
                        start = Offset(trackX, 0f),
                        end = Offset(trackX, handleY - gapPx),
                        strokeWidth = trackStrokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }

                if (handleY + handleHeightPx + gapPx < size.height) {
                    drawLine(
                        color = surfaceVariantColor,
                        start = Offset(trackX, handleY + handleHeightPx + gapPx),
                        end = Offset(trackX, size.height),
                        strokeWidth = trackStrokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }

                indicatorPath.reset()
                indicatorPath.addRoundRect(
                    RoundRect(
                        rect = Rect(
                            offset = Offset(currentIndicatorX, handleY),
                            size = Size(indicatorWidthPx, handleHeightPx)
                        ),
                        topLeft = CornerRadius(indicatorLeftCornerRadius, indicatorLeftCornerRadius),
                        topRight = CornerRadius(resolvedRightCornerRadius, resolvedRightCornerRadius),
                        bottomRight = CornerRadius(resolvedRightCornerRadius, resolvedRightCornerRadius),
                        bottomLeft = CornerRadius(indicatorLeftCornerRadius, indicatorLeftCornerRadius)
                    )
                )
                drawPath(
                    path = indicatorPath,
                    color = primaryColor
                )
            }
            
            if (iconAlpha > 0f) {
               Box(
                   modifier = Modifier
                       .offset {
                           val stats = getScrollStats()
                           val realProgress = stats.progress
                           val scrollableHeight = stats.scrollableHeight
                           val displayProgress = if (isDragging && dragProgress >= 0f) dragProgress else realProgress
                           val handleY = displayProgress * scrollableHeight
                           val handleHeightPx = with(density) { minHeight.toPx() }
                           
                           val iconSizePx = with(density) { 24.dp.toPx() }
                           val paddingEndPx = with(density) { paddingEnd.toPx() }
                           val animatedWidthPx = with(density) { animatedWidth.toPx() }
                           val maxWidthPx = with(density) { constraintsMaxWidth.toPx() }
                           
                           val x = maxWidthPx - paddingEndPx - (animatedWidthPx / 2) - (iconSizePx / 2)
                           val y = handleY + (handleHeightPx / 2) - (iconSizePx / 2)
                           
                           androidx.compose.ui.unit.IntOffset(x.toInt(), y.toInt())
                       }
                       .size(24.dp)
                       .graphicsLayer { 
                           alpha = iconAlpha 
                           scaleX = iconAlpha
                           scaleY = iconAlpha
                       }
               ) {
                   Icon(
                       imageVector = innerIcon,
                       contentDescription = null,
                       tint = MaterialTheme.colorScheme.onPrimary,
                       modifier = Modifier.fillMaxSize()
                   )
               }
            }
        }
    }
}
