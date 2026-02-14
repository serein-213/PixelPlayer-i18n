@file:OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)

package com.theveloper.pixelplay.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.theveloper.pixelplay.R
import com.theveloper.pixelplay.ui.theme.GoogleSansRounded
import kotlin.math.max
import kotlin.math.min
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

@Composable
fun PlaylistCreationTypeDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onManualSelected: () -> Unit,
    onAiSelected: () -> Unit,
    isAiEnabled: Boolean = true,
    onSetupAiClick: (() -> Unit)? = null
) {
    if (!visible) return

    val dialogShape = AbsoluteSmoothCornerShape(
        cornerRadiusTL = 24.dp,
        smoothnessAsPercentTL = 60,
        cornerRadiusTR = 24.dp,
        smoothnessAsPercentTR = 60,
        cornerRadiusBL = 40.dp,
        smoothnessAsPercentBL = 60,
        cornerRadiusBR = 40.dp,
        smoothnessAsPercentBR = 60
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Surface(
            shape = dialogShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f),
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        )
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.playlist_create_title),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = GoogleSansRounded,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = stringResource(R.string.playlist_create_flow_choose),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }

                CreationModeCard(
                    title = stringResource(R.string.playlist_mode_manual_title),
                    subtitle = stringResource(R.string.playlist_mode_manual_subtitle),
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.PlaylistAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    onClick = onManualSelected,
                    enabled = true,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                CreationModeCard(
                    title = stringResource(R.string.playlist_mode_ai_title),
                    subtitle = if (isAiEnabled) {
                        stringResource(R.string.playlist_mode_ai_subtitle)
                    } else {
                        stringResource(R.string.playlist_mode_ai_no_key)
                    },
                    icon = {
                        Icon(
                            imageVector = if (isAiEnabled) Icons.Rounded.AutoAwesome else Icons.Rounded.Key,
                            contentDescription = null,
                            tint = if (isAiEnabled) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    onClick = onAiSelected,
                    enabled = isAiEnabled,
                    containerColor = if (isAiEnabled) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                    contentColor = if (isAiEnabled) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (!isAiEnabled) {
                    FilledTonalButton(
                        onClick = {
                            onDismiss()
                            onSetupAiClick?.invoke()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Rounded.Key, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.playlist_setup_api_key))
                    }
                }
            }
        }
    }
}

@Composable
private fun CreationModeCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = AbsoluteSmoothCornerShape(
            cornerRadiusTL = 22.dp,
            smoothnessAsPercentTL = 60,
            cornerRadiusTR = 22.dp,
            smoothnessAsPercentTR = 60,
            cornerRadiusBL = 22.dp,
            smoothnessAsPercentBL = 60,
            cornerRadiusBR = 22.dp,
            smoothnessAsPercentBR = 60
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = contentColor.copy(alpha = 0.16f),
                shape = AbsoluteSmoothCornerShape(
                    cornerRadiusTL = 12.dp,
                    smoothnessAsPercentTL = 60,
                    cornerRadiusTR = 18.dp,
                    smoothnessAsPercentTR = 60,
                    cornerRadiusBL = 18.dp,
                    smoothnessAsPercentBL = 60,
                    cornerRadiusBR = 12.dp,
                    smoothnessAsPercentBR = 60
                )
            ) {
                Row(modifier = Modifier.padding(10.dp)) {
                    icon()
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun CreateAiPlaylistDialog(
    visible: Boolean,
    isGenerating: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onGenerate: (playlistName: String?, prompt: String, minLength: Int, maxLength: Int) -> Unit
) {
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = visible

    if (transitionState.currentState || transitionState.targetState) {
        Dialog(
            onDismissRequest = {
                if (!isGenerating) {
                    onDismiss()
                }
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            AnimatedVisibility(
                visibleState = transitionState,
                enter = slideInVertically(initialOffsetY = { it / 7 }) + fadeIn(animationSpec = tween(220)),
                exit = slideOutVertically(targetOffsetY = { it / 7 }) + fadeOut(animationSpec = tween(200)),
                label = "create_ai_playlist_dialog"
            ) {
                CreateAiPlaylistContent(
                    isGenerating = isGenerating,
                    error = error,
                    onDismiss = onDismiss,
                    onGenerate = onGenerate
                )
            }
        }
    }
}

@Composable
private fun CreateAiPlaylistContent(
    isGenerating: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onGenerate: (playlistName: String?, prompt: String, minLength: Int, maxLength: Int) -> Unit
) {
    val context = LocalContext.current
    var playlistName by rememberSaveable { mutableStateOf("") }
    var basePrompt by rememberSaveable { mutableStateOf("") }
    var includeGenres by rememberSaveable { mutableStateOf("") }
    var excludeGenres by rememberSaveable { mutableStateOf("") }
    var preferredLanguage by rememberSaveable { mutableStateOf("") }
    var minSongsInput by rememberSaveable { mutableStateOf("12") }
    var maxSongsInput by rememberSaveable { mutableStateOf("24") }
    var selectedMood by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedActivity by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedEra by rememberSaveable { mutableStateOf("Any era") }
    var energyLevel by rememberSaveable { mutableIntStateOf(3) }
    var discoveryLevel by rememberSaveable { mutableIntStateOf(3) }
    var prioritizeFavorites by rememberSaveable { mutableStateOf(true) }
    var avoidExplicit by rememberSaveable { mutableStateOf(false) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val moodOptions = remember {
        listOf(
            "Chill" to R.string.playlist_mood_chill,
            "Energetic" to R.string.playlist_mood_energetic,
            "Happy" to R.string.playlist_mood_happy,
            "Dark" to R.string.playlist_mood_dark,
            "Romantic" to R.string.playlist_mood_romantic,
            "Melancholic" to R.string.playlist_mood_melancholic
        )
    }
    val activityOptions = remember {
        listOf(
            "Workout" to R.string.playlist_activity_workout,
            "Focus" to R.string.playlist_activity_focus,
            "Road trip" to R.string.playlist_activity_road_trip,
            "Party" to R.string.playlist_activity_party,
            "Study" to R.string.playlist_activity_study,
            "Late night" to R.string.playlist_activity_late_night
        )
    }
    val eraOptions = remember {
        listOf(
            "Any era" to R.string.playlist_era_any,
            "70s" to R.string.playlist_era_70s,
            "80s" to R.string.playlist_era_80s,
            "90s" to R.string.playlist_era_90s,
            "2000s" to R.string.playlist_era_2000s,
            "2010s" to R.string.playlist_era_2010s,
            "2020s" to R.string.playlist_era_2020s
        )
    }

    val generatedPromptPreview = remember(
        basePrompt,
        selectedMood,
        selectedActivity,
        selectedEra,
        includeGenres,
        excludeGenres,
        preferredLanguage,
        energyLevel,
        discoveryLevel,
        prioritizeFavorites,
        avoidExplicit
    ) {
        buildAiPlaylistPrompt(
            basePrompt = basePrompt,
            mood = selectedMood,
            activity = selectedActivity,
            era = selectedEra,
            includeGenres = includeGenres,
            excludeGenres = excludeGenres,
            preferredLanguage = preferredLanguage,
            energyLevel = energyLevel,
            discoveryLevel = discoveryLevel,
            prioritizeFavorites = prioritizeFavorites,
            avoidExplicit = avoidExplicit
        )
    }

    val triggerGeneration: () -> Unit = generation@{
        val minSongs = minSongsInput.toIntOrNull()
        val maxSongs = maxSongsInput.toIntOrNull()

        if (generatedPromptPreview.isBlank()) {
            localError = context.getString(R.string.playlist_error_add_instruction)
            return@generation
        }

        if (minSongs == null || maxSongs == null) {
            localError = context.getString(R.string.playlist_error_invalid_range)
            return@generation
        }

        val normalizedMin = min(minSongs.coerceIn(5, 150), maxSongs.coerceIn(5, 150))
        val normalizedMax = max(minSongs.coerceIn(5, 150), maxSongs.coerceIn(5, 150))

        localError = null
        onGenerate(
            playlistName.trim().ifBlank { null },
            generatedPromptPreview,
            normalizedMin,
            normalizedMax
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.playlist_ai_lab_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = GoogleSansRounded,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    FilledTonalIconButton(
                        modifier = Modifier.padding(start = 6.dp),
                        enabled = !isGenerating,
                        onClick = onDismiss,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.common_dismiss))
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 2.dp,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        modifier = Modifier.padding(start = 10.dp),
                        onClick = {
                            playlistName = ""
                            basePrompt = ""
                            includeGenres = ""
                            excludeGenres = ""
                            preferredLanguage = ""
                            minSongsInput = "12"
                            maxSongsInput = "24"
                            selectedMood = null
                            selectedActivity = null
                            selectedEra = "Any era"
                            energyLevel = 3
                            discoveryLevel = 3
                            prioritizeFavorites = true
                            avoidExplicit = false
                            localError = null
                        },
                        shape = CircleShape,
                        enabled = !isGenerating,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                    ) {
                        Text(stringResource(R.string.playlist_reset))
                    }

                    MediumExtendedFloatingActionButton(
                        onClick = triggerGeneration,
                        //enabled = !isGenerating,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        shape = CircleShape
                    ) {
                        if (isGenerating) {
                            LoadingIndicator(modifier = Modifier.height(28.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(stringResource(R.string.playlist_generating_indicator))
                        } else {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(stringResource(R.string.playlist_generate_ai))
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            HeroAiCard(isGenerating = isGenerating)

            AiSectionCard(title = stringResource(R.string.playlist_ai_section_intent)) {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text(stringResource(R.string.playlist_name_optional)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = basePrompt,
                    onValueChange = { basePrompt = it },
                    label = { Text(stringResource(R.string.playlist_prompt_feel_label)) },
                    placeholder = { Text(stringResource(R.string.playlist_prompt_placeholder_example)) },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AiSectionCard(title = stringResource(R.string.playlist_ai_section_direction)) {
                Text(stringResource(R.string.playlist_mood_label), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                ChipsSingleSelect(
                    options = moodOptions,
                    selected = selectedMood,
                    onSelectedChange = { selectedMood = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(stringResource(R.string.playlist_activity_label), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                ChipsSingleSelect(
                    options = activityOptions,
                    selected = selectedActivity,
                    onSelectedChange = { selectedActivity = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(stringResource(R.string.playlist_era_label), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                ChipsSingleSelect(
                    options = eraOptions,
                    selected = selectedEra,
                    onSelectedChange = { selectedEra = it ?: "Any era" }
                )
            }

            AiSectionCard(title = stringResource(R.string.playlist_ai_section_curation)) {
                LevelSelector(
                    label = stringResource(R.string.playlist_energy_label),
                    selectedLevel = energyLevel,
                    onLevelSelected = { energyLevel = it }
                )
                Spacer(modifier = Modifier.height(10.dp))
                LevelSelector(
                    label = stringResource(R.string.playlist_discovery_label),
                    selectedLevel = discoveryLevel,
                    onLevelSelected = { discoveryLevel = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minSongsInput,
                        onValueChange = { value: String ->
                            minSongsInput = value.filter { ch: Char -> ch.isDigit() }.take(3)
                        },
                        label = { Text(stringResource(R.string.playlist_min_songs)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxSongsInput,
                        onValueChange = { value: String ->
                            maxSongsInput = value.filter { ch: Char -> ch.isDigit() }.take(3)
                        },
                        label = { Text(stringResource(R.string.playlist_max_songs)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            AiSectionCard(title = stringResource(R.string.playlist_ai_section_filters)) {
                OutlinedTextField(
                    value = includeGenres,
                    onValueChange = { includeGenres = it },
                    label = { Text(stringResource(R.string.playlist_prioritize_genres_label)) },
                    placeholder = { Text(stringResource(R.string.playlist_prioritize_genres_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = excludeGenres,
                    onValueChange = { excludeGenres = it },
                    label = { Text(stringResource(R.string.playlist_avoid_genres_label)) },
                    placeholder = { Text(stringResource(R.string.playlist_avoid_genres_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = preferredLanguage,
                    onValueChange = { preferredLanguage = it },
                    label = { Text(stringResource(R.string.playlist_preferred_language_label)) },
                    placeholder = { Text(stringResource(R.string.playlist_preferred_language_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                ToggleRow(
                    title = stringResource(R.string.playlist_prioritize_favorites),
                    checked = prioritizeFavorites,
                    onCheckedChange = { prioritizeFavorites = it }
                )
                ToggleRow(
                    title = stringResource(R.string.playlist_avoid_explicit),
                    checked = avoidExplicit,
                    onCheckedChange = { avoidExplicit = it }
                )
            }

            if (!localError.isNullOrBlank() || !error.isNullOrBlank()) {
                Card(
                    shape = AbsoluteSmoothCornerShape(
                        cornerRadiusTL = 16.dp,
                        smoothnessAsPercentTL = 60,
                        cornerRadiusTR = 16.dp,
                        smoothnessAsPercentTR = 60,
                        cornerRadiusBL = 16.dp,
                        smoothnessAsPercentBL = 60,
                        cornerRadiusBR = 16.dp,
                        smoothnessAsPercentBR = 60
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = localError ?: error.orEmpty(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            AiSectionCard(title = stringResource(R.string.playlist_ai_section_prompt_preview)) {
                Text(
                    text = generatedPromptPreview.ifBlank {
                        stringResource(R.string.playlist_prompt_preview_empty)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun HeroAiCard(isGenerating: Boolean) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
        ),
        shape = AbsoluteSmoothCornerShape(
            cornerRadiusTL = 30.dp,
            smoothnessAsPercentTL = 60,
            cornerRadiusTR = 20.dp,
            smoothnessAsPercentTR = 60,
            cornerRadiusBL = 20.dp,
            smoothnessAsPercentBL = 60,
            cornerRadiusBR = 30.dp,
            smoothnessAsPercentBR = 60
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.14f),
                    shape = AbsoluteSmoothCornerShape(
                        cornerRadiusTL = 14.dp,
                        smoothnessAsPercentTL = 60,
                        cornerRadiusTR = 28.dp,
                        smoothnessAsPercentTR = 60,
                        cornerRadiusBL = 28.dp,
                        smoothnessAsPercentBL = 60,
                        cornerRadiusBR = 14.dp,
                        smoothnessAsPercentBR = 60
                    )
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(22.dp),
                                strokeWidth = 2.2.dp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.playlist_hero_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.playlist_hero_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.22f)
            )
            Text(
                text = stringResource(R.string.playlist_hero_note),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun AiSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = AbsoluteSmoothCornerShape(
            cornerRadiusTL = 20.dp,
            smoothnessAsPercentTL = 60,
            cornerRadiusTR = 20.dp,
            smoothnessAsPercentTR = 60,
            cornerRadiusBL = 20.dp,
            smoothnessAsPercentBL = 60,
            cornerRadiusBR = 20.dp,
            smoothnessAsPercentBR = 60
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ChipsSingleSelect(
    options: List<Pair<String, Int>>,
    selected: String?,
    onSelectedChange: (String?) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        options.forEachIndexed { index, (internalKey, labelRes) ->
            val isSelected = selected == internalKey
            val usePrimaryPalette = index % 2 == 0
            val selectedContainer = if (usePrimaryPalette) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer
            }
            val selectedLabel = if (usePrimaryPalette) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onTertiaryContainer
            }
            FilterChip(
                selected = isSelected,
                shape = CircleShape,
                onClick = {
                    onSelectedChange(if (isSelected) null else internalKey)
                },
                border = BorderStroke(
                    color = Color.Transparent,
                    width = 0.dp
                ),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = selectedContainer,
                    selectedLabelColor = selectedLabel,
                    selectedLeadingIconColor = selectedLabel,
                    containerColor = selectedContainer.copy(alpha = 0.24f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                label = { Text(stringResource(labelRes)) }
            )
        }
    }
}

@Composable
private fun LevelSelector(
    label: String,
    selectedLevel: Int,
    onLevelSelected: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = "$selectedLevel/5",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            (1..5).forEach { level ->
                SegmentedButton(
                    selected = selectedLevel == level,
                    onClick = { onLevelSelected(level) },
                    shape = SegmentedButtonDefaults.itemShape(index = level - 1, count = 5),
                    label = { Text(level.toString()) }
                )
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun buildAiPlaylistPrompt(
    basePrompt: String,
    mood: String?,
    activity: String?,
    era: String,
    includeGenres: String,
    excludeGenres: String,
    preferredLanguage: String,
    energyLevel: Int,
    discoveryLevel: Int,
    prioritizeFavorites: Boolean,
    avoidExplicit: Boolean
): String {
    val promptParts = mutableListOf<String>()

    if (basePrompt.isNotBlank()) {
        promptParts += "Core request: ${basePrompt.trim()}."
    }
    if (!mood.isNullOrBlank()) {
        promptParts += "Mood target: $mood."
    }
    if (!activity.isNullOrBlank()) {
        promptParts += "Activity context: $activity."
    }
    if (era != "Any era") {
        promptParts += "Era focus: $era."
    }
    if (includeGenres.isNotBlank()) {
        promptParts += "Prioritize genres: ${includeGenres.trim()}."
    }
    if (excludeGenres.isNotBlank()) {
        promptParts += "Avoid genres: ${excludeGenres.trim()}."
    }
    if (preferredLanguage.isNotBlank()) {
        promptParts += "Preferred language: ${preferredLanguage.trim()}."
    }

    promptParts += "Energy level target: ${energyLevel.coerceIn(1, 5)}/5."
    promptParts += "Discovery target: ${discoveryLevel.coerceIn(1, 5)}/5 where 1 is familiar and 5 is deep cuts."

    if (prioritizeFavorites) {
        promptParts += "Prioritize songs closer to listener favorites when possible."
    }
    if (avoidExplicit) {
        promptParts += "Avoid explicit lyrics whenever alternatives exist."
    }

    promptParts += "Keep transitions smooth and avoid repetitive artist clustering."

    return promptParts.joinToString(separator = " ").trim()
}
