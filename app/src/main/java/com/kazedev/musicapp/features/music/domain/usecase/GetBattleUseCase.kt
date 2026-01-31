package com.kazedev.musicapp.features.music.domain.usecase

import com.kazedev.musicapp.core.network.DeezerApi
import com.kazedev.musicapp.features.music.data.remote.mapper.toDomain
import com.kazedev.musicapp.features.music.domain.entity.Song

class GetBattleUseCase(private val api: DeezerApi) {

    // IDs de géneros musicales de Deezer
    private val genreIds = listOf(
        "132", // Pop
        "116", // Rap/Hip Hop
        "152", // Rock
        "113", // Dance
        "197", // Latino (¡Importante!)
        "464", // Metal
        "165", // R&B
        "0"    // Top Mundial (General)
    )

    suspend operator fun invoke(): Pair<Song, Song>? {
        return try {
            val randomGenre = genreIds.random()

            val response = api.getGenreTracks(randomGenre)
            val tracks = response.data

            if (tracks.size >= 2) {
                val shuffled = tracks.shuffled()
                val fighterA = shuffled[0].toDomain()
                val fighterB = shuffled[1].toDomain()

                Pair(fighterA, fighterB)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}