package com.kazedev.musicapp.features.music.presentation.viewmodel

import com.kazedev.musicapp.features.music.domain.entity.Song

data class BattleUiState(
    val isLoading: Boolean = false,
    val fighterA: Song? = null,
    val fighterB: Song? = null,
    val winner: Song? = null, // Quién ganó la ronda
    val error: String? = null
)