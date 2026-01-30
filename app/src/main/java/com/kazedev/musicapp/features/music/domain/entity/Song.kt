package com.kazedev.musicapp.features.music.domain.entity

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val coverUrl: String,
    val previewUrl: String
)