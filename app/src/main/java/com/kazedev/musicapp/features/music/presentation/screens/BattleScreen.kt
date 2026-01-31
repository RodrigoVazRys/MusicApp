package com.kazedev.musicapp.features.music.presentation.screens

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kazedev.musicapp.features.music.domain.entity.Song
import com.kazedev.musicapp.features.music.presentation.viewmodel.BattleUiState
import com.kazedev.musicapp.features.music.presentation.viewmodel.BattleViewModel
import com.kazedev.musicapp.features.music.presentation.viewmodel.BattleViewModelFactory
import java.net.URLEncoder

// --- UTILIDAD PARA YOUTUBE ---
private fun openInYoutube(context: android.content.Context, artist: String, title: String) {
    val query = "$artist $title audio"
    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$encodedQuery"))
    try {
        context.startActivity(webIntent)
    } catch (e: Exception) {
        // Fallback
    }
}

@Composable
fun BattleScreen(factory: BattleViewModelFactory) {
    val viewModel: BattleViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121216))) {
        when (val s = state) {
            is BattleUiState.SelectingGenre -> GenreGridView { viewModel.startBattle(it) }
            is BattleUiState.Loading -> LoadingView()
            is BattleUiState.BattleReady -> BattleArenaView(s.songA, s.songB) { viewModel.reset() }
            is BattleUiState.Error -> ErrorView(s.msg) { viewModel.reset() }
        }
    }
}

// --- VISTAS AUXILIARES ---

@Composable
fun LoadingView() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(color = Color(0xFFA238FF))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Preparando el ring...", color = Color.White)
    }
}

@Composable
fun ErrorView(msg: String, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Error: $msg", color = Color.Red, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}

@Composable
fun GenreGridView(onGenreSelected: (String) -> Unit) {
    val genres = listOf(
        "Pop" to "132",
        "Rock" to "152",
        "Reggaeton" to "122",
        "Rap/Hip Hop" to "116",
        "Electro" to "106",
        "Metal" to "464",
        "Latino" to "197",
        "Alternativo" to "85"
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "Music Battle",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ITEM 1: RANDOM (Destacado en Morado)
            item {
                GenreCard(
                    name = "🎲 RANDOM",
                    color1 = Color(0xFFA238FF),
                    color2 = Color(0xFF7B1FA2)
                ) { onGenreSelected("RANDOM") }
            }

            items(genres) { (name, id) ->
                GenreCard(
                    name = name,
                    color1 = Color(0xFF23232D),
                    color2 = Color(0xFF191922)
                ) { onGenreSelected(id) }
            }
        }
    }
}

@Composable
fun GenreCard(name: String, color1: Color, color2: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.height(100.dp).clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(color1, color2)))
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(name, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun BattleArenaView(songA: Song, songB: Song, onNextBattle: () -> Unit) {
    var winner by remember { mutableStateOf<Song?>(null) }

    // Estado para controlar qué canción suena (A, B o ninguna)
    var playingUrl by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Limpieza al salir
    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    // Función para manejar Play/Pause
    fun toggleAudio(url: String) {
        if (playingUrl == url) {
            // Si tocamos la misma, pausamos
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            playingUrl = null
        } else {
            // Si es nueva, preparamos y tocamos
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                    setDataSource(url)
                    prepareAsync()
                    setOnPreparedListener { start() }
                    setOnCompletionListener { playingUrl = null } // Al terminar, reseteamos icono
                }
                playingUrl = url
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // --- PELEADOR A ---
        BattleFighter(
            song = songA,
            isPlaying = playingUrl == songA.previewUrl,
            onPlayClick = { toggleAudio(songA.previewUrl) },
            onVoteClick = { winner = songA },
            modifier = Modifier.weight(1f),
            color = Color(0xFFE91E63) // Rosa/Rojo para A
        )

        // SEPARADOR VS
        Box(modifier = Modifier.height(40.dp).fillMaxWidth().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("V S", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }

        // --- PELEADOR B ---
        BattleFighter(
            song = songB,
            isPlaying = playingUrl == songB.previewUrl,
            onPlayClick = { toggleAudio(songB.previewUrl) },
            onVoteClick = { winner = songB },
            modifier = Modifier.weight(1f),
            color = Color(0xFF2196F3) // Azul para B
        )
    }

    if (winner != null) {
        // Al ganar, detenemos la música
        LaunchedEffect(Unit) { mediaPlayer?.release(); playingUrl = null }

        Dialog(onDismissRequest = {}) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF191922)), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆 GANADOR", style = MaterialTheme.typography.headlineMedium, color = Color(0xFFA238FF))
                    Spacer(modifier = Modifier.height(16.dp))
                    AsyncImage(model = winner!!.coverUrl, contentDescription = null, modifier = Modifier.size(140.dp).clip(RoundedCornerShape(12.dp)))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(winner!!.title, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(winner!!.artist, color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { openInYoutube(context, winner!!.artist, winner!!.title) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver en YouTube")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { winner = null; onNextBattle() }) { Text("Siguiente Batalla", color = Color.Gray) }
                }
            }
        }
    }
}

@Composable
fun BattleFighter(
    song: Song,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onVoteClick: () -> Unit,
    modifier: Modifier,
    color: Color
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // Imagen de fondo
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Capa oscura para leer
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))

        // Contenido Central
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título Artista
            Text(
                song.artist,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Gigante de PLAY (Clickable en el icono)
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            if (isPlaying) {
                Text("Reproduciendo preview...", color = Color(0xFFA238FF), fontSize = MaterialTheme.typography.bodySmall.fontSize)
            }
        }

        // Botón de VOTAR (Abajo)
        Button(
            onClick = onVoteClick,
            colors = ButtonDefaults.buttonColors(containerColor = color),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Default.Check, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("VOTAR")
        }
    }
}