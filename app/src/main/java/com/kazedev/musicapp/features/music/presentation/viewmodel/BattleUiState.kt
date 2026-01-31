package com.kazedev.musicapp.features.music.presentation.viewmodel

import com.kazedev.musicapp.features.music.domain.entity.Song

sealed class BattleUiState {
    data class SelectingMode(
        val searchQuery: String = "",
        val searchResults: List<Song> = emptyList(),
        val selectedFighterA: Song? = null,
        val selectedFighterB: Song? = null
    ) : BattleUiState()

    object Loading : BattleUiState()

    data class RandomBattle(val songA: Song, val songB: Song) : BattleUiState()

    data class PartyBattle(
        val songA: Song,
        val songB: Song,
        val votesA: Int = 0,
        val votesB: Int = 0,
        val isFinished: Boolean = false
    ) : BattleUiState()

    data class Error(val msg: String) : BattleUiState()
}