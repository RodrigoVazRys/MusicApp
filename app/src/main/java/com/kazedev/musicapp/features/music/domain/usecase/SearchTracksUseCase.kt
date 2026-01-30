package com.kazedev.musicapp.features.music.domain.usecase

import com.kazedev.musicapp.features.music.domain.entity.Song
import com.kazedev.musicapp.features.music.domain.repository.MusicRepository

class SearchTracksUseCase(private val repository: MusicRepository) {
    suspend operator fun invoke(query: String): List<Song> {
        return repository.searchSongs(query)
    }
}