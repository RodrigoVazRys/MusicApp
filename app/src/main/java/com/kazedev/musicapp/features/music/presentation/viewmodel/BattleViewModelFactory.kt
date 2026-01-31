package com.kazedev.musicapp.features.music.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kazedev.musicapp.features.music.domain.usecase.GetBattleUseCase

class BattleViewModelFactory(private val useCase: GetBattleUseCase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BattleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BattleViewModel(useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}