package com.assestment.kis.data.session.remote

import kotlinx.serialization.Serializable

@Serializable
data class SessionDto(
    val id: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val events: List<DistractionEventDto> = emptyList(),
    val synced: Boolean = false,
)

@Serializable
data class DistractionEventDto(
    val type: String,
    val magnitude: Float,
    val timestampMillis: Long,
)
