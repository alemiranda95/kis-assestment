package com.assestment.kis.domain.session

data class DistractionEvent(
    val type: DistractionType,
    val magnitude: Float,
    val timestampMillis: Long,
)
