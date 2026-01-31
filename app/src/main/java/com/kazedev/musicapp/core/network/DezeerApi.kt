package com.kazedev.musicapp.core.network

import com.kazedev.musicapp.features.music.data.remote.model.DeezerResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface DeezerApi {
    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): DeezerResponse

    @GET("chart/{genreId}/tracks")
    suspend fun getGenreTracks(@Path("genreId") genreId: String): DeezerResponse
}