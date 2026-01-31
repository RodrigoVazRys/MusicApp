package com.kazedev.musicapp.features.music.presentation.screens

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kazedev.musicapp.features.music.domain.entity.Song
import com.kazedev.musicapp.features.music.presentation.viewmodel.BattleViewModel
import com.kazedev.musicapp.features.music.presentation.viewmodel.BattleViewModelFactory

@Composable
fun BattleScreen(factory: BattleViewModelFactory) {
    val viewModel: BattleViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Lógica de Audio (Un solo player para evitar ruido)
    val mediaPlayer = remember { MediaPlayer() }
    var playingUrl by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer.release() }
    }

    fun toggleAudio(url: String) {
        try {
            if (playingUrl == url && mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playingUrl = null
            } else {
                mediaPlayer.reset()
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mediaPlayer.setDataSource(url)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener {
                    it.start()
                    playingUrl = url
                }
                mediaPlayer.setOnCompletionListener { playingUrl = null }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (state.fighterA != null && state.fighterB != null) {

            // Si ya hay un ganador, mostramos pantalla de victoria
            if (state.winner != null) {
                WinnerView(
                    winner = state.winner!!,
                    onNextBattle = { viewModel.loadNewBattle() }
                )
            } else {
                // Si no, mostramos el VS
                Column(modifier = Modifier.fillMaxSize()) {
                    // PELEADOR A (Arriba - Rojo/Azul)
                    FighterSection(
                        song = state.fighterA!!,
                        color = Color(0xFFE91E63), // Pink
                        modifier = Modifier.weight(1f),
                        isPlaying = playingUrl == state.fighterA!!.previewUrl,
                        onPlay = { toggleAudio(state.fighterA!!.previewUrl) },
                        onVote = { viewModel.voteFor(state.fighterA!!) }
                    )

                    // PELEADOR B (Abajo - Cyan/Morado)
                    FighterSection(
                        song = state.fighterB!!,
                        color = Color(0xFF2196F3), // Blue
                        modifier = Modifier.weight(1f),
                        isPlaying = playingUrl == state.fighterB!!.previewUrl,
                        onPlay = { toggleAudio(state.fighterB!!.previewUrl) },
                        onVote = { viewModel.voteFor(state.fighterB!!) }
                    )
                }

                // Icono VS en el centro
                Box(
                    modifier = Modifier.align(Alignment.Center)
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("VS", fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun FighterSection(
    song: Song,
    color: Color,
    modifier: Modifier,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onVote: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(color.copy(alpha=0.6f), Color.Black)))
    ) {
        // Imagen de fondo grande
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().alpha(0.5f),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón Play
            IconButton(
                onClick = onPlay,
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Botón Votar
            Button(
                onClick = onVote,
                colors = ButtonDefaults.buttonColors(containerColor = color)
            ) {
                Text("¡VOTAR!", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WinnerView(winner: Song, onNextBattle: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🏆 GANADOR 🏆", style = MaterialTheme.typography.headlineLarge, color = Color.Yellow)
        Spacer(modifier = Modifier.height(24.dp))

        AsyncImage(
            model = winner.coverUrl,
            contentDescription = null,
            modifier = Modifier.size(200.dp).clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(winner.title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Text(winner.artist, style = MaterialTheme.typography.titleMedium, color = Color.Gray)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNextBattle,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA238FF))
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Siguiente Batalla")
        }
    }
}