package com.kazedev.musicapp.features.music.data.remote.model

data class DeezerChartResponse(
    val tracks: TrackListDto
)

data class TrackListDto(
    val data: List<TrackDto>
)