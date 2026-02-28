package com.theveloper.pixelplay.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey // Added import
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.common.Player
import com.theveloper.pixelplay.data.model.Playlist
import com.theveloper.pixelplay.data.model.SortOption // Added import
import com.theveloper.pixelplay.data.model.FolderSource
import com.theveloper.pixelplay.data.model.LyricsSourcePreference
import com.theveloper.pixelplay.data.model.TransitionSettings
import com.theveloper.pixelplay.data.equalizer.EqualizerPreset // Added import
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.text.get
import kotlin.text.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object ThemePreference {
    const val DEFAULT = "default"
    const val DYNAMIC = "dynamic"
    const val ALBUM_ART = "album_art"
    const val GLOBAL = "global"
}

object AppThemeMode {
    const val FOLLOW_SYSTEM = "follow_system"
    const val LIGHT = "light"
    const val DARK = "dark"
}

/**
 * Album art quality settings for developer options.
 * Controls maximum resolution for album artwork in player view.
 * Thumbnails in lists always use low resolution for performance.
 * 
 * @property maxSize Maximum size in pixels (0 = original size)
 * @property label Human-readable label for UI
 */
enum class AlbumArtQuality(val maxSize: Int, val label: String) {
    LOW(256, "Low (256px) - Better performance"),
    MEDIUM(512, "Medium (512px) - Balanced"),
    HIGH(800, "High (800px) - Best quality"),
    ORIGINAL(0, "Original - Maximum quality")
}

@Singleton
class UserPreferencesRepository
@Inject
constructor(
        private val dataStore: DataStore<Preferences>,
        private val json: Json // Inyectar Json para serialización
) {

    private object PreferencesKeys {
        val APP_REBRAND_DIALOG_SHOWN = booleanPreferencesKey("app_rebrand_dialog_shown")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val GEMINI_SYSTEM_PROMPT = stringPreferencesKey("gemini_system_prompt")
        val ALLOWED_DIRECTORIES = stringSetPreferencesKey("allowed_directories")
        val BLOCKED_DIRECTORIES = stringSetPreferencesKey("blocked_directories")
        val INITIAL_SETUP_DONE = booleanPreferencesKey("initial_setup_done")
        // val GLOBAL_THEME_PREFERENCE = stringPreferencesKey("global_theme_preference_v2") //
        // Removed
        val PLAYER_THEME_PREFERENCE = stringPreferencesKey("player_theme_preference_v2")
        val ALBUM_ART_PALETTE_STYLE = stringPreferencesKey("album_art_palette_style_v1")
        val APP_THEME_MODE = stringPreferencesKey("app_theme_mode")
        val FAVORITE_SONG_IDS = stringSetPreferencesKey("favorite_song_ids")
        val USER_PLAYLISTS = stringPreferencesKey("user_playlists_json_v1")
        val PLAYLIST_SONG_ORDER_MODES = stringPreferencesKey("playlist_song_order_modes")

        // Sort Option Keys
        val SONGS_SORT_OPTION = stringPreferencesKey("songs_sort_option")
        val SONGS_SORT_OPTION_MIGRATED = booleanPreferencesKey("songs_sort_option_migrated_v2")
        val ALBUMS_SORT_OPTION = stringPreferencesKey("albums_sort_option")
        val ARTISTS_SORT_OPTION = stringPreferencesKey("artists_sort_option")
        val PLAYLISTS_SORT_OPTION = stringPreferencesKey("playlists_sort_option")
        val FOLDERS_SORT_OPTION = stringPreferencesKey("folders_sort_option")
        val LIKED_SONGS_SORT_OPTION = stringPreferencesKey("liked_songs_sort_option")

        // UI State Keys
        val LAST_LIBRARY_TAB_INDEX =
                intPreferencesKey("last_library_tab_index") // Corrected: Add intPreferencesKey here
        val MOCK_GENRES_ENABLED = booleanPreferencesKey("mock_genres_enabled")
        val LAST_DAILY_MIX_UPDATE = longPreferencesKey("last_daily_mix_update")
        val DAILY_MIX_SONG_IDS = stringPreferencesKey("daily_mix_song_ids")
        val YOUR_MIX_SONG_IDS = stringPreferencesKey("your_mix_song_ids")
        val NAV_BAR_CORNER_RADIUS = intPreferencesKey("nav_bar_corner_radius")
        val NAV_BAR_STYLE = stringPreferencesKey("nav_bar_style")
        val CAROUSEL_STYLE = stringPreferencesKey("carousel_style")
        val LIBRARY_NAVIGATION_MODE = stringPreferencesKey("library_navigation_mode")
        val LAUNCH_TAB = stringPreferencesKey("launch_tab")

        // Transition Settings
        val GLOBAL_TRANSITION_SETTINGS = stringPreferencesKey("global_transition_settings_json")
        val LIBRARY_TABS_ORDER = stringPreferencesKey("library_tabs_order")
        val IS_FOLDER_FILTER_ACTIVE = booleanPreferencesKey("is_folder_filter_active")
        val IS_FOLDERS_PLAYLIST_VIEW = booleanPreferencesKey("is_folders_playlist_view")
        val FOLDERS_SOURCE = stringPreferencesKey("folders_source")
        val FOLDER_BACK_GESTURE_NAVIGATION = booleanPreferencesKey("folder_back_gesture_navigation")
        val USE_SMOOTH_CORNERS = booleanPreferencesKey("use_smooth_corners")
        val KEEP_PLAYING_IN_BACKGROUND = booleanPreferencesKey("keep_playing_in_background")
        val IS_CROSSFADE_ENABLED = booleanPreferencesKey("is_crossfade_enabled")
        val CROSSFADE_DURATION = intPreferencesKey("crossfade_duration")
        val CUSTOM_GENRES = androidx.datastore.preferences.core.stringSetPreferencesKey("custom_genres")
        val CUSTOM_GENRE_ICONS = stringPreferencesKey("custom_genre_icons") // JSON Map<String, Int>
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val IS_SHUFFLE_ON = booleanPreferencesKey("is_shuffle_on")
        val PERSISTENT_SHUFFLE_ENABLED = booleanPreferencesKey("persistent_shuffle_enabled")
        val DISABLE_CAST_AUTOPLAY = booleanPreferencesKey("disable_cast_autoplay")
        val SHOW_QUEUE_HISTORY = booleanPreferencesKey("show_queue_history")
        val FULL_PLAYER_SHOW_FILE_INFO = booleanPreferencesKey("full_player_show_file_info")
        val FULL_PLAYER_DELAY_ALL = booleanPreferencesKey("full_player_delay_all")
        val FULL_PLAYER_DELAY_ALBUM = booleanPreferencesKey("full_player_delay_album")
        val FULL_PLAYER_DELAY_METADATA = booleanPreferencesKey("full_player_delay_metadata")
        val FULL_PLAYER_DELAY_PROGRESS = booleanPreferencesKey("full_player_delay_progress")
        val FULL_PLAYER_DELAY_CONTROLS = booleanPreferencesKey("full_player_delay_controls")
        val FULL_PLAYER_PLACEHOLDERS = booleanPreferencesKey("full_player_placeholders")
        val FULL_PLAYER_PLACEHOLDER_TRANSPARENT = booleanPreferencesKey("full_player_placeholder_transparent")
        val FULL_PLAYER_PLACEHOLDERS_ON_CLOSE = booleanPreferencesKey("full_player_placeholders_on_close")
        val FULL_PLAYER_SWITCH_ON_DRAG_RELEASE = booleanPreferencesKey("full_player_switch_on_drag_release")
        val FULL_PLAYER_DELAY_THRESHOLD = intPreferencesKey("full_player_delay_threshold_percent")
        val FULL_PLAYER_CLOSE_THRESHOLD = intPreferencesKey("full_player_close_threshold_percent")
        val USE_PLAYER_SHEET_V2 = booleanPreferencesKey("use_player_sheet_v2")

        // Multi-Artist Settings
        val ARTIST_DELIMITERS = stringPreferencesKey("artist_delimiters")
        val GROUP_BY_ALBUM_ARTIST = booleanPreferencesKey("group_by_album_artist")
        val ARTIST_SETTINGS_RESCAN_REQUIRED =
                booleanPreferencesKey("artist_settings_rescan_required")

        // Equalizer Settings
        val EQUALIZER_ENABLED = booleanPreferencesKey("equalizer_enabled")
        val EQUALIZER_PRESET = stringPreferencesKey("equalizer_preset")
        val EQUALIZER_CUSTOM_BANDS = stringPreferencesKey("equalizer_custom_bands")
        val BASS_BOOST_STRENGTH = intPreferencesKey("bass_boost_strength")
        val VIRTUALIZER_STRENGTH = intPreferencesKey("virtualizer_strength")
        val BASS_BOOST_ENABLED = booleanPreferencesKey("bass_boost_enabled")
        val VIRTUALIZER_ENABLED = booleanPreferencesKey("virtualizer_enabled")
        val LOUDNESS_ENHANCER_ENABLED = booleanPreferencesKey("loudness_enhancer_enabled")
        val LOUDNESS_ENHANCER_STRENGTH = intPreferencesKey("loudness_enhancer_strength")
        
        // Dismissed Warning States
        val BASS_BOOST_DISMISSED = booleanPreferencesKey("bass_boost_dismissed")
        val VIRTUALIZER_DISMISSED = booleanPreferencesKey("virtualizer_dismissed")
        val LOUDNESS_DISMISSED = booleanPreferencesKey("loudness_dismissed")
        val BACKUP_INFO_DISMISSED = booleanPreferencesKey("backup_info_dismissed")
        
        // View Mode
        // val IS_GRAPH_VIEW = booleanPreferencesKey("is_graph_view") // Deprecated
        val VIEW_MODE = stringPreferencesKey("equalizer_view_mode")

        // Custom Presets
        val CUSTOM_PRESETS = stringPreferencesKey("custom_presets_json") // List<EqualizerPreset>
        val PINNED_PRESETS = stringPreferencesKey("pinned_presets_json") // List<String> (names)
        
        // Library Sync
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        val DIRECTORY_RULES_VERSION = intPreferencesKey("directory_rules_version")
        val LAST_APPLIED_DIRECTORY_RULES_VERSION =
            intPreferencesKey("last_applied_directory_rules_version")
        
        // Lyrics Sync Offset per song (Map<songId, offsetMs> as JSON)
        val LYRICS_SYNC_OFFSETS = stringPreferencesKey("lyrics_sync_offsets_json")
        
        // Lyrics Source Preference
        val LYRICS_SOURCE_PREFERENCE = stringPreferencesKey("lyrics_source_preference")
        val AUTO_SCAN_LRC_FILES = booleanPreferencesKey("auto_scan_lrc_files")
        
        // Developer Options
        val ALBUM_ART_QUALITY = stringPreferencesKey("album_art_quality")
        val TAP_BACKGROUND_CLOSES_PLAYER = booleanPreferencesKey("tap_background_closes_player")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val IMMERSIVE_LYRICS_ENABLED = booleanPreferencesKey("immersive_lyrics_enabled")
        val IMMERSIVE_LYRICS_TIMEOUT = longPreferencesKey("immersive_lyrics_timeout")
        val USE_ANIMATED_LYRICS = booleanPreferencesKey("use_animated_lyrics")
        
        // Genre View Preference
        val IS_GENRE_GRID_VIEW = booleanPreferencesKey("is_genre_grid_view")
        
        // Album View Preference
        val IS_ALBUMS_LIST_VIEW = booleanPreferencesKey("is_albums_list_view")

        // Collage Pattern
        val COLLAGE_PATTERN = stringPreferencesKey("collage_pattern")
        val COLLAGE_AUTO_ROTATE = booleanPreferencesKey("collage_auto_rotate")

        // Quick Settings / Last Playlist
        val LAST_PLAYLIST_ID = stringPreferencesKey("last_playlist_id")
        val LAST_PLAYLIST_NAME = stringPreferencesKey("last_playlist_name")

        // Smart Duration Filtering
        val MIN_SONG_DURATION = intPreferencesKey("min_song_duration_ms")

        // ReplayGain
        val REPLAYGAIN_ENABLED = booleanPreferencesKey("replaygain_enabled")
        val REPLAYGAIN_USE_ALBUM_GAIN = booleanPreferencesKey("replaygain_use_album_gain")
        
        // AI Provider Settings
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val DEEPSEEK_API_KEY = stringPreferencesKey("deepseek_api_key")
        val DEEPSEEK_MODEL = stringPreferencesKey("deepseek_model")
        val DEEPSEEK_SYSTEM_PROMPT = stringPreferencesKey("deepseek_system_prompt")
    }

    val appRebrandDialogShownFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.APP_REBRAND_DIALOG_SHOWN] ?: false
            }

    suspend fun setAppRebrandDialogShown(wasShown: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_REBRAND_DIALOG_SHOWN] = wasShown
        }
    }

    val isCrossfadeEnabledFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.IS_CROSSFADE_ENABLED] ?: false
            }

    suspend fun setCrossfadeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_CROSSFADE_ENABLED] = enabled
        }
    }

    // Effects Settings
    val bassBoostEnabledFlow: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.BASS_BOOST_ENABLED] ?: false
        }

    suspend fun setBassBoostEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.BASS_BOOST_ENABLED] = enabled }
    }

    val virtualizerEnabledFlow: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.VIRTUALIZER_ENABLED] ?: false
        }

    suspend fun setVirtualizerEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.VIRTUALIZER_ENABLED] = enabled }
    }

    val loudnessEnhancerEnabledFlow: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LOUDNESS_ENHANCER_ENABLED] ?: false
        }

    val loudnessEnhancerStrengthFlow: Flow<Int> =
        dataStore.data.map { preferences ->
            (preferences[PreferencesKeys.LOUDNESS_ENHANCER_STRENGTH] ?: 0).coerceIn(0, 1000)
        }

    suspend fun setLoudnessEnhancerEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.LOUDNESS_ENHANCER_ENABLED] = enabled }
    }

    suspend fun setLoudnessEnhancerStrength(strength: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOUDNESS_ENHANCER_STRENGTH] = strength.coerceIn(0, 1000)
        }
    }

    // Dismissed Warning Flows & Setters
    val bassBoostDismissedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BASS_BOOST_DISMISSED] ?: false
    }

    suspend fun setBassBoostDismissed(dismissed: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.BASS_BOOST_DISMISSED] = dismissed }
    }

    val virtualizerDismissedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.VIRTUALIZER_DISMISSED] ?: false
    }

    suspend fun setVirtualizerDismissed(dismissed: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.VIRTUALIZER_DISMISSED] = dismissed }
    }

    val loudnessDismissedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LOUDNESS_DISMISSED] ?: false
    }

    suspend fun setLoudnessDismissed(dismissed: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.LOUDNESS_DISMISSED] = dismissed }
    }

    val backupInfoDismissedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.BACKUP_INFO_DISMISSED] ?: false
    }

    suspend fun setBackupInfoDismissed(dismissed: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.BACKUP_INFO_DISMISSED] = dismissed }
    }

    enum class EqualizerViewMode {
        SLIDERS, GRAPH, HYBRID
    }

    val equalizerViewModeFlow: Flow<EqualizerViewMode> = dataStore.data.map { preferences ->
        val modeString = preferences[PreferencesKeys.VIEW_MODE]
        if (modeString != null) {
            try {
                EqualizerViewMode.valueOf(modeString)
            } catch (e: Exception) {
                EqualizerViewMode.SLIDERS
            }
        } else {
            // Migration: Check legacy boolean
            val isGraph = preferences[booleanPreferencesKey("is_graph_view")] ?: false
            if (isGraph) EqualizerViewMode.GRAPH else EqualizerViewMode.SLIDERS
        }
    }

    suspend fun setEqualizerViewMode(mode: EqualizerViewMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIEW_MODE] = mode.name
        }
    }

    val crossfadeDurationFlow: Flow<Int> =
            dataStore.data.map { preferences ->
                (preferences[PreferencesKeys.CROSSFADE_DURATION] ?: 2000).coerceIn(1000, 12000)
            }

    suspend fun setCrossfadeDuration(duration: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CROSSFADE_DURATION] = duration.coerceIn(1000, 12000)
        }
    }

    // Custom Genres Names
    val customGenresFlow: Flow<Set<String>> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.CUSTOM_GENRES] ?: emptySet()
        }

    // Custom Genres Icons (JSON Map: Name -> ResId)
    val customGenreIconsFlow: Flow<Map<String, Int>> =
        dataStore.data.map { preferences ->
            val jsonString = preferences[PreferencesKeys.CUSTOM_GENRE_ICONS]
            if (jsonString != null) {
                try {
                    json.decodeFromString<Map<String, Int>>(jsonString)
                } catch (e: Exception) {
                    emptyMap()
                }
            } else {
                emptyMap()
            }
        }

    suspend fun addCustomGenre(genre: String, iconResId: Int? = null) {
        dataStore.edit { preferences ->
            val currentGenres = preferences[PreferencesKeys.CUSTOM_GENRES] ?: emptySet()
            preferences[PreferencesKeys.CUSTOM_GENRES] = currentGenres + genre
            
            if (iconResId != null) {
                val currentIconsJson = preferences[PreferencesKeys.CUSTOM_GENRE_ICONS]
                val currentIcons = if (currentIconsJson != null) {
                    try {
                        json.decodeFromString<Map<String, Int>>(currentIconsJson)
                    } catch (e: Exception) {
                        emptyMap()
                    }
                } else {
                    emptyMap()
                }
                
                val newIcons = currentIcons.toMutableMap()
                newIcons[genre] = iconResId
                preferences[PreferencesKeys.CUSTOM_GENRE_ICONS] = json.encodeToString(newIcons)
            }
        }
    }
    val repeatModeFlow: Flow<Int> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.REPEAT_MODE] ?: Player.REPEAT_MODE_OFF
            }

    suspend fun setRepeatMode(@Player.RepeatMode mode: Int) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.REPEAT_MODE] = mode }
    }

    val isShuffleOnFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.IS_SHUFFLE_ON] ?: false
            }

    suspend fun setShuffleOn(on: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.IS_SHUFFLE_ON] = on }
    }

    val persistentShuffleEnabledFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.PERSISTENT_SHUFFLE_ENABLED] ?: false
            }

    suspend fun setPersistentShuffleEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.PERSISTENT_SHUFFLE_ENABLED] = enabled }
    }

    // ===== Multi-Artist Settings =====

    val artistDelimitersFlow: Flow<List<String>> =
            dataStore.data.map { preferences ->
                val stored = preferences[PreferencesKeys.ARTIST_DELIMITERS]
                if (stored != null) {
                    try {
                        json.decodeFromString<List<String>>(stored)
                    } catch (e: Exception) {
                        DEFAULT_ARTIST_DELIMITERS
                    }
                } else {
                    DEFAULT_ARTIST_DELIMITERS
                }
            }

    suspend fun setArtistDelimiters(delimiters: List<String>) {
        // Ensure at least one delimiter is always maintained
        if (delimiters.isEmpty()) {
            return
        }

        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ARTIST_DELIMITERS] = json.encodeToString(delimiters)
            // Mark rescan as required when delimiters change
            preferences[PreferencesKeys.ARTIST_SETTINGS_RESCAN_REQUIRED] = true
        }
    }

    suspend fun resetArtistDelimitersToDefault() {
        setArtistDelimiters(DEFAULT_ARTIST_DELIMITERS)
    }

    val groupByAlbumArtistFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.GROUP_BY_ALBUM_ARTIST] ?: false
            }

    suspend fun setGroupByAlbumArtist(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GROUP_BY_ALBUM_ARTIST] = enabled
            // Mark rescan as required when this setting changes
            preferences[PreferencesKeys.ARTIST_SETTINGS_RESCAN_REQUIRED] = true
        }
    }

    val artistSettingsRescanRequiredFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.ARTIST_SETTINGS_RESCAN_REQUIRED] ?: false
            }

    suspend fun clearArtistSettingsRescanRequired() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ARTIST_SETTINGS_RESCAN_REQUIRED] = false
        }
    }

    // ===== Library Sync Settings =====
    
    val lastSyncTimestampFlow: Flow<Long> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] ?: 0L
            }

    val directoryRulesVersionFlow: Flow<Int> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.DIRECTORY_RULES_VERSION] ?: 0
            }

    val lastAppliedDirectoryRulesVersionFlow: Flow<Int> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.LAST_APPLIED_DIRECTORY_RULES_VERSION] ?: 0
            }

    suspend fun getLastSyncTimestamp(): Long {
        return lastSyncTimestampFlow.first()
    }

    suspend fun getDirectoryRulesVersion(): Int {
        return directoryRulesVersionFlow.first()
    }

    suspend fun getLastAppliedDirectoryRulesVersion(): Int {
        return lastAppliedDirectoryRulesVersionFlow.first()
    }

    suspend fun setLastSyncTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] = timestamp
        }
    }

    suspend fun markDirectoryRulesVersionApplied(version: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_APPLIED_DIRECTORY_RULES_VERSION] = version
        }
    }

    // ===== End Library Sync Settings =====

    // ===== Lyrics Sync Offset Settings =====
    
    /**
     * Lyrics sync offset per song in milliseconds.
     * Stored as a JSON map: { "songId": offsetMs, ... }
     * Positive values = lyrics appear later (use when lyrics are ahead of audio)
     * Negative values = lyrics appear earlier (use when lyrics are behind audio)
     */
    private val lyricsSyncOffsetsFlow: Flow<Map<String, Int>> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.LYRICS_SYNC_OFFSETS]?.let { jsonString ->
                    try {
                        json.decodeFromString<Map<String, Int>>(jsonString)
                    } catch (e: Exception) {
                        emptyMap()
                    }
                } ?: emptyMap()
            }

    fun getLyricsSyncOffsetFlow(songId: String): Flow<Int> {
        return lyricsSyncOffsetsFlow.map { offsets -> offsets[songId] ?: 0 }
    }

    suspend fun getLyricsSyncOffset(songId: String): Int {
        return getLyricsSyncOffsetFlow(songId).first()
    }

    suspend fun setLyricsSyncOffset(songId: String, offsetMs: Int) {
        dataStore.edit { preferences ->
            val currentOffsets = preferences[PreferencesKeys.LYRICS_SYNC_OFFSETS]?.let { jsonString ->
                try {
                    json.decodeFromString<Map<String, Int>>(jsonString).toMutableMap()
                } catch (e: Exception) {
                    mutableMapOf()
                }
            } ?: mutableMapOf()
            
            if (offsetMs == 0) {
                currentOffsets.remove(songId) // Don't store default value
            } else {
                currentOffsets[songId] = offsetMs
            }
            
            preferences[PreferencesKeys.LYRICS_SYNC_OFFSETS] = json.encodeToString(currentOffsets)
        }
    }

    // ===== End Lyrics Sync Offset Settings =====

    // ===== Lyrics Source Preference Settings =====
    
    val lyricsSourcePreferenceFlow: Flow<LyricsSourcePreference> =
            dataStore.data.map { preferences ->
                LyricsSourcePreference.fromName(preferences[PreferencesKeys.LYRICS_SOURCE_PREFERENCE])
            }

    suspend fun setLyricsSourcePreference(preference: LyricsSourcePreference) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LYRICS_SOURCE_PREFERENCE] = preference.name
        }
    }

    val autoScanLrcFilesFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.AUTO_SCAN_LRC_FILES] ?: false
            }

    suspend fun setAutoScanLrcFiles(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SCAN_LRC_FILES] = enabled
        }
    }

    val immersiveLyricsEnabledFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.IMMERSIVE_LYRICS_ENABLED] ?: false
            }

    val immersiveLyricsTimeoutFlow: Flow<Long> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.IMMERSIVE_LYRICS_TIMEOUT] ?: 4000L
            }

    suspend fun setImmersiveLyricsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IMMERSIVE_LYRICS_ENABLED] = enabled
        }
    }

    suspend fun setImmersiveLyricsTimeout(timeout: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IMMERSIVE_LYRICS_TIMEOUT] = timeout
        }
    }

    // ===== End Lyrics Source Preference Settings =====

    // ===== End Multi-Artist Settings =====

    val globalTransitionSettingsFlow: Flow<TransitionSettings> =
            dataStore.data.map { preferences ->
                val duration = (preferences[PreferencesKeys.CROSSFADE_DURATION] ?: 2000).coerceIn(1000, 12000)
                val settings =
                        preferences[PreferencesKeys.GLOBAL_TRANSITION_SETTINGS]?.let { jsonString ->
                            try {
                                json.decodeFromString<TransitionSettings>(jsonString)
                            } catch (e: Exception) {
                                TransitionSettings() // Return default on error
                            }
                        }
                                ?: TransitionSettings() // Return default if not set

                settings.copy(durationMs = duration)
            }

    suspend fun saveGlobalTransitionSettings(settings: TransitionSettings) {
        dataStore.edit { preferences ->
            val jsonString = json.encodeToString(settings)
            preferences[PreferencesKeys.GLOBAL_TRANSITION_SETTINGS] = jsonString
        }
    }

    val dailyMixSongIdsFlow: Flow<List<String>> =
            dataStore.data.map { preferences ->
                val jsonString = preferences[PreferencesKeys.DAILY_MIX_SONG_IDS]
                if (jsonString != null) {
                    try {
                        json.decodeFromString<List<String>>(jsonString)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
            }

    suspend fun saveDailyMixSongIds(songIds: List<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_MIX_SONG_IDS] = json.encodeToString(songIds)
        }
    }

    val yourMixSongIdsFlow: Flow<List<String>> =
            dataStore.data.map { preferences ->
                val jsonString = preferences[PreferencesKeys.YOUR_MIX_SONG_IDS]
                if (jsonString != null) {
                    try {
                        json.decodeFromString<List<String>>(jsonString)
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
            }

    suspend fun saveYourMixSongIds(songIds: List<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.YOUR_MIX_SONG_IDS] = json.encodeToString(songIds)
        }
    }

    val isGenreGridViewFlow: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.IS_GENRE_GRID_VIEW] ?: true // Default to Grid (true)
        }

    suspend fun setGenreGridView(isGrid: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_GENRE_GRID_VIEW] = isGrid
        }
    }

    val isAlbumsListViewFlow: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.IS_ALBUMS_LIST_VIEW] ?: false // Default to Grid (false)
        }

    suspend fun setAlbumsListView(isList: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ALBUMS_LIST_VIEW] = isList
        }
    }

    val lastDailyMixUpdateFlow: Flow<Long> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.LAST_DAILY_MIX_UPDATE] ?: 0L
            }

    suspend fun saveLastDailyMixUpdateTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_DAILY_MIX_UPDATE] = timestamp
        }
    }

    // ===== Smart Duration Filtering =====

    /** Minimum song duration in milliseconds. Default 10000ms (10 seconds). */
    val minSongDurationFlow: Flow<Int> =
        dataStore.data.map { preferences ->
            (preferences[PreferencesKeys.MIN_SONG_DURATION] ?: 10000).coerceIn(0, 120000)
        }

    suspend fun setMinSongDuration(durationMs: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MIN_SONG_DURATION] = durationMs.coerceIn(0, 120000)
        }
    }

    suspend fun getMinSongDuration(): Int {
        return minSongDurationFlow.first()
    }

    // ===== End Smart Duration Filtering =====

    // ===== ReplayGain =====

    val replayGainEnabledFlow: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.REPLAYGAIN_ENABLED] ?: false
        }

    val replayGainUseAlbumGainFlow: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.REPLAYGAIN_USE_ALBUM_GAIN] ?: false
        }

    suspend fun setReplayGainEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REPLAYGAIN_ENABLED] = enabled
        }
    }

    suspend fun setReplayGainUseAlbumGain(useAlbumGain: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REPLAYGAIN_USE_ALBUM_GAIN] = useAlbumGain
        }
    }

    // ===== End ReplayGain =====

    val allowedDirectoriesFlow: Flow<Set<String>> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.ALLOWED_DIRECTORIES] ?: emptySet()
            }

    val blockedDirectoriesFlow: Flow<Set<String>> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.BLOCKED_DIRECTORIES] ?: emptySet()
            }

    val initialSetupDoneFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.INITIAL_SETUP_DONE] ?: false
            }

    val playerThemePreferenceFlow: Flow<String> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.PLAYER_THEME_PREFERENCE]
                        ?: ThemePreference.ALBUM_ART // Default to Album Art
            }

    val albumArtPaletteStyleFlow: Flow<AlbumArtPaletteStyle> =
            dataStore.data.map { preferences ->
                AlbumArtPaletteStyle.fromStorageKey(
                    preferences[PreferencesKeys.ALBUM_ART_PALETTE_STYLE]
                )
            }

    val appThemeModeFlow: Flow<String> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.APP_THEME_MODE] ?: AppThemeMode.FOLLOW_SYSTEM
            }

    val keepPlayingInBackgroundFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.KEEP_PLAYING_IN_BACKGROUND] ?: true
            }

    val disableCastAutoplayFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.DISABLE_CAST_AUTOPLAY] ?: false
            }

    val showQueueHistoryFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.SHOW_QUEUE_HISTORY] ?: false  // Default to false for performance
            }

    suspend fun setShowQueueHistory(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_QUEUE_HISTORY] = show
        }
    }

    val showPlayerFileInfoFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.FULL_PLAYER_SHOW_FILE_INFO] ?: true
            }

    suspend fun setShowPlayerFileInfo(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULL_PLAYER_SHOW_FILE_INFO] = show
        }
    }

    val fullPlayerLoadingTweaksFlow: Flow<FullPlayerLoadingTweaks> = dataStore.data
        .map { preferences ->
            val delayAlbum = preferences[PreferencesKeys.FULL_PLAYER_DELAY_ALBUM] ?: true
            val delayMetadata = preferences[PreferencesKeys.FULL_PLAYER_DELAY_METADATA] ?: true
            val delayProgress = preferences[PreferencesKeys.FULL_PLAYER_DELAY_PROGRESS] ?: true
            val delayControls = preferences[PreferencesKeys.FULL_PLAYER_DELAY_CONTROLS] ?: true
            
            val delayAll = delayAlbum && delayMetadata && delayProgress && delayControls

            FullPlayerLoadingTweaks(
                delayAll = delayAll,
                delayAlbumCarousel = delayAlbum,
                delaySongMetadata = delayMetadata,
                delayProgressBar = delayProgress,
                delayControls = delayControls,
                showPlaceholders = preferences[PreferencesKeys.FULL_PLAYER_PLACEHOLDERS] ?: true,
                transparentPlaceholders = preferences[PreferencesKeys.FULL_PLAYER_PLACEHOLDER_TRANSPARENT] ?: false,
                applyPlaceholdersOnClose = preferences[PreferencesKeys.FULL_PLAYER_PLACEHOLDERS_ON_CLOSE] ?: false,
                switchOnDragRelease = preferences[PreferencesKeys.FULL_PLAYER_SWITCH_ON_DRAG_RELEASE] ?: true,
                contentAppearThresholdPercent = preferences[PreferencesKeys.FULL_PLAYER_DELAY_THRESHOLD] ?: 98,
                contentCloseThresholdPercent = preferences[PreferencesKeys.FULL_PLAYER_CLOSE_THRESHOLD] ?: 0
            )
        }

    val usePlayerSheetV2Flow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_PLAYER_SHEET_V2] ?: true
        }

    val favoriteSongIdsFlow: Flow<Set<String>> =
            dataStore.data // Nuevo flujo para favoritos
                    .map { preferences ->
                preferences[PreferencesKeys.FAVORITE_SONG_IDS] ?: emptySet()
            }

    val playlistSongOrderModesFlow: Flow<Map<String, String>> =
            dataStore.data.map { preferences ->
                val serializedModes = preferences[PreferencesKeys.PLAYLIST_SONG_ORDER_MODES]
                if (serializedModes.isNullOrBlank()) {
                    emptyMap()
                } else {
                    runCatching { json.decodeFromString<Map<String, String>>(serializedModes) }
                            .getOrDefault(emptyMap())
                }
            }

    val userPlaylistsFlow: Flow<List<Playlist>> =
            dataStore.data.map { preferences ->
                val jsonString = preferences[PreferencesKeys.USER_PLAYLISTS]
                if (jsonString != null) {
                    try {
                        json.decodeFromString<List<Playlist>>(jsonString)
                    } catch (e: Exception) {
                        // Error al deserializar, devolver lista vacía o manejar error
                        emptyList()
                    }
                } else {
                    emptyList()
                }
            }

    private suspend fun savePlaylists(playlists: List<Playlist>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_PLAYLISTS] = json.encodeToString(playlists)
        }
    }

    suspend fun createPlaylist(
            name: String,
            songIds: List<String> = emptyList(),
            isAiGenerated: Boolean = false,
            isQueueGenerated: Boolean = false,
            coverImageUri: String? = null,
            coverColorArgb: Int? = null,
            coverIconName: String? = null,
            coverShapeType: String? = null,
            coverShapeDetail1: Float? = null,
            coverShapeDetail2: Float? = null,
            coverShapeDetail3: Float? = null,
            coverShapeDetail4: Float? = null,
            customId: String? = null,  // Support custom ID for NetEase sync de-duplication
            source: String = "LOCAL"   // Source tag
    ): Playlist {
        val currentPlaylists = userPlaylistsFlow.first().toMutableList()
        val newPlaylist =
                Playlist(
                        id = customId ?: UUID.randomUUID().toString(),  // Use custom ID or UUID
                        name = name,
                        songIds = songIds,
                        isAiGenerated = isAiGenerated,
                        isQueueGenerated = isQueueGenerated,
                        coverImageUri = coverImageUri,
                        coverColorArgb = coverColorArgb,
                        coverIconName = coverIconName,
                        coverShapeType = coverShapeType,
                        coverShapeDetail1 = coverShapeDetail1,
                        coverShapeDetail2 = coverShapeDetail2,
                        coverShapeDetail3 = coverShapeDetail3,
                        coverShapeDetail4 = coverShapeDetail4,
                        source = source // Set source
                )
        currentPlaylists.add(newPlaylist)
        savePlaylists(currentPlaylists)
        return newPlaylist
    }

    suspend fun deletePlaylist(playlistId: String) {
        val currentPlaylists = userPlaylistsFlow.first().toMutableList()
        currentPlaylists.removeAll { it.id == playlistId }
        savePlaylists(currentPlaylists)
        clearPlaylistSongOrderMode(playlistId)
    }

    suspend fun renamePlaylist(playlistId: String, newName: String) {
        val currentPlaylists = userPlaylistsFlow.first().toMutableList()
        val index = currentPlaylists.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            currentPlaylists[index] =
                    currentPlaylists[index].copy(
                            name = newName,
                            lastModified = System.currentTimeMillis()
                    )
            savePlaylists(currentPlaylists)
        }
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        val currentPlaylists = userPlaylistsFlow.first().toMutableList()
        val index = currentPlaylists.indexOfFirst { it.id == playlist.id }
        if (index != -1) {
            currentPlaylists[index] = playlist.copy(lastModified = System.currentTimeMillis())
            savePlaylists(currentPlaylists)
        }
    }

    suspend fun addSongsToPlaylist(playlistId: String, songIdsToAdd: List<String>) {
        val currentPlaylists = userPlaylistsFlow.first().toMutableList()
        val index = currentPlaylists.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val playlist = currentPlaylists[index]
            // Evitar duplicados, añadir solo los nuevos
            val newSongIds = (playlist.songIds + songIdsToAdd).distinct()
            currentPlaylists[index] =
                    playlist.copy(songIds = newSongIds, lastModified = System.currentTimeMillis())
            savePlaylists(currentPlaylists)
        }
    }

    /*
     * @param playlistIds playlistIds Ids of playlists to add the song to
     * will remove song from the playlists which are not in playlistIds
     * */
    suspend fun addOrRemoveSongFromPlaylists(
            songId: String,
            playlistIds: List<String>
    ): MutableList<String> {
        val currentPlaylists = userPlaylistsFlow.first().toMutableList()
        val removedPlaylistIds = mutableListOf<String>()

        // adding to playlist if not already in
        playlistIds.forEach { playlistId ->
            val index = currentPlaylists.indexOfFirst { it.id == playlistId }
            if (index != -1) {
                val playlist = currentPlaylists[index]
                if (playlist.songIds.contains(songId)) return@forEach
                else {
                    val newSongIds = (playlist.songIds + songId).distinct()
                    currentPlaylists[index] =
                            playlist.copy(
                                    songIds = newSongIds,
                                    lastModified = System.currentTimeMillis()
                            )
                    savePlaylists(currentPlaylists)
                }
            }
        }

        // removing from playlist if not in playlistIds
        currentPlaylists.forEach { playlist ->
            if (playlist.songIds.contains(songId) && !playlistIds.contains(playlist.id)) {
                removeSongFromPlaylist(playlist.id, songId)
                removedPlaylistIds.add(playlist.id)
            }
        }
        return removedPlaylistIds
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songIdToRemove: String) {
        val currentPlaylists = userPlaylistsFlow.first().toMutableList()
        val index = currentPlaylists.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val playlist = currentPlaylists[index]
            currentPlaylists[index] =
                    playlist.copy(
                            songIds = playlist.songIds.filterNot { it == songIdToRemove },
                            lastModified = System.currentTimeMillis()
                    )
            savePlaylists(currentPlaylists)
        }
    }

    suspend fun removeSongFromAllPlaylists(songId: String) {
        val currentPlaylists = userPlaylistsFlow.first().toMutableList()
        var updated = false

        // Iterate through all playlists and remove the song
        currentPlaylists.forEachIndexed { index, playlist ->
            if (playlist.songIds.contains(songId)) {
                currentPlaylists[index] =
                        playlist.copy(
                                songIds = playlist.songIds.filterNot { it == songId },
                                lastModified = System.currentTimeMillis()
                        )
                updated = true
            }
        }

        if (updated) {
            savePlaylists(currentPlaylists)
        }
    }

    suspend fun reorderSongsInPlaylist(playlistId: String, newSongOrderIds: List<String>) {
        val currentPlaylists = userPlaylistsFlow.first().toMutableList()
        val index = currentPlaylists.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            currentPlaylists[index] =
                    currentPlaylists[index].copy(
                            songIds = newSongOrderIds,
                            lastModified = System.currentTimeMillis()
                    )
            savePlaylists(currentPlaylists)
        }
    }

    suspend fun setPlaylistSongOrderMode(playlistId: String, modeValue: String) {
        dataStore.edit { preferences ->
            val existingModes =
                    preferences[PreferencesKeys.PLAYLIST_SONG_ORDER_MODES]?.let { raw ->
                        runCatching { json.decodeFromString<Map<String, String>>(raw) }
                                .getOrDefault(emptyMap())
                    }
                            ?: emptyMap()

            val updated = existingModes.toMutableMap()
            updated[playlistId] = modeValue

            preferences[PreferencesKeys.PLAYLIST_SONG_ORDER_MODES] = json.encodeToString(updated)
        }
    }

    suspend fun clearPlaylistSongOrderMode(playlistId: String) {
        dataStore.edit { preferences ->
            val existingModes =
                    preferences[PreferencesKeys.PLAYLIST_SONG_ORDER_MODES]?.let { raw ->
                        runCatching { json.decodeFromString<Map<String, String>>(raw) }
                                .getOrDefault(emptyMap())
                    }
                            ?: emptyMap()

            if (!existingModes.containsKey(playlistId)) return@edit

            val updated = existingModes.toMutableMap()
            updated.remove(playlistId)

            preferences[PreferencesKeys.PLAYLIST_SONG_ORDER_MODES] = json.encodeToString(updated)
        }
    }

    suspend fun updateAllowedDirectories(allowedPaths: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALLOWED_DIRECTORIES] = allowedPaths
            // Directory rules changed: force next sync to fetch full library again.
            preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] = 0L
            val currentVersion = preferences[PreferencesKeys.DIRECTORY_RULES_VERSION] ?: 0
            preferences[PreferencesKeys.DIRECTORY_RULES_VERSION] =
                if (currentVersion == Int.MAX_VALUE) 0 else currentVersion + 1
        }
    }

    suspend fun updateDirectorySelections(allowedPaths: Set<String>, blockedPaths: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALLOWED_DIRECTORIES] = allowedPaths
            preferences[PreferencesKeys.BLOCKED_DIRECTORIES] = blockedPaths
            // Directory rules changed: force next sync to fetch full library again.
            preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] = 0L
            val currentVersion = preferences[PreferencesKeys.DIRECTORY_RULES_VERSION] ?: 0
            preferences[PreferencesKeys.DIRECTORY_RULES_VERSION] =
                if (currentVersion == Int.MAX_VALUE) 0 else currentVersion + 1
        }
    }

    suspend fun setPlayerThemePreference(themeMode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYER_THEME_PREFERENCE] = themeMode
        }
    }

    suspend fun setAlbumArtPaletteStyle(style: AlbumArtPaletteStyle) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALBUM_ART_PALETTE_STYLE] = style.storageKey
        }
    }

    suspend fun setAppThemeMode(themeMode: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.APP_THEME_MODE] = themeMode }
    }

    suspend fun toggleFavoriteSong(
            songId: String,
            removing: Boolean = false
    ) { // Nueva función para favoritos
        dataStore.edit { preferences ->
            val currentFavorites = preferences[PreferencesKeys.FAVORITE_SONG_IDS] ?: emptySet()
            val contains = currentFavorites.contains(songId)

            if (contains) preferences[PreferencesKeys.FAVORITE_SONG_IDS] = currentFavorites - songId
            else {
                if (removing)
                        preferences[PreferencesKeys.FAVORITE_SONG_IDS] = currentFavorites - songId
                else preferences[PreferencesKeys.FAVORITE_SONG_IDS] = currentFavorites + songId
            }
        }
    }

    suspend fun setFavoriteSong(songId: String, isFavorite: Boolean) {
        dataStore.edit { preferences ->
            val currentFavorites = preferences[PreferencesKeys.FAVORITE_SONG_IDS] ?: emptySet()
            preferences[PreferencesKeys.FAVORITE_SONG_IDS] = if (isFavorite) {
                currentFavorites + songId
            } else {
                currentFavorites - songId
            }
        }
    }

    suspend fun setInitialSetupDone(isDone: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.INITIAL_SETUP_DONE] = isDone }
    }

    // Flows for Sort Options
    val songsSortOptionFlow: Flow<String> =
            dataStore.data.map { preferences ->
                SortOption.fromStorageKey(
                                preferences[PreferencesKeys.SONGS_SORT_OPTION],
                                SortOption.SONGS,
                                SortOption.SongTitleAZ
                        )
                        .storageKey
            }

    val albumsSortOptionFlow: Flow<String> =
            dataStore.data.map { preferences ->
                SortOption.fromStorageKey(
                                preferences[PreferencesKeys.ALBUMS_SORT_OPTION],
                                SortOption.ALBUMS,
                                SortOption.AlbumTitleAZ
                        )
                        .storageKey
            }

    val artistsSortOptionFlow: Flow<String> =
            dataStore.data.map { preferences ->
                SortOption.fromStorageKey(
                                preferences[PreferencesKeys.ARTISTS_SORT_OPTION],
                                SortOption.ARTISTS,
                                SortOption.ArtistNameAZ
                        )
                        .storageKey
            }

    val playlistsSortOptionFlow: Flow<String> =
            dataStore.data.map { preferences ->
                SortOption.fromStorageKey(
                                preferences[PreferencesKeys.PLAYLISTS_SORT_OPTION],
                                SortOption.PLAYLISTS,
                                SortOption.PlaylistNameAZ
                        )
                        .storageKey
            }

    val foldersSortOptionFlow: Flow<String> =
            dataStore.data.map { preferences ->
                SortOption.fromStorageKey(
                                preferences[PreferencesKeys.FOLDERS_SORT_OPTION],
                                SortOption.FOLDERS,
                                SortOption.FolderNameAZ
                        )
                        .storageKey
            }

    val likedSongsSortOptionFlow: Flow<String> =
            dataStore.data.map { preferences ->
                SortOption.fromStorageKey(
                                preferences[PreferencesKeys.LIKED_SONGS_SORT_OPTION],
                                SortOption.LIKED,
                                SortOption.LikedSongDateLiked
                        )
                        .storageKey
            }

    // Functions to update Sort Options
    suspend fun setSongsSortOption(optionKey: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SONGS_SORT_OPTION] = optionKey
            preferences[PreferencesKeys.SONGS_SORT_OPTION_MIGRATED] = true
        }
    }

    suspend fun setAlbumsSortOption(optionKey: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALBUMS_SORT_OPTION] = optionKey
        }
    }

    suspend fun setArtistsSortOption(optionKey: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ARTISTS_SORT_OPTION] = optionKey
        }
    }

    suspend fun setPlaylistsSortOption(optionKey: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PLAYLISTS_SORT_OPTION] = optionKey
        }
    }

    suspend fun setFoldersSortOption(optionKey: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FOLDERS_SORT_OPTION] = optionKey
        }
    }

    suspend fun setLikedSongsSortOption(optionKey: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LIKED_SONGS_SORT_OPTION] = optionKey
        }
    }

    suspend fun ensureLibrarySortDefaults() {
        dataStore.edit { preferences ->
            val songsMigrated = preferences[PreferencesKeys.SONGS_SORT_OPTION_MIGRATED] ?: false
            val rawSongSort = preferences[PreferencesKeys.SONGS_SORT_OPTION]
            val resolvedSongSort =
                    SortOption.fromStorageKey(rawSongSort, SortOption.SONGS, SortOption.SongTitleAZ)
            val shouldForceSongDefault =
                    !songsMigrated &&
                            (rawSongSort.isNullOrBlank() ||
                                    rawSongSort == SortOption.SongTitleZA.storageKey ||
                                    rawSongSort == SortOption.SongTitleZA.displayName)

            preferences[PreferencesKeys.SONGS_SORT_OPTION] =
                    if (shouldForceSongDefault) {
                        SortOption.SongTitleAZ.storageKey
                    } else {
                        resolvedSongSort.storageKey
                    }
            if (!songsMigrated) {
                preferences[PreferencesKeys.SONGS_SORT_OPTION_MIGRATED] = true
            }

            migrateSortPreference(
                    preferences,
                    PreferencesKeys.SONGS_SORT_OPTION,
                    SortOption.SONGS,
                    SortOption.SongTitleAZ
            )
            migrateSortPreference(
                    preferences,
                    PreferencesKeys.ALBUMS_SORT_OPTION,
                    SortOption.ALBUMS,
                    SortOption.AlbumTitleAZ
            )
            migrateSortPreference(
                    preferences,
                    PreferencesKeys.ARTISTS_SORT_OPTION,
                    SortOption.ARTISTS,
                    SortOption.ArtistNameAZ
            )
            migrateSortPreference(
                    preferences,
                    PreferencesKeys.PLAYLISTS_SORT_OPTION,
                    SortOption.PLAYLISTS,
                    SortOption.PlaylistNameAZ
            )
            migrateSortPreference(
                    preferences,
                    PreferencesKeys.FOLDERS_SORT_OPTION,
                    SortOption.FOLDERS,
                    SortOption.FolderNameAZ
            )
            migrateSortPreference(
                    preferences,
                    PreferencesKeys.LIKED_SONGS_SORT_OPTION,
                    SortOption.LIKED,
                    SortOption.LikedSongDateLiked
            )
        }
    }

    private fun migrateSortPreference(
            preferences: MutablePreferences,
            key: Preferences.Key<String>,
            allowed: Collection<SortOption>,
            fallback: SortOption
    ) {
        val resolved = SortOption.fromStorageKey(preferences[key], allowed, fallback)
        if (preferences[key] != resolved.storageKey) {
            preferences[key] = resolved.storageKey
        }
    }

    // --- Library UI State ---
    val lastLibraryTabIndexFlow: Flow<Int> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.LAST_LIBRARY_TAB_INDEX] ?: 0 // Default to 0 (Songs tab)
            }

    suspend fun saveLastLibraryTabIndex(tabIndex: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_LIBRARY_TAB_INDEX] = tabIndex
        }
    }

    val mockGenresEnabledFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.MOCK_GENRES_ENABLED] ?: false // Default to false
            }

    suspend fun setMockGenresEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.MOCK_GENRES_ENABLED] = enabled }
    }

    val geminiApiKey: Flow<String> =
            dataStore.data.map { preferences -> preferences[PreferencesKeys.GEMINI_API_KEY] ?: "" }

    suspend fun setGeminiApiKey(apiKey: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.GEMINI_API_KEY] = apiKey }
    }

    val geminiModel: Flow<String> =
            dataStore.data.map { preferences -> preferences[PreferencesKeys.GEMINI_MODEL] ?: "" }

    suspend fun setGeminiModel(model: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.GEMINI_MODEL] = model }
    }

    companion object {
        const val DEFAULT_SYSTEM_PROMPT =
                "You are a helpful AI assistant integrated into a music player app. You help users create perfect playlists based on their request."
        const val DEFAULT_DEEPSEEK_SYSTEM_PROMPT =
            "You are a helpful AI assistant integrated into a music player app. You help users create perfect playlists based on their request."

        /** Default delimiters for splitting multi-artist tags */
        val DEFAULT_ARTIST_DELIMITERS = listOf("/", ";", ",", "+", "&")
    }

    val geminiSystemPrompt: Flow<String> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.GEMINI_SYSTEM_PROMPT] ?: DEFAULT_SYSTEM_PROMPT
            }

    suspend fun setGeminiSystemPrompt(prompt: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.GEMINI_SYSTEM_PROMPT] = prompt }
    }

    suspend fun resetGeminiSystemPrompt() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GEMINI_SYSTEM_PROMPT] = DEFAULT_SYSTEM_PROMPT
        }
    }

    // AI Provider Settings
    val aiProvider: Flow<String> =
        dataStore.data.map { preferences -> 
            preferences[PreferencesKeys.AI_PROVIDER] ?: "GEMINI"
        }
    
    suspend fun setAiProvider(provider: String) {
        dataStore.edit { preferences -> 
            preferences[PreferencesKeys.AI_PROVIDER] = provider 
        }
    }
    
    val deepseekApiKey: Flow<String> =
        dataStore.data.map { preferences -> 
            preferences[PreferencesKeys.DEEPSEEK_API_KEY] ?: "" 
        }
    
    suspend fun setDeepseekApiKey(apiKey: String) {
        dataStore.edit { preferences -> 
            preferences[PreferencesKeys.DEEPSEEK_API_KEY] = apiKey 
        }
    }
    
    val deepseekModel: Flow<String> =
        dataStore.data.map { preferences -> 
            preferences[PreferencesKeys.DEEPSEEK_MODEL] ?: "" 
        }
    
    suspend fun setDeepseekModel(model: String) {
        dataStore.edit { preferences -> 
            preferences[PreferencesKeys.DEEPSEEK_MODEL] = model 
        }
    }

    val deepseekSystemPrompt: Flow<String> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.DEEPSEEK_SYSTEM_PROMPT] ?: DEFAULT_DEEPSEEK_SYSTEM_PROMPT
        }

    suspend fun setDeepseekSystemPrompt(prompt: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEEPSEEK_SYSTEM_PROMPT] = prompt
        }
    }

    suspend fun resetDeepseekSystemPrompt() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEEPSEEK_SYSTEM_PROMPT] = DEFAULT_DEEPSEEK_SYSTEM_PROMPT
        }
    }

    val navBarCornerRadiusFlow: Flow<Int> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.NAV_BAR_CORNER_RADIUS] ?: 32
            }

    suspend fun setNavBarCornerRadius(radius: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NAV_BAR_CORNER_RADIUS] = radius
        }
    }

    val navBarStyleFlow: Flow<String> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.NAV_BAR_STYLE] ?: NavBarStyle.DEFAULT
            }

    suspend fun setNavBarStyle(style: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.NAV_BAR_STYLE] = style }
    }

    val libraryNavigationModeFlow: Flow<String> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.LIBRARY_NAVIGATION_MODE]
                        ?: LibraryNavigationMode.TAB_ROW
            }

    suspend fun setLibraryNavigationMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LIBRARY_NAVIGATION_MODE] = mode
        }
    }

    val carouselStyleFlow: Flow<String> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.CAROUSEL_STYLE] ?: CarouselStyle.NO_PEEK
            }

    suspend fun setCarouselStyle(style: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.CAROUSEL_STYLE] = style }
    }

    val launchTabFlow: Flow<String> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.LAUNCH_TAB] ?: LaunchTab.HOME
            }

    suspend fun setLaunchTab(tab: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.LAUNCH_TAB] = tab }
    }

    suspend fun setKeepPlayingInBackground(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEEP_PLAYING_IN_BACKGROUND] = enabled
        }
    }

    suspend fun setDisableCastAutoplay(disabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISABLE_CAST_AUTOPLAY] = disabled
        }
    }

    suspend fun setDelayAllFullPlayerContent(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULL_PLAYER_DELAY_ALL] = enabled
            
            preferences[PreferencesKeys.FULL_PLAYER_DELAY_ALBUM] = enabled
            preferences[PreferencesKeys.FULL_PLAYER_DELAY_METADATA] = enabled
            preferences[PreferencesKeys.FULL_PLAYER_DELAY_PROGRESS] = enabled
            preferences[PreferencesKeys.FULL_PLAYER_DELAY_CONTROLS] = enabled
        }
    }

    suspend fun setDelayAlbumCarousel(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULL_PLAYER_DELAY_ALBUM] = enabled
        }
    }

    suspend fun setDelaySongMetadata(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULL_PLAYER_DELAY_METADATA] = enabled
        }
    }

    suspend fun setDelayProgressBar(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULL_PLAYER_DELAY_PROGRESS] = enabled
        }
    }

    suspend fun setDelayControls(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULL_PLAYER_DELAY_CONTROLS] = enabled
        }
    }

    suspend fun setFullPlayerPlaceholders(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULL_PLAYER_PLACEHOLDERS] = enabled
            if (!enabled) {
                preferences[PreferencesKeys.FULL_PLAYER_PLACEHOLDER_TRANSPARENT] = false
            }
        }
    }

    suspend fun setTransparentPlaceholders(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULL_PLAYER_PLACEHOLDER_TRANSPARENT] = enabled
        }
    }

    suspend fun setFullPlayerPlaceholdersOnClose(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULL_PLAYER_PLACEHOLDERS_ON_CLOSE] = enabled
        }
    }

    suspend fun setFullPlayerSwitchOnDragRelease(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULL_PLAYER_SWITCH_ON_DRAG_RELEASE] = enabled
        }
    }

    suspend fun setFullPlayerAppearThreshold(thresholdPercent: Int) {
        val coercedValue = thresholdPercent.coerceIn(0, 100)
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULL_PLAYER_DELAY_THRESHOLD] = coercedValue
        }
    }

    suspend fun setFullPlayerCloseThreshold(thresholdPercent: Int) {
        val coercedValue = thresholdPercent.coerceIn(0, 100)
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FULL_PLAYER_CLOSE_THRESHOLD] = coercedValue
        }
    }

    suspend fun setUsePlayerSheetV2(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_PLAYER_SHEET_V2] = enabled
        }
    }

    val useAnimatedLyricsFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_ANIMATED_LYRICS] ?: false
        }

    suspend fun setUseAnimatedLyrics(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_ANIMATED_LYRICS] = enabled
        }
    }

    val libraryTabsOrderFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LIBRARY_TABS_ORDER]
        }

    suspend fun saveLibraryTabsOrder(order: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.LIBRARY_TABS_ORDER] = order }
    }

    suspend fun resetLibraryTabsOrder() {
        dataStore.edit { preferences -> preferences.remove(PreferencesKeys.LIBRARY_TABS_ORDER) }
    }

    suspend fun migrateTabOrder() {
        dataStore.edit { preferences ->
            val orderJson = preferences[PreferencesKeys.LIBRARY_TABS_ORDER]
            if (orderJson != null) {
                try {
                    val order = json.decodeFromString<MutableList<String>>(orderJson)
                    if (!order.contains("FOLDERS")) {
                        val likedIndex = order.indexOf("LIKED")
                        if (likedIndex != -1) {
                            order.add(likedIndex + 1, "FOLDERS")
                        } else {
                            order.add("FOLDERS") // Fallback
                        }
                        preferences[PreferencesKeys.LIBRARY_TABS_ORDER] = json.encodeToString(order)
                    }
                } catch (e: Exception) {
                    // Si la deserialización falla, no hacemos nada para evitar sobrescribir los
                    // datos del usuario.
                }
            }
            // Si orderJson es nulo, significa que el usuario nunca ha reordenado,
            // por lo que se utilizará el orden predeterminado que ya incluye FOLDERS.
        }
    }

    val isFolderFilterActiveFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.IS_FOLDER_FILTER_ACTIVE] ?: false
            }

    suspend fun setFolderFilterActive(isActive: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FOLDER_FILTER_ACTIVE] = isActive
        }
    }

    val isFoldersPlaylistViewFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_FOLDERS_PLAYLIST_VIEW] ?: false
        }

    val foldersSourceFlow: Flow<FolderSource> = dataStore.data
        .map { preferences ->
            FolderSource.fromStorageKey(preferences[PreferencesKeys.FOLDERS_SOURCE])
        }

    val folderBackGestureNavigationFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.FOLDER_BACK_GESTURE_NAVIGATION] ?: false
        }

    val useSmoothCornersFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_SMOOTH_CORNERS] ?: true
        }

    suspend fun setUseSmoothCorners(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_SMOOTH_CORNERS] = enabled
        }
    }

    suspend fun setFoldersPlaylistView(isPlaylistView: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FOLDERS_PLAYLIST_VIEW] = isPlaylistView
        }
    }

    suspend fun setFoldersSource(source: FolderSource) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FOLDERS_SOURCE] = source.storageKey
        }
    }

    suspend fun setFolderBackGestureNavigation(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FOLDER_BACK_GESTURE_NAVIGATION] = enabled
        }
    }

    // ===== Equalizer Settings =====

    val equalizerEnabledFlow: Flow<Boolean> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.EQUALIZER_ENABLED] ?: false
            }

    suspend fun setEqualizerEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EQUALIZER_ENABLED] = enabled
        }
    }

    val equalizerPresetFlow: Flow<String> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.EQUALIZER_PRESET] ?: "flat"
            }

    suspend fun setEqualizerPreset(preset: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EQUALIZER_PRESET] = preset
        }
    }

    val equalizerCustomBandsFlow: Flow<List<Int>> =
            dataStore.data.map { preferences ->
                val stored = preferences[PreferencesKeys.EQUALIZER_CUSTOM_BANDS]
                if (stored != null) {
                    try {
                        val decoded = json.decodeFromString<List<Int>>(stored)
                        when {
                            decoded.size >= 10 -> decoded.take(10)
                            decoded.isEmpty() -> List(10) { 0 }
                            else -> decoded + List(10 - decoded.size) { 0 }
                        }
                    } catch (e: Exception) {
                        List(10) { 0 }
                    }
                } else {
                    List(10) { 0 }
                }
            }

    suspend fun setEqualizerCustomBands(bands: List<Int>) {
        dataStore.edit { preferences ->
            val normalized = when {
                bands.size >= 10 -> bands.take(10)
                bands.isEmpty() -> List(10) { 0 }
                else -> bands + List(10 - bands.size) { 0 }
            }
            preferences[PreferencesKeys.EQUALIZER_CUSTOM_BANDS] = json.encodeToString(normalized)
        }
    }

    val bassBoostStrengthFlow: Flow<Int> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.BASS_BOOST_STRENGTH] ?: 0
            }

    suspend fun setBassBoostStrength(strength: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BASS_BOOST_STRENGTH] = strength.coerceIn(0, 1000)
        }
    }

    val virtualizerStrengthFlow: Flow<Int> =
            dataStore.data.map { preferences ->
                preferences[PreferencesKeys.VIRTUALIZER_STRENGTH] ?: 0
            }

    suspend fun setVirtualizerStrength(strength: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIRTUALIZER_STRENGTH] = strength.coerceIn(0, 1000)
        }
    }

    // ===== End Equalizer Settings =====
    // ===== Custom Presets Persistence =====

    val customPresetsFlow: Flow<List<EqualizerPreset>> =
        dataStore.data.map { preferences ->
            val jsonString = preferences[PreferencesKeys.CUSTOM_PRESETS]
            if (jsonString != null) {
                try {
                    json.decodeFromString<List<EqualizerPreset>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
        
    suspend fun saveCustomPreset(preset: EqualizerPreset) {
        val current = customPresetsFlow.first().toMutableList()
        // Remove existing if overwriting (by name)
        current.removeAll { it.name == preset.name }
        current.add(preset)
        
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_PRESETS] = json.encodeToString(current)
        }
    }
    
    suspend fun deleteCustomPreset(presetName: String) {
        val current = customPresetsFlow.first().toMutableList()
        current.removeAll { it.name == presetName }
        
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_PRESETS] = json.encodeToString(current)
        }
        
        // Also remove from pinned if present
        val pinned = pinnedPresetsFlow.first().toMutableList()
        if (pinned.remove(presetName)) {
            setPinnedPresets(pinned)
        }
    }
    
    suspend fun renameCustomPreset(oldName: String, newName: String) {
        val current = customPresetsFlow.first().toMutableList()
        val index = current.indexOfFirst { it.name == oldName }
        if (index == -1) return
        
        current[index] = current[index].copy(name = newName, displayName = newName)
        
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_PRESETS] = json.encodeToString(current)
        }
        
        val pinned = pinnedPresetsFlow.first().toMutableList()
        val pinnedIndex = pinned.indexOf(oldName)
        if (pinnedIndex != -1) {
            pinned[pinnedIndex] = newName
            setPinnedPresets(pinned)
        }
        
        val activePreset = dataStore.data.first()[PreferencesKeys.EQUALIZER_PRESET]
        if (activePreset == oldName) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.EQUALIZER_PRESET] = newName
            }
        }
    }
    
    suspend fun updateCustomPresetBands(presetName: String, bandLevels: List<Int>) {
        val current = customPresetsFlow.first().toMutableList()
        val index = current.indexOfFirst { it.name == presetName }
        if (index == -1) return
        
        current[index] = current[index].copy(bandLevels = bandLevels)
        
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_PRESETS] = json.encodeToString(current)
        }
    }
    
    // ===== Pinned Presets Persistence =====
    
    val pinnedPresetsFlow: Flow<List<String>> =
        dataStore.data.map { preferences ->
            val jsonString = preferences[PreferencesKeys.PINNED_PRESETS]
            if (jsonString != null) {
                try {
                    json.decodeFromString<List<String>>(jsonString)
                } catch (e: Exception) {
                    // Default pinned: All standard presets
                    EqualizerPreset.ALL_PRESETS.map { it.name }
                }
            } else {
                 // Default pinned: All standard presets
                 EqualizerPreset.ALL_PRESETS.map { it.name }
            }
        }
        
    suspend fun setPinnedPresets(presetNames: List<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PINNED_PRESETS] = json.encodeToString(presetNames)
        }
    }

    // ===== Developer Options =====
    
    /**
     * Album art quality for player view.
     * Controls the maximum resolution for album artwork displayed in the full player.
     * Thumbnails in lists always use low resolution (256px) for optimal performance.
     */
    val albumArtQualityFlow: Flow<AlbumArtQuality> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ALBUM_ART_QUALITY]
                ?.let { 
                    try { AlbumArtQuality.valueOf(it) } 
                    catch (e: Exception) { AlbumArtQuality.ORIGINAL }
                }
                ?: AlbumArtQuality.ORIGINAL
        }

    suspend fun setAlbumArtQuality(quality: AlbumArtQuality) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALBUM_ART_QUALITY] = quality.name
        }
    }

    /**
     * Whether tapping the background area of the player sheet closes it.
     * Default is true for intuitive dismissal, but power users may prefer to disable this.
     */
    val tapBackgroundClosesPlayerFlow: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.TAP_BACKGROUND_CLOSES_PLAYER] ?: true
        }

    suspend fun setTapBackgroundClosesPlayer(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TAP_BACKGROUND_CLOSES_PLAYER] = enabled
        }
    }

    val hapticsEnabledFlow: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true
        }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun clearPreferencesByKeys(keyNames: Set<String>) {
        if (keyNames.isEmpty()) return
        dataStore.edit { preferences ->
            preferences.asMap().keys
                .filter { key -> key.name in keyNames }
                .forEach { key ->
                    @Suppress("UNCHECKED_CAST")
                    preferences.remove(key as Preferences.Key<Any>)
                }
        }
    }

    suspend fun clearPreferencesExceptKeys(excludedKeyNames: Set<String>) {
        dataStore.edit { preferences ->
            preferences.asMap().keys
                .filterNot { key -> key.name in excludedKeyNames }
                .forEach { key ->
                    @Suppress("UNCHECKED_CAST")
                    preferences.remove(key as Preferences.Key<Any>)
                }
        }
    }

    suspend fun exportPreferencesForBackup(): List<PreferenceBackupEntry> {
        val snapshot = dataStore.data.first().asMap()
        return snapshot.mapNotNull { (key, value) ->
            val keyName = key.name
            when (value) {
                is String -> PreferenceBackupEntry(
                    key = keyName,
                    type = "string",
                    stringValue = value
                )
                is Int -> PreferenceBackupEntry(
                    key = keyName,
                    type = "int",
                    intValue = value
                )
                is Long -> PreferenceBackupEntry(
                    key = keyName,
                    type = "long",
                    longValue = value
                )
                is Boolean -> PreferenceBackupEntry(
                    key = keyName,
                    type = "boolean",
                    booleanValue = value
                )
                is Float -> PreferenceBackupEntry(
                    key = keyName,
                    type = "float",
                    floatValue = value
                )
                is Double -> PreferenceBackupEntry(
                    key = keyName,
                    type = "double",
                    doubleValue = value
                )
                is Set<*> -> {
                    val stringSet = value.filterIsInstance<String>().toSet()
                    PreferenceBackupEntry(
                        key = keyName,
                        type = "string_set",
                        stringSetValue = stringSet
                    )
                }
                else -> null
            }
        }
    }

    suspend fun importPreferencesFromBackup(
        entries: List<PreferenceBackupEntry>,
        clearExisting: Boolean = true
    ) {
        dataStore.edit { preferences ->
            if (clearExisting) {
                preferences.clear()
            }

            entries.forEach { entry ->
                when (entry.type) {
                    "string" -> {
                        val value = entry.stringValue ?: return@forEach
                        preferences[stringPreferencesKey(entry.key)] = value
                    }
                    "int" -> {
                        val value = entry.intValue
                            ?: entry.doubleValue?.toInt()
                            ?: entry.longValue?.toInt()
                            ?: return@forEach
                        preferences[intPreferencesKey(entry.key)] = value
                    }
                    "long" -> {
                        val value = entry.longValue
                            ?: entry.doubleValue?.toLong()
                            ?: entry.intValue?.toLong()
                            ?: return@forEach
                        preferences[longPreferencesKey(entry.key)] = value
                    }
                    "boolean" -> {
                        val value = entry.booleanValue ?: return@forEach
                        preferences[booleanPreferencesKey(entry.key)] = value
                    }
                    "float" -> {
                        val value = entry.floatValue
                            ?: entry.doubleValue?.toFloat()
                            ?: return@forEach
                        preferences[androidx.datastore.preferences.core.floatPreferencesKey(entry.key)] = value
                    }
                    "double" -> {
                        val value = entry.doubleValue
                            ?: entry.floatValue?.toDouble()
                            ?: return@forEach
                        preferences[androidx.datastore.preferences.core.doublePreferencesKey(entry.key)] = value
                    }
                    "string_set" -> {
                        val value = entry.stringSetValue ?: return@forEach
                        preferences[stringSetPreferencesKey(entry.key)] = value
                    }
                }
            }
        }
    }

    // --- Collage Pattern ---

    val collagePatternFlow: Flow<CollagePattern> =
        dataStore.data.map { preferences ->
            CollagePattern.fromStorageKey(preferences[PreferencesKeys.COLLAGE_PATTERN])
        }

    suspend fun setCollagePattern(pattern: CollagePattern) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLLAGE_PATTERN] = pattern.storageKey
        }
    }

    val collageAutoRotateFlow: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[PreferencesKeys.COLLAGE_AUTO_ROTATE] ?: false
        }

    suspend fun setCollageAutoRotate(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLLAGE_AUTO_ROTATE] = enabled
        }
    }

    // --- Quick Settings: Last Playlist ---

    val lastPlaylistIdFlow: Flow<String?> =
        dataStore.data.map { it[PreferencesKeys.LAST_PLAYLIST_ID] }

    val lastPlaylistNameFlow: Flow<String?> =
        dataStore.data.map { it[PreferencesKeys.LAST_PLAYLIST_NAME] }

    suspend fun setLastPlaylist(playlistId: String, playlistName: String) {
        dataStore.edit {
            it[PreferencesKeys.LAST_PLAYLIST_ID] = playlistId
            it[PreferencesKeys.LAST_PLAYLIST_NAME] = playlistName
        }
    }
}
