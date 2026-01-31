package com.kazedev.musicapp.features.music.presentation.viewmodel

import com.kazedev.musicapp.features.music.domain.entity.Song

// Definimos los estados aquí para que BattleViewModel los consuma
sealed class BattleUiState {
    object SelectingGenre : BattleUiState() // 1. Eligiendo género
    object Loading : BattleUiState()        // 2. Cargando
    data class BattleReady(val songA: Song, val songB: Song) : BattleUiState() // 3. Vs
    data class Error(val msg: String) : BattleUiState() // 4. Error
}