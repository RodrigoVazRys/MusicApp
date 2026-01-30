package com.kazedev.musicapp.features.music.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kazedev.musicapp.features.music.domain.usecase.SearchTracksUseCase

class MusicViewModelFactory(
    private val searchTracksUseCase: SearchTracksUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(searchTracksUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}