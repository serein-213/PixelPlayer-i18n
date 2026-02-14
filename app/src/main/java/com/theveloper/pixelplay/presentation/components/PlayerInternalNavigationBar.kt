package com.theveloper.pixelplay.presentation.components

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.theveloper.pixelplay.BottomNavItem
import com.theveloper.pixelplay.data.preferences.NavBarStyle
import com.theveloper.pixelplay.presentation.components.scoped.CustomNavigationBarItem
import com.theveloper.pixelplay.presentation.navigation.Screen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal val NavBarContentHeight = 90.dp // Altura del contenido de la barra de navegación
internal val NavBarContentHeightFullWidth = NavBarContentHeight // Altura del contenido de la barra de navegación en modo completo

@Composable
private fun PlayerInternalNavigationItemsRow(
    navController: NavHostController,
    navItems: ImmutableList<BottomNavItem>,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    navBarStyle: String,
    onSearchIconDoubleTap: () -> Unit
) {
    val navBarInsetPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val latestCurrentRoute by rememberUpdatedState(currentRoute)
    val latestOnSearchIconDoubleTap by rememberUpdatedState(onSearchIconDoubleTap)

    val rowModifier = if (navBarStyle == NavBarStyle.FULL_WIDTH) {
        modifier
            .fillMaxWidth()
            .padding(top = 0.dp, bottom = navBarInsetPadding, start = 12.dp, end = 12.dp)
    } else {
        modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
    }
    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val scope = rememberCoroutineScope()
        var lastSearchTapTimestamp by remember { mutableStateOf(0L) }
        navItems.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            val selectedColor = MaterialTheme.colorScheme.primary
            val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            val indicatorColorFromTheme = MaterialTheme.colorScheme.secondaryContainer
            val itemLabel = stringResource(item.labelResId)

            val iconPainterResId = if (isSelected && item.selectedIconResId != null && item.selectedIconResId != 0) {
                item.selectedIconResId
            } else {
                item.iconResId
            }
            val iconLambda: @Composable () -> Unit = remember(iconPainterResId, item.labelResId) {
                {
                    Icon(
                        painter = painterResource(id = iconPainterResId),
                        contentDescription = itemLabel
                    )
                }
            }
            val selectedIconLambda: @Composable () -> Unit = remember(iconPainterResId, item.labelResId) {
                {
                    Icon(
                        painter = painterResource(id = iconPainterResId),
                        contentDescription = itemLabel
                    )
                }
            }
            val labelLambda: @Composable () -> Unit = remember(item.labelResId) {
                { Text(itemLabel) }
            }
            val onClickLambda: () -> Unit = remember(item.screen.route, navController, scope) {
                {
                    val itemRoute = item.screen.route
                    val isSearchTab = itemRoute == Screen.Search.route
                    val isAlreadySelected = latestCurrentRoute == itemRoute

                    if (isSearchTab) {
                        val now = SystemClock.elapsedRealtime()
                        val isDoubleTap = now - lastSearchTapTimestamp <= 350L
                        lastSearchTapTimestamp = now

                        if (!isAlreadySelected) {
                            navController.navigate(itemRoute) {
                                popUpTo(navController.graph.id) { inclusive = true; saveState = false }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }

                        if (isDoubleTap) {
                            lastSearchTapTimestamp = 0L
                            if (isAlreadySelected) {
                                latestOnSearchIconDoubleTap()
                            } else {
                                scope.launch {
                                    delay(160L)
                                    latestOnSearchIconDoubleTap()
                                }
                            }
                        }
                    } else if (!isAlreadySelected) {
                        lastSearchTapTimestamp = 0L
                        navController.navigate(itemRoute) {
                            popUpTo(navController.graph.id) { inclusive = true; saveState = false }
                            launchSingleTop = true
                            restoreState = false
                        }
                    } else {
                        lastSearchTapTimestamp = 0L
                    }
                }
            }
            CustomNavigationBarItem(
                modifier = Modifier.weight(1f),
                selected = isSelected,
                onClick = onClickLambda,
                icon = iconLambda,
                selectedIcon = selectedIconLambda,
                label = labelLambda,
                contentDescription = itemLabel,
                alwaysShowLabel = true,
                selectedIconColor = selectedColor,
                unselectedIconColor = unselectedColor,
                selectedTextColor = selectedColor,
                unselectedTextColor = unselectedColor,
                indicatorColor = indicatorColorFromTheme
            )
        }
    }
}

@Composable
fun PlayerInternalNavigationBar(
    navController: NavHostController,
    navItems: ImmutableList<BottomNavItem>,
    currentRoute: String?,
    modifier: Modifier = Modifier,
    navBarStyle: String,
    onSearchIconDoubleTap: () -> Unit = {}
) {
    PlayerInternalNavigationItemsRow(
        navController = navController,
        navItems = navItems,
        currentRoute = currentRoute,
        navBarStyle = navBarStyle,
        onSearchIconDoubleTap = onSearchIconDoubleTap,
        modifier = modifier
    )
}
