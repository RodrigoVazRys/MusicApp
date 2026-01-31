package com.kazedev.musicapp.features.music.presentation.screens

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private fun openInYoutube(context: android.content.Context, artist: String, title: String) {
    val query = "$artist $title audio"
    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$encodedQuery"))
    try { context.startActivity(webIntent) } catch (e: Exception) { }
}

@Composable
fun BattleScreen(factory: BattleViewModelFactory) {
    val viewModel: BattleViewModel = viewModel(factory = factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF121216))) {
        when (val s = state) {
            is BattleUiState.SelectingMode -> SelectionScreen(
                state = s,
                onSearch = { viewModel.searchSong(it) },
                onSelectSong = { viewModel.selectFighter(it) },
                onGenreSelected = { viewModel.startRandomBattle(it) },
                onClear = { viewModel.clearSelection() }
            )
            is BattleUiState.Loading -> LoadingView()

            is BattleUiState.RandomBattle -> BattleArenaView(
                songA = s.songA,
                songB = s.songB,
                // mode = "RANDOM",
                onNextBattle = { viewModel.reset() }
            )

            is BattleUiState.PartyBattle -> PartyArenaView(
                state = s,
                onVote = { isA -> viewModel.voteInParty(isA) },
                onFinishBattle = { viewModel.finishParty() },
                onExit = { viewModel.reset() }
            )

            is BattleUiState.Error -> ErrorView(s.msg) { viewModel.reset() }
        }
    }
}

@Composable
fun SelectionScreen(
    state: BattleUiState.SelectingMode,
    onSearch: (String) -> Unit,
    onSelectSong: (Song) -> Unit,
    onGenreSelected: (String) -> Unit,
    onClear: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        val title = when {
            state.selectedFighterA == null -> "Music Battle"
            state.selectedFighterB == null -> "Elige al Oponente"
            else -> "¡Listos!"
        }
        Text(title, style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold), color = Color.White, modifier = Modifier.padding(vertical = 10.dp))

        if (state.selectedFighterA != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).background(Color(0xFF23232D), RoundedCornerShape(12.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(model = state.selectedFighterA.coverUrl, contentDescription = null, modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Peleador 1", color = Color(0xFFA238FF), fontSize = MaterialTheme.typography.labelSmall.fontSize)
                    Text(state.selectedFighterA.artist, color = Color.White, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onClear) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
            }
        }

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            placeholder = { Text("Canciones VS personalizado") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF23232D),
                unfocusedContainerColor = Color(0xFF23232D),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        if (state.searchQuery.isNotEmpty()) {
            LazyColumn {
                items(state.searchResults) { song ->
                    ListItem(
                        headlineContent = { Text(song.title, color = Color.White) },
                        supportingContent = { Text(song.artist, color = Color.Gray) },
                        leadingContent = { AsyncImage(model = song.coverUrl, contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp))) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onSelectSong(song) }
                    )
                }
            }
        } else {
            GenreGridView(onGenreSelected)
        }
    }
}

@Composable
fun BattleArenaView(songA: Song, songB: Song, onNextBattle: () -> Unit) {
    var winner by remember { mutableStateOf<Song?>(null) }

    var playingUrl by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release() }
    }

    fun toggleAudio(url: String) {
        if (playingUrl == url) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            playingUrl = null
        } else {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                    setDataSource(url)
                    prepareAsync()
                    setOnPreparedListener { start() }
                    setOnCompletionListener { playingUrl = null }
                }
                playingUrl = url
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        BattleFighter(
            song = songA,
            isPlaying = playingUrl == songA.previewUrl,
            onPlayClick = { toggleAudio(songA.previewUrl) },
            onVoteClick = { winner = songA },
            modifier = Modifier.weight(1f),
            color = Color(0xFFE91E63)
        )

        Box(modifier = Modifier.height(40.dp).fillMaxWidth().background(Color.Black), contentAlignment = Alignment.Center) {
            Text("V S", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        }

        BattleFighter(
            song = songB,
            isPlaying = playingUrl == songB.previewUrl,
            onPlayClick = { toggleAudio(songB.previewUrl) },
            onVoteClick = { winner = songB },
            modifier = Modifier.weight(1f),
            color = Color(0xFF2196F3)
        )
    }

    if (winner != null) {
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
fun PartyArenaView(
    state: BattleUiState.PartyBattle,
    onVote: (Boolean) -> Unit,
    onFinishBattle: () -> Unit,
    onExit: () -> Unit
) {
    var playingUrl by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val context = LocalContext.current

    DisposableEffect(Unit) { onDispose { mediaPlayer?.release() } }

    fun toggleAudio(url: String) {
        if (playingUrl == url) {
            mediaPlayer?.stop(); mediaPlayer?.reset(); playingUrl = null
        } else {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                    setDataSource(url); prepareAsync()
                    setOnPreparedListener { start() }
                    setOnCompletionListener { playingUrl = null }
                }
                playingUrl = url
            } catch (e: Exception) {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                FighterCard(state.songA, state.votesA, Color(0xFFE91E63), playingUrl == state.songA.previewUrl) {
                    toggleAudio(state.songA.previewUrl)
                }
                Button(
                    onClick = { onVote(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                ) { Text("+1 VOTO") }
            }

            Box(modifier = Modifier.weight(1f)) {
                FighterCard(state.songB, state.votesB, Color(0xFF2196F3), playingUrl == state.songB.previewUrl) {
                    toggleAudio(state.songB.previewUrl)
                }
                Button(
                    onClick = { onVote(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                ) { Text("+1 VOTO") }
            }

            Button(
                onClick = onFinishBattle,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA238FF)),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("🏁 FINALIZAR BATALLA") }
        }


        if (state.isFinished) {
            val isDraw = state.votesA == state.votesB

            val winner = if (state.votesA > state.votesB) state.songA else state.songB
            val winnerVotes = if (state.votesA > state.votesB) state.votesA else state.votesB

            Dialog(onDismissRequest = {}) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF191922)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isDraw) {
                            Text(
                                "😐 ¡EMPATE!",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.Yellow
                            )
                            Text("Nadie ganó esta ronda", color = Color.Gray)
                        } else {
                            Text(
                                "🏆 GANADOR",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color(0xFFA238FF)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        if (isDraw) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AsyncImage(
                                    model = state.songA.coverUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
                                )
                                AsyncImage(
                                    model = state.songB.coverUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ambas tuvieron $winnerVotes votos", color = Color.White)
                        } else {
                            AsyncImage(
                                model = winner.coverUrl,
                                contentDescription = null,
                                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Ganador: ${winner.artist}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text("con $winnerVotes votos", color = Color.Gray)

                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    openInYoutube(
                                        context,
                                        winner.artist,
                                        winner.title
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    null
                                ); Spacer(modifier = Modifier.width(8.dp)); Text("Ver Ganador en YouTube")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onExit) { Text("Nueva Batalla", color = Color.Gray) }
                    }
                }
            }
        }
    }
}

@Composable
fun FighterCard(song: Song, votes: Int, color: Color, isPlaying: Boolean, onPlay: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(model = song.coverUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))

        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(song.artist, style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            IconButton(onClick = onPlay, modifier = Modifier.size(60.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
        }

        Text(
            text = votes.toString(),
            color = color.copy(alpha = 0.3f),
            fontSize = MaterialTheme.typography.displayLarge.fontSize,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp)
        )
    }
}

@Composable
fun GenreGridView(onGenreSelected: (String) -> Unit) {
    val genres = listOf(
        "K-Pop" to "16",
        "Pop" to "132",
        "Dance" to "113",
        "Techno/House" to "111",
        "Electro" to "106",
        "Rap/Hip Hop" to "116",
        "Latino" to "197",
        "Reggaeton" to "122",
        "Metal" to "464",
        "Rock" to "152",
        "Mexicana" to "65",
        "Clásica" to "98",
        "Indie" to "85"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
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

@Composable
fun GenreCard(name: String, color1: Color, color2: Color, onClick: () -> Unit) {
    Box(modifier = Modifier.height(100.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(color1, color2))).clickable { onClick() }.padding(16.dp), contentAlignment = Alignment.Center) {
        Text(name, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun LoadingView() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFA238FF)) } }

@Composable
fun ErrorView(msg: String, onRetry: () -> Unit) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Error: $msg", color = Color.Red); Button(onClick = onRetry) { Text("Reintentar") } } } }

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
        AsyncImage(
            model = song.coverUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))

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