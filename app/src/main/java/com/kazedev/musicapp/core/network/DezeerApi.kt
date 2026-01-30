package com.kazedev.musicapp.core.network

import com.kazedev.musicapp.features.music.data.remote.model.DeezerResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DeezerApi {
    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): DeezerResponse
}