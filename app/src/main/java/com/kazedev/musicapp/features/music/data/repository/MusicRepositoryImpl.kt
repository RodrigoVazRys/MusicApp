package com.kazedev.musicapp.features.music.data.repository

import com.kazedev.musicapp.core.network.DeezerApi
import com.kazedev.musicapp.features.music.data.remote.mapper.toDomain
import com.kazedev.musicapp.features.music.domain.entity.Song
import com.kazedev.musicapp.features.music.domain.repository.MusicRepository

class MusicRepositoryImpl(private val api: DeezerApi) : MusicRepository {
    override suspend fun searchSongs(query: String): List<Song> {
        return try {
            api.searchTracks(query).data.map { it.toDomain() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}