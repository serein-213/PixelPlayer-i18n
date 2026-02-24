package com.theveloper.pixelplay.presentation.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.data.netease.NeteaseRepository
import com.theveloper.pixelplay.presentation.netease.auth.NeteaseLoginActivity
import com.theveloper.pixelplay.presentation.telegram.auth.TelegramLoginActivity
import com.theveloper.pixelplay.ui.theme.GoogleSansRounded
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

/**
 * Bottom sheet that lets the user choose between streaming providers
 * (Telegram, Google Drive, Netease Cloud Music).
 *
 * For Netease: if already logged in, navigates to dashboard.
 * If not logged in, launches WebView login activity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamingProviderSheet(
    onDismissRequest: () -> Unit,
    isNeteaseLoggedIn: Boolean = false,
    onNavigateToNeteaseDashboard: () -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
) {
    val context = LocalContext.current

    val cardShape = AbsoluteSmoothCornerShape(
        cornerRadiusTR = 20.dp, cornerRadiusTL = 20.dp,
        cornerRadiusBR = 20.dp, cornerRadiusBL = 20.dp,
        smoothnessAsPercentTR = 60, smoothnessAsPercentTL = 60,
        smoothnessAsPercentBR = 60, smoothnessAsPercentBL = 60
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.cloud_streaming_title),
                style = MaterialTheme.typography.titleLarge,
                fontFamily = GoogleSansRounded,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.cloud_streaming_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = GoogleSansRounded,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // Telegram Provider
            ProviderCard(
                iconPainter = painterResource(R.drawable.telegram),
                icon = Icons.Rounded.Cloud,
                title = stringResource(R.string.cloud_streaming_provider_telegram_title),
                subtitle = stringResource(R.string.cloud_streaming_provider_telegram_subtitle),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                iconColor = MaterialTheme.colorScheme.primaryContainer,
                shape = cardShape,
                onClick = {
                    context.startActivity(Intent(context, TelegramLoginActivity::class.java))
                    onDismissRequest()
                }
            )

            Spacer(Modifier.height(12.dp))

            // Google Drive Provider (coming soon)
            ProviderCard(
                icon = Icons.Rounded.CloudQueue,
                iconPainter = painterResource(R.drawable.rounded_drive_export_24),
                title = stringResource(R.string.cloud_streaming_provider_gdrive_title),
                subtitle = stringResource(R.string.cloud_streaming_provider_gdrive_subtitle_coming_soon),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                iconColor = MaterialTheme.colorScheme.onSurface,
                shape = cardShape,
                enabled = false,
                onClick = { }
            )

            Spacer(Modifier.height(12.dp))

            // Netease Cloud Music Provider
            ProviderCard(
                icon = Icons.Rounded.MusicNote,
                iconPainter = painterResource(R.drawable.netease_cloud_music_logo_icon_206716__1_),
                title = stringResource(R.string.cloud_streaming_provider_netease_title),
                subtitle = if (isNeteaseLoggedIn) {
                    stringResource(R.string.cloud_streaming_provider_netease_subtitle_connected)
                } else {
                    stringResource(R.string.cloud_streaming_provider_netease_subtitle_sign_in)
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                iconColor = MaterialTheme.colorScheme.tertiaryContainer,
                shape = cardShape,
                onClick = {
                    if (isNeteaseLoggedIn) {
                        onNavigateToNeteaseDashboard()
                    } else {
                        context.startActivity(Intent(context, NeteaseLoginActivity::class.java))
                    }
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
private fun ProviderCard(
    icon: ImageVector,
    iconPainter: Painter? = null,
    title: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    iconColor: Color,
    shape: AbsoluteSmoothCornerShape,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.62f)
            .clip(shape = shape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(contentColor),
                contentAlignment = Alignment.Center
            ) {
                if (iconPainter != null){
                    Icon(
                        painter = iconPainter,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconColor
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconColor
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = GoogleSansRounded,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = GoogleSansRounded,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}
