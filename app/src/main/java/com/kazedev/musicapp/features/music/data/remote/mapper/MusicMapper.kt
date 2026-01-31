package com.kazedev.musicapp.features.music.data.remote.mapper

import com.kazedev.musicapp.features.music.data.remote.model.TrackDto
import com.kazedev.musicapp.features.music.domain.entity.Song

fun TrackDto.toDomain(): Song {
    return Song(
        id = this.id,
        title = this.title,
        artist = this.artist.name,
        coverUrl = this.album.coverUrl,
        previewUrl = this.preview,
        externalUrl = this.link
    )
}