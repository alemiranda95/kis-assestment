package com.assestment.kis.domain.detection

import kotlinx.coroutines.flow.Flow

/** Emits movement magnitude (linear acceleration, m/s²). Implemented in the platform layer. */
fun interface MotionSource {
    fun magnitudes(): Flow<Float>
}
