package com.kazedev.musicapp.features.music.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazedev.musicapp.features.music.domain.entity.Song
import com.kazedev.musicapp.features.music.domain.usecase.GetBattleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BattleViewModel(
    private val getBattleUseCase: GetBattleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BattleUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadNewBattle() // Cargar batalla al iniciar
    }

    fun loadNewBattle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, winner = null, error = null) }

            val result = getBattleUseCase()

            if (result != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fighterA = result.first,
                        fighterB = result.second
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, error = "No se pudo cargar la batalla")
                }
            }
        }
    }

    fun voteFor(song: Song) {
        _uiState.update { it.copy(winner = song) }
    }
}