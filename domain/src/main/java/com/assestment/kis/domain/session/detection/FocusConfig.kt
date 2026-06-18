package com.assestment.kis.domain.session.detection

import com.assestment.kis.domain.session.model.DistractionType

/**
 * Tunable detection parameters. Threshold units depend on the platform sources:
 * noise is an RMS amplitude of 16-bit PCM, movement is m/s² of linear acceleration.
 * The defaults are indicative starting points and are meant to be tuned empirically.
 */
data class FocusConfig(
    val noiseThreshold: Float,
    val movementThreshold: Float,
    val rearmWindowMillis: Long,
    val notificationCooldownMillis: Long,
) {
    fun thresholdFor(type: DistractionType): Float = when (type) {
        DistractionType.NOISE -> noiseThreshold
        DistractionType.MOVEMENT -> movementThreshold
    }

    companion object {
        val Default = FocusConfig(
            noiseThreshold = 1500f,
            movementThreshold = 3f,
            rearmWindowMillis = 1500,
            notificationCooldownMillis = 30_000,
        )
    }
}
