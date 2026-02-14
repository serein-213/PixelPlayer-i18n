package com.theveloper.pixelplay.data.backup.model

import com.theveloper.pixelplay.R

enum class BackupSection(
    val key: String,
    val label: String,
    val description: String,
    val iconRes: Int,
    val sinceVersion: Int = 1
) {
    PLAYLISTS(
        key = "playlists",
        label = "Playlists",
        description = "Your custom playlists and ordering preferences.",
        iconRes = R.drawable.rounded_playlist_play_24
    ),
    GLOBAL_SETTINGS(
        key = "global_settings",
        label = "Global Settings",
        description = "Themes, behavior, playback, and app preferences.",
        iconRes = R.drawable.rounded_settings_24
    ),
    FAVORITES(
        key = "favorites",
        label = "Favorites",
        description = "Songs marked as favorite.",
        iconRes = R.drawable.rounded_favorite_24
    ),
    LYRICS(
        key = "lyrics",
        label = "Saved Lyrics",
        description = "Lyrics you've saved or imported.",
        iconRes = R.drawable.rounded_lyrics_24
    ),
    SEARCH_HISTORY(
        key = "search_history",
        label = "Search History",
        description = "Recent search terms in the app.",
        iconRes = R.drawable.rounded_search_24
    ),
    TRANSITIONS(
        key = "transitions",
        label = "Transition Rules",
        description = "Custom transition settings between songs.",
        iconRes = R.drawable.rounded_align_justify_space_even_24
    ),
    ENGAGEMENT_STATS(
        key = "engagement_stats",
        label = "Engagement Stats",
        description = "Play count and listening duration per song.",
        iconRes = R.drawable.rounded_monitoring_24
    ),
    PLAYBACK_HISTORY(
        key = "playback_history",
        label = "Playback History",
        description = "Timeline-based listening history for stats.",
        iconRes = R.drawable.rounded_schedule_24
    ),
    QUICK_FILL(
        key = "quick_fill",
        label = "QuickFill Genres",
        description = "Custom genres and their icons.",
        iconRes = R.drawable.rounded_instant_mix_24,
        sinceVersion = 3
    ),
    ARTIST_IMAGES(
        key = "artist_images",
        label = "Artist Images",
        description = "Cached artist image URLs from Deezer.",
        iconRes = R.drawable.rounded_person_24,
        sinceVersion = 3
    ),
    EQUALIZER(
        key = "equalizer",
        label = "Equalizer Presets",
        description = "Custom equalizer presets and pinned order.",
        iconRes = R.drawable.rounded_surround_sound_24,
        sinceVersion = 3
    );
    
    fun getLabel(context: android.content.Context): String {
        val resId = when (this) {
            PLAYLISTS -> R.string.backup_section_playlists_label
            GLOBAL_SETTINGS -> R.string.backup_section_global_settings_label
            FAVORITES -> R.string.backup_section_favorites_label
            LYRICS -> R.string.backup_section_lyrics_label
            SEARCH_HISTORY -> R.string.backup_section_search_history_label
            TRANSITIONS -> R.string.backup_section_transitions_label
            ENGAGEMENT_STATS -> R.string.backup_section_engagement_stats_label
            PLAYBACK_HISTORY -> R.string.backup_section_playback_history_label
            QUICK_FILL -> R.string.backup_section_quick_fill_label
            ARTIST_IMAGES -> R.string.backup_section_artist_images_label
            EQUALIZER -> R.string.backup_section_equalizer_label
        }
        return context.getString(resId)
    }
    
    fun getDescription(context: android.content.Context): String {
        val resId = when (this) {
            PLAYLISTS -> R.string.backup_section_playlists_desc
            GLOBAL_SETTINGS -> R.string.backup_section_global_settings_desc
            FAVORITES -> R.string.backup_section_favorites_desc
            LYRICS -> R.string.backup_section_lyrics_desc
            SEARCH_HISTORY -> R.string.backup_section_search_history_desc
            TRANSITIONS -> R.string.backup_section_transitions_desc
            ENGAGEMENT_STATS -> R.string.backup_section_engagement_stats_desc
            PLAYBACK_HISTORY -> R.string.backup_section_playback_history_desc
            QUICK_FILL -> R.string.backup_section_quick_fill_desc
            ARTIST_IMAGES -> R.string.backup_section_artist_images_desc
            EQUALIZER -> R.string.backup_section_equalizer_desc
        }
        return context.getString(resId)
    }

    companion object {
        val defaultSelection: Set<BackupSection> = entries.toSet()

        fun fromKey(key: String): BackupSection? = entries.find { it.key == key }
    }
}
