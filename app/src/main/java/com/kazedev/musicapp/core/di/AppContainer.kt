package com.kazedev.musicapp.core.di

import android.content.Context
import com.kazedev.musicapp.core.network.DeezerApi
import com.kazedev.musicapp.features.music.data.repository.MusicRepositoryImpl
import com.kazedev.musicapp.features.music.domain.repository.MusicRepository
import com.kazedev.musicapp.features.music.domain.usecase.GetBattleUseCase
import com.kazedev.musicapp.features.music.domain.usecase.SearchTracksUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AppContainer(context: Context) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.deezer.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val deezerApi: DeezerApi by lazy {
        retrofit.create(DeezerApi::class.java)
    }

    val musicRepository: MusicRepository by lazy {
        MusicRepositoryImpl(deezerApi)
    }

    val searchTracksUseCase: SearchTracksUseCase by lazy {
        SearchTracksUseCase(musicRepository)
    }

    val getBattleUseCase: GetBattleUseCase by lazy {
        GetBattleUseCase(deezerApi)
    }
}