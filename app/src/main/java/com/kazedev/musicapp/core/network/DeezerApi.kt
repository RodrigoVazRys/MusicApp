package com.kazedev.musicapp.core.network

import com.kazedev.musicapp.features.music.data.remote.model.DeezerResponse
import com.kazedev.musicapp.features.music.data.remote.model.DeezerChartResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeezerApi {
    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): DeezerResponse

    @GET("editorial/{genreId}/tracks")
    suspend fun getGenreCharts(@Path("genreId") genreId: String): DeezerChartResponse

    @GET("chart/{genreId}/tracks")
    suspend fun getGenreTracks(@Path("genreId") genreId: String): DeezerResponse
}