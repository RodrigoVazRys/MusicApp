package com.kazedev.musicapp.features.music.domain.repository

import com.kazedev.musicapp.features.music.domain.entity.Song

interface MusicRepository {
    suspend fun searchSongs(query: String): List<Song>
}