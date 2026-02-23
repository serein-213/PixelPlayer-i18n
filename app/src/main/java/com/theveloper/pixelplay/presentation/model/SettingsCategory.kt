package com.theveloper.pixelplay.presentation.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeveloperBoard
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.ui.graphics.vector.ImageVector
import com.theveloper.pixelplay.R

enum class SettingsCategory(
    val id: String,
    @StringRes val titleResId: Int,
    @StringRes val subtitleResId: Int,
    val icon: ImageVector? = null,
    val iconRes: Int? = null
) {
    LIBRARY(
        id = "library",
        titleResId = R.string.settings_category_library_title,
        subtitleResId = R.string.settings_category_library_subtitle,
        icon = Icons.Rounded.LibraryMusic
    ),
    APPEARANCE(
        id = "appearance",
        titleResId = R.string.settings_category_appearance_title,
        subtitleResId = R.string.settings_category_appearance_subtitle,
        icon = Icons.Rounded.Palette
    ),
    PLAYBACK(
        id = "playback",
        titleResId = R.string.settings_category_playback_title,
        subtitleResId = R.string.settings_category_playback_subtitle,
        icon = Icons.Rounded.MusicNote // Using MusicNote again or maybe PlayCircle if available
    ),
    BEHAVIOR(
        id = "behavior",
        titleResId = R.string.settings_category_behavior_title,
        subtitleResId = R.string.settings_category_behavior_subtitle,
        iconRes = R.drawable.rounded_touch_app_24
    ),
    AI_INTEGRATION(
        id = "ai",
        titleResId = R.string.settings_category_ai_title,
        subtitleResId = R.string.settings_category_ai_subtitle,
        iconRes = R.drawable.gemini_ai
    ),
    BACKUP_RESTORE(
        id = "backup_restore",
        titleResId = R.string.settings_category_backup_title,
        subtitleResId = R.string.settings_category_backup_subtitle,
        iconRes = R.drawable.rounded_upload_file_24
    ),
    DEVELOPER(
        id = "developer",
        titleResId = R.string.settings_category_developer_title,
        subtitleResId = R.string.settings_category_developer_subtitle,
        icon = Icons.Rounded.DeveloperMode
    ),
    EQUALIZER(
        id = "equalizer",
        titleResId = R.string.settings_category_equalizer_title,
        subtitleResId = R.string.settings_category_equalizer_subtitle,
        icon = Icons.Rounded.GraphicEq
    ),
    DEVICE_CAPABILITIES(
        id = "device_capabilities",
        titleResId = R.string.device_capabilities_title,
        subtitleResId = R.string.device_capabilities_subtitle,
        icon = Icons.Rounded.DeveloperBoard // Placeholder, maybe Memory or SettingsInputComponent
    ),
    ABOUT(
        id = "about",
        titleResId = R.string.settings_category_about_title,
        subtitleResId = R.string.settings_category_about_subtitle,
        icon = Icons.Rounded.Info
    );

    companion object {
        fun fromId(id: String): SettingsCategory? = entries.find { it.id == id }
    }
}
