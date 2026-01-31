package com.kazedev.musicapp.features.music.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazedev.musicapp.features.music.domain.entity.Song
import com.kazedev.musicapp.features.music.domain.usecase.GetBattleUseCase
import com.kazedev.musicapp.features.music.domain.usecase.SearchTracksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BattleViewModel(
    private val getBattleUseCase: GetBattleUseCase,
    private val searchTracksUseCase: SearchTracksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BattleUiState>(BattleUiState.SelectingMode())
    val uiState = _uiState.asStateFlow()

    fun startRandomBattle(genreId: String) {
        viewModelScope.launch {
            _uiState.value = BattleUiState.Loading
            val result = getBattleUseCase(genreId)
            if (result != null) {
                _uiState.value = BattleUiState.RandomBattle(result.first, result.second)
            } else {
                _uiState.value = BattleUiState.Error("No se encontraron oponentes.")
            }
        }
    }

    fun searchSong(query: String) {
        viewModelScope.launch {
            val current = _uiState.value as? BattleUiState.SelectingMode ?: BattleUiState.SelectingMode()

            if (query.isBlank()) {
                _uiState.value = current.copy(searchQuery = "", searchResults = emptyList())
                return@launch
            }

            _uiState.value = current.copy(searchQuery = query)

            try {
                val songs = searchTracksUseCase(query)
                // Verificamos de nuevo el estado por si cambió rápido
                (_uiState.value as? BattleUiState.SelectingMode)?.let {
                    _uiState.value = it.copy(searchResults = songs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectFighter(song: Song) {
        val current = _uiState.value as? BattleUiState.SelectingMode ?: return

        if (current.selectedFighterA == null) {
            _uiState.value = current.copy(
                selectedFighterA = song,
                searchQuery = "",
                searchResults = emptyList()
            )
        } else if (current.selectedFighterB == null) {
            _uiState.value = BattleUiState.PartyBattle(
                songA = current.selectedFighterA,
                songB = song
            )
        }
    }

    fun voteInParty(isForA: Boolean) {
        val current = _uiState.value as? BattleUiState.PartyBattle ?: return
        if (current.isFinished) return

        _uiState.value = if (isForA) {
            current.copy(votesA = current.votesA + 1)
        } else {
            current.copy(votesB = current.votesB + 1)
        }
    }

    fun finishParty() {
        val current = _uiState.value as? BattleUiState.PartyBattle ?: return
        _uiState.value = current.copy(isFinished = true)
    }

    fun reset() {
        _uiState.value = BattleUiState.SelectingMode()
    }

    fun clearSelection() {
        _uiState.value = BattleUiState.SelectingMode()
    }
}