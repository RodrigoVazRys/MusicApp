package com.kazedev.musicapp.core.network

import com.kazedev.musicapp.features.music.data.remote.model.DeezerResponse
import com.kazedev.musicapp.features.music.data.remote.model.DeezerAlbumResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DeezerApi {
    @GET("search")
    suspend fun searchTracks(@Query("q") query: String): DeezerResponse

    @GET("chart/{genreId}/albums")
    suspend fun getGenreAlbums(@Path("genreId") genreId: String): DeezerAlbumResponse

    @GET("album/{albumId}/tracks")
    suspend fun getAlbumTracks(@Path("albumId") albumId: Long): DeezerResponse
}