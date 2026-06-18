package com.assestment.kis.domain.detection

import com.assestment.kis.domain.session.DistractionType

/** A single raw sensor reading fed to the [DistractionEvaluator]. */
data class SensorSignal(
    val type: DistractionType,
    val magnitude: Float,
    val timestampMillis: Long,
)
