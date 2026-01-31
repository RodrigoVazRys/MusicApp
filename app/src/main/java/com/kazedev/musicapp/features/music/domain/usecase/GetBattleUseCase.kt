package com.kazedev.musicapp.features.music.domain.usecase

import com.kazedev.musicapp.core.network.DeezerApi
import com.kazedev.musicapp.features.music.data.remote.mapper.toDomain
import com.kazedev.musicapp.features.music.data.remote.model.DeezerResponse
import com.kazedev.musicapp.features.music.domain.entity.Song
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetBattleUseCase(private val api: DeezerApi) {

    // MAPA ACTUALIZADO CON LOS IDs DEL JSON OFICIAL DE DEEZER
    private val genres = mapOf(
        "Pop" to "132",
        "Rock" to "152",
        "Reggaeton" to "122",
        "Rap/Hip Hop" to "116",
        "Electro" to "106",
        "Metal" to "464",
        "Latino" to "197",
        "Alternativo" to "85"
    )

    suspend operator fun invoke(genreId: String): Pair<Song, Song>? = coroutineScope {
        try {
            if (genreId == "RANDOM") {
                // MODO CAOS: Elegimos 2 géneros distintos de la lista oficial
                val allGenres = genres.values.toList()
                val genreA = allGenres.random()
                var genreB = allGenres.random()
                while (genreA == genreB) { genreB = allGenres.random() }

                // Llamadas en paralelo
                val deferredA = async<DeezerResponse> { api.getGenreTracks(genreA) }
                val deferredB = async<DeezerResponse> { api.getGenreTracks(genreB) }

                val tracksA = deferredA.await().data
                val tracksB = deferredB.await().data

                if (tracksA.isNotEmpty() && tracksB.isNotEmpty()) {
                    Pair(tracksA.random().toDomain(), tracksB.random().toDomain())
                } else null

            } else {
                // MODO NORMAL: Mismo género
                val response = api.getGenreTracks(genreId)
                val tracks = response.data

                if (tracks.size >= 2) {
                    val shuffled = tracks.shuffled()
                    Pair(shuffled[0].toDomain(), shuffled[1].toDomain())
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}