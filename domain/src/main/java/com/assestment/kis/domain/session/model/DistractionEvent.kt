package com.assestment.kis.domain.session.model

data class DistractionEvent(
    val type: DistractionType,
    val magnitude: Float,
    val timestampMillis: Long,
)
