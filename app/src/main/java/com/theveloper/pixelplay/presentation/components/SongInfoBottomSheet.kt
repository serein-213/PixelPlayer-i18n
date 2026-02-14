package com.theveloper.pixelplay.presentation.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import com.theveloper.pixelplay.R
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.theveloper.pixelplay.data.model.Song
import com.theveloper.pixelplay.presentation.components.subcomps.AutoSizingTextToFill
import com.theveloper.pixelplay.utils.formatDuration
import com.theveloper.pixelplay.utils.shapes.RoundedStarShape
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape
import androidx.core.net.toUri
import com.theveloper.pixelplay.presentation.viewmodel.PlaylistViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.theveloper.pixelplay.data.ai.SongMetadata
import com.theveloper.pixelplay.data.media.CoverArtUpdate
import com.theveloper.pixelplay.ui.theme.MontserratFamily
import kotlinx.coroutines.launch
import java.io.File

import androidx.compose.ui.graphics.TransformOrigin
import com.theveloper.pixelplay.presentation.screens.TabAnimation
import com.theveloper.pixelplay.ui.theme.GoogleSansRounded

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SongInfoBottomSheet(
    song: Song,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
    onPlaySong: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddNextToQueue: () -> Unit,
    onAddToPlayList: () -> Unit,
    onDeleteFromDevice: (activity: Activity, song: Song, onResult: (Boolean) -> Unit) -> Unit,
    onNavigateToAlbum: () -> Unit,
    onNavigateToArtist: () -> Unit,
    onEditSong: (title: String, artist: String, album: String, genre: String, lyrics: String, trackNumber: Int, coverArtUpdate: CoverArtUpdate?) -> Unit,
    generateAiMetadata: suspend (List<String>) -> Result<SongMetadata>,
    removeFromListTrigger: () -> Unit
) {
    val context = LocalContext.current
    var showEditSheet by remember { mutableStateOf(false) }

    val evenCornerRadiusElems = 26.dp

    val listItemShape = AbsoluteSmoothCornerShape(
        cornerRadiusTR = 20.dp, smoothnessAsPercentBR = 60, cornerRadiusBR = 20.dp,
        smoothnessAsPercentTL = 60, cornerRadiusTL = 20.dp, smoothnessAsPercentBL = 60,
        cornerRadiusBL = 20.dp, smoothnessAsPercentTR = 60
    )
    val albumArtShape = AbsoluteSmoothCornerShape(
        cornerRadiusTR = evenCornerRadiusElems, smoothnessAsPercentBR = 60, cornerRadiusBR = evenCornerRadiusElems,
        smoothnessAsPercentTL = 60, cornerRadiusTL = evenCornerRadiusElems, smoothnessAsPercentBL = 60,
        cornerRadiusBL = evenCornerRadiusElems, smoothnessAsPercentTR = 60
    )
    val playButtonShape = AbsoluteSmoothCornerShape(
        cornerRadiusTR = evenCornerRadiusElems, smoothnessAsPercentBR = 60, cornerRadiusBR = evenCornerRadiusElems,
        smoothnessAsPercentTL = 60, cornerRadiusTL = evenCornerRadiusElems, smoothnessAsPercentBL = 60,
        cornerRadiusBL = evenCornerRadiusElems, smoothnessAsPercentTR = 60
    )

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    val favoriteButtonCornerRadius by animateDpAsState(
        targetValue = if (isFavorite) evenCornerRadiusElems else 60.dp,
        animationSpec = tween(durationMillis = 300), label = "FavoriteCornerAnimation"
    )
    val favoriteButtonContainerColor by animateColorAsState(
        targetValue = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 300), label = "FavoriteContainerColorAnimation"
    )
    val favoriteButtonContentColor by animateColorAsState(
        targetValue = if (isFavorite) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 300), label = "FavoriteContentColorAnimation"
    )

    val favoriteButtonShape = AbsoluteSmoothCornerShape(
        cornerRadiusTR = favoriteButtonCornerRadius, smoothnessAsPercentBR = 60, cornerRadiusBR = favoriteButtonCornerRadius,
        smoothnessAsPercentTL = 60, cornerRadiusTL = favoriteButtonCornerRadius, smoothnessAsPercentBL = 60,
        cornerRadiusBL = favoriteButtonCornerRadius, smoothnessAsPercentTR = 60
    )

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 2 })
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            if (!showEditSheet) {
                onDismiss()
            }
        },
        sheetState = sheetState,
    ) {
        Box(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = tween(durationMillis = 200),
                    alignment = Alignment.BottomCenter
                )
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = tween(durationMillis = 200))
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                ) {
                    // Fila para la carátula del álbum y el título (Always visible)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmartImage(
                            model = song.albumArtUriString,
                            contentDescription = stringResource(R.string.song_info_album_art_cd),
                            shape = albumArtShape,
                            modifier = Modifier.size(80.dp),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            AutoSizingTextToFill(
                                modifier = Modifier.padding(end = 4.dp),
                                fontWeight = FontWeight.Light,
                                text = song.title
                            )
                        }
                        FilledTonalIconButton(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(vertical = 6.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceBright,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            onClick = { showEditSheet = true },
                        ) {
                            Icon(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.song_info_edit_cd)
                            )
                        }
                    }


                }

                Spacer(modifier = Modifier.height(16.dp))

                // Swipeable Content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .wrapContentHeight() // Allow sizing to content
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) { page ->
                    when (page) {
                        0 -> { // Options / Actions
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Min),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        MediumExtendedFloatingActionButton(
                                            modifier = Modifier
                                                .weight(0.5f)
                                                .fillMaxHeight(),
                                            onClick = onPlaySong,
                                            elevation = FloatingActionButtonDefaults.elevation(0.dp),
                                            shape = playButtonShape,
                                            icon = {
                                                Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.song_info_play_cd))
                                            },
                                            text = {
                                                Text(
                                                    modifier = Modifier.padding(end = 10.dp),
                                                    text = stringResource(R.string.common_play)
                                                )
                                            }
                                        )

                                        FilledIconButton(
                                            modifier = Modifier
                                                .weight(0.25f)
                                                .fillMaxHeight(),
                                            onClick = onToggleFavorite,
                                            shape = favoriteButtonShape,
                                            colors = IconButtonDefaults.filledIconButtonColors(
                                                containerColor = favoriteButtonContainerColor,
                                                contentColor = favoriteButtonContentColor
                                            )
                                        ) {
                                            Icon(
                                                modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
                                                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                                contentDescription = if (isFavorite) stringResource(R.string.song_info_favorite_remove_cd) else stringResource(R.string.song_info_favorite_add_cd)
                                            )
                                        }

                                        FilledTonalIconButton(
                                            modifier = Modifier
                                                .weight(0.25f)
                                                .fillMaxHeight(),
                                            onClick = {
                                                try {
                                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "audio/*"
                                                        putExtra(Intent.EXTRA_STREAM, song.contentUriString.toUri())
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.song_info_share_chooser_title)))
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, context.getString(R.string.song_info_share_failed, e.localizedMessage), Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            shape = CircleShape
                                        ) {
                                            Icon(
                                                modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize),
                                                imageVector = Icons.Rounded.Share,
                                                contentDescription = stringResource(R.string.song_info_share_file_cd)
                                            )
                                        }
                                    }
                                }
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Min),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        FilledTonalButton(
                                            modifier = Modifier
                                                .weight(0.6f)
                                                .heightIn(min = 66.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                            ),
                                            contentPadding = PaddingValues(horizontal = 0.dp),
                                            shape = CircleShape,
                                            onClick = onAddToQueue
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Rounded.QueueMusic,
                                                contentDescription = stringResource(R.string.song_info_add_to_queue_cd)
                                            )
                                            Spacer(Modifier.width(14.dp))
                                            Text(stringResource(R.string.song_info_add_to_queue_cd))
                                        }
                                        FilledTonalButton(
                                            modifier = Modifier
                                                .weight(0.4f)
                                                .heightIn(min = 66.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = MaterialTheme.colorScheme.tertiary,
                                                contentColor = MaterialTheme.colorScheme.onTertiary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 0.dp),
                                            shape = CircleShape,
                                            onClick = onAddNextToQueue
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.QueueMusic,
                                                contentDescription = stringResource(R.string.song_info_play_next_cd)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(stringResource(R.string.common_next))
                                        }
                                    }
                                }

                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Min),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        FilledTonalButton(
                                            modifier = Modifier
                                                .weight(0.5f)
                                                .heightIn(min = 66.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            ),
                                            shape = CircleShape,
                                            onClick = onAddToPlayList
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Rounded.PlaylistAdd,
                                                contentDescription = stringResource(R.string.song_info_add_to_playlist_cd)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(stringResource(R.string.selection_playlist_label))
                                        }

                                        FilledTonalButton(
                                            modifier = Modifier
                                                .weight(0.5f)
                                                .heightIn(min = 66.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                                            ),
                                            shape = CircleShape,
                                            onClick = {
                                                (context as? Activity)?.let { activity ->
                                                    onDeleteFromDevice(activity, song) { result ->
                                                        if (result) {
                                                            removeFromListTrigger()
                                                            onDismiss()
                                                        }
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteForever,
                                                contentDescription = stringResource(R.string.common_delete)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(stringResource(R.string.common_delete))
                                        }
                                    }
                                }
                                item {
                                    Spacer(Modifier.height(80.dp))
                                }
                            }
                        }
                        1 -> { // Details / Info
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                item {
                                    ListItem(
                                        modifier = Modifier.clip(shape = listItemShape),
                                        headlineContent = { Text(stringResource(R.string.edit_song_field_duration)) },
                                        supportingContent = { Text(formatDuration(song.duration)) },
                                        leadingContent = { Icon(Icons.Rounded.Schedule, contentDescription = stringResource(R.string.edit_song_field_duration)) }
                                    )
                                }

                                if (!song.genre.isNullOrEmpty()) {
                                    item {
                                        ListItem(
                                            modifier = Modifier.clip(shape = listItemShape),
                                            headlineContent = { Text(stringResource(R.string.edit_song_field_genre)) },
                                            supportingContent = { Text(song.genre) },
                                            leadingContent = { Icon(Icons.Rounded.MusicNote, contentDescription = stringResource(R.string.edit_song_field_genre)) }
                                        )
                                    }
                                }

                                item {
                                    ListItem(
                                        modifier = Modifier
                                            .clip(shape = listItemShape)
                                            .clickable(onClick = onNavigateToAlbum),
                                        headlineContent = { Text(stringResource(R.string.edit_song_field_album)) },
                                        supportingContent = { Text(song.album) },
                                        leadingContent = { Icon(Icons.Rounded.Album, contentDescription = stringResource(R.string.edit_song_field_album)) }
                                    )
                                }

                                item {
                                    ListItem(
                                        modifier = Modifier
                                            .clip(shape = listItemShape)
                                            .clickable(onClick = onNavigateToArtist),
                                        headlineContent = { Text(stringResource(R.string.edit_song_field_artist)) },
                                        supportingContent = { Text(song.displayArtist) },
                                        leadingContent = { Icon(Icons.Rounded.Person, contentDescription = stringResource(R.string.edit_song_field_artist)) }
                                    )
                                }
                                item {
                                    ListItem(
                                        modifier = Modifier
                                            .clip(shape = listItemShape),
                                        headlineContent = { Text(stringResource(R.string.edit_song_field_path)) },
                                        supportingContent = { Text(song.path) },
                                        leadingContent = { Icon(Icons.Rounded.AudioFile, contentDescription = stringResource(R.string.edit_song_field_path)) }
                                    )
                                }
                                item {
                                    Spacer(Modifier.height(80.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Tab Bar
            PrimaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(5.dp),
                containerColor = Color.Transparent,
                divider = {},
                indicator = {}
            ) {
                 TabAnimation(
                    index = 0,
                    title = "Options",
                    selectedIndex = pagerState.currentPage,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    transformOrigin = TransformOrigin(0f, 0.5f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Menu,
                            contentDescription = stringResource(R.string.common_options),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.common_options).uppercase(),
                            fontFamily = GoogleSansRounded,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                
                TabAnimation(
                    index = 1,
                    title = "Details",
                    selectedIndex = pagerState.currentPage,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    transformOrigin = TransformOrigin(1f, 0.5f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = stringResource(R.string.common_info),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            stringResource(R.string.common_info).uppercase(),
                            fontFamily = GoogleSansRounded,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    EditSongSheet(
        visible = showEditSheet,
        song = song,
        onDismiss = { showEditSheet = false },
        onSave = { title, artist, album, genre, lyrics, trackNumber, coverArt ->
            onEditSong(title, artist, album, genre, lyrics, trackNumber, coverArt)
            showEditSheet = false
        },
        generateAiMetadata = generateAiMetadata
    )
}
