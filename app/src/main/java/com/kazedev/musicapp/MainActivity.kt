package com.kazedev.musicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kazedev.musicapp.core.di.AppContainer
import com.kazedev.musicapp.features.music.presentation.screens.MusicScreen
import com.kazedev.musicapp.features.music.presentation.viewmodel.MusicViewModelFactory
import com.kazedev.musicapp.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = AppContainer(this)
        val viewModelFactory = MusicViewModelFactory(appContainer.searchTracksUseCase)

        setContent {
            AppTheme {
                MusicScreen(factory = viewModelFactory)
            }
        }
    }
}