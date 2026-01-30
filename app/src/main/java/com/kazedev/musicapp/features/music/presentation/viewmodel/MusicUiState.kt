package com.kazedev.musicapp.features.music.presentation.viewmodel

import com.kazedev.musicapp.features.music.domain.entity.Song

data class MusicUiState(
    val isLoading: Boolean = false,
    val songs: List<Song> = emptyList(),
    val error: String? = null
)