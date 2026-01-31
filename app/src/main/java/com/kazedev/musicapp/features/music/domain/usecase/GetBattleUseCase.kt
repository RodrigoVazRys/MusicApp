package com.kazedev.musicapp.features.music.domain.usecase

import com.kazedev.musicapp.core.network.DeezerApi
import com.kazedev.musicapp.features.music.data.remote.mapper.toDomain
import com.kazedev.musicapp.features.music.domain.entity.Song
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetBattleUseCase(private val api: DeezerApi) {

    // IDs de Deezer
    private val genres = mapOf(
        "Pop" to "132",
        "Rock" to "152",
        "Reggaeton" to "122",
        "Rap/Hip Hop" to "116",
        "Electro" to "106",
        "Dance" to "113",
        "Techno/House" to "111",
        "Metal" to "464",
        "Latino" to "197",
        "Música Mexicana" to "65",
        "K-Pop / Asia" to "16",
        "Clásica" to "98",
        "Alternativo" to "85"
    )

    suspend operator fun invoke(genreId: String): Pair<Song, Song>? = coroutineScope {
        try {
            if (genreId == "RANDOM") {
                val allGenres = genres.values.toList()
                val genreA = allGenres.random()
                var genreB = allGenres.random()
                while (genreA == genreB) { genreB = allGenres.random() }

                val deferredA = async { getSongFromGenre(genreA) }
                val deferredB = async { getSongFromGenre(genreB) }

                val songA = deferredA.await()
                val songB = deferredB.await()

                if (songA != null && songB != null) Pair(songA, songB) else null

            } else {
                val songA = getSongFromGenre(genreId)
                val songB = getSongFromGenre(genreId)

                if (songA != null && songB != null && songA.id != songB.id) {
                    Pair(songA, songB)
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun getSongFromGenre(genreId: String): Song? {
        return try {
            val albumsResponse = api.getGenreAlbums(genreId)
            if (albumsResponse.data.isEmpty()) return null

            val randomAlbum = albumsResponse.data.random()

            val tracksResponse = api.getAlbumTracks(randomAlbum.id)

            val trackDto = tracksResponse.data.randomOrNull()

            trackDto?.let { track ->
                val finalCover = track.album?.coverUrl ?: randomAlbum.coverUrl

                Song(
                    id = track.id,
                    title = track.title,
                    artist = track.artist.name,
                    coverUrl = finalCover,
                    previewUrl = track.preview,
                    externalUrl = track.link
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}