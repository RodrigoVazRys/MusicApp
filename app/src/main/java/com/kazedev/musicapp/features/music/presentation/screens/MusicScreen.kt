package com.kazedev.musicapp.features.music.presentation.screens

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kazedev.musicapp.features.music.domain.entity.Song
import com.kazedev.musicapp.features.music.presentation.components.SongCard
import com.kazedev.musicapp.features.music.presentation.viewmodel.MusicViewModel
import com.kazedev.musicapp.features.music.presentation.viewmodel.MusicViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(factory: MusicViewModelFactory) {
    val viewModel: MusicViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    var currentSongId by remember { mutableStateOf<Long?>(null) }

    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer.release() }
    }

    fun toggleSong(song: Song) {
        if (currentSongId == song.id) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                currentSongId = null
            } else {
                mediaPlayer.start()
                currentSongId = song.id
            }
        } else {
            try {
                mediaPlayer.reset()
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mediaPlayer.setDataSource(song.previewUrl)
                mediaPlayer.prepareAsync()

                mediaPlayer.setOnPreparedListener { mp ->
                    mp.start()
                    currentSongId = song.id
                }

                mediaPlayer.setOnCompletionListener {
                    currentSongId = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Text(
                text = "MusicApp by ROY",
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, top = 40.dp, bottom = 10.dp)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                placeholder = { Text("Buscar un artista o canción...") },
                trailingIcon = {
                    IconButton(onClick = {
                        if (query.isNotBlank()) {
                            viewModel.search(query)
                            keyboardController?.hide()
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (query.isNotBlank()) {
                            viewModel.search(query)
                            keyboardController?.hide()
                        }
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(state.songs) { song ->
                        SongCard(
                            song = song,
                            isPlaying = (song.id == currentSongId),
                            onToggle = { toggleSong(song) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 90.dp, end = 20.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}