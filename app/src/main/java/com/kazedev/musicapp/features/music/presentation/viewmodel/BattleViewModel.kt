package com.kazedev.musicapp.features.music.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazedev.musicapp.features.music.domain.usecase.GetBattleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BattleViewModel(
    private val getBattleUseCase: GetBattleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BattleUiState>(BattleUiState.SelectingGenre)
    val uiState = _uiState.asStateFlow()

    fun startBattle(genreId: String) {
        viewModelScope.launch {
            _uiState.value = BattleUiState.Loading

            val result = getBattleUseCase(genreId)

            if (result != null) {
                _uiState.value = BattleUiState.BattleReady(result.first, result.second)
            } else {
                _uiState.value = BattleUiState.Error("No se encontraron canciones para este género.")
            }
        }
    }

    fun reset() {
        _uiState.value = BattleUiState.SelectingGenre
    }
}