package com.assestment.kis.domain.session.source

import kotlinx.coroutines.flow.Flow

/** Emits ambient noise magnitude (RMS amplitude). Implemented in the platform layer. */
fun interface NoiseSource {
    fun amplitudes(): Flow<Float>
}
