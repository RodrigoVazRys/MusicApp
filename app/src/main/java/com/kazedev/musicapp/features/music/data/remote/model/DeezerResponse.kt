package com.kazedev.musicapp.features.music.data.remote.model

import com.google.gson.annotations.SerializedName

data class DeezerResponse(
    val data: List<TrackDto>
)

data class TrackDto(
    val id: Long,
    val title: String,
    val preview: String,
    val artist: ArtistDto,
    val album: AlbumDto
)

data class ArtistDto(val name: String)
data class AlbumDto(@SerializedName("cover_medium") val coverUrl: String)