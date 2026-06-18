package com.assestment.kis.fake

import com.assestment.kis.domain.detection.MotionSource
import com.assestment.kis.domain.detection.NoiseSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Stand-in sources for Phase 3 so the app runs end-to-end before the real platform sensors
 * (Phase 5). Each emits a clear above-threshold spike, then enough below-threshold samples to
 * re-arm the evaluator, producing a steady stream of demo distractions.
 */
class DemoNoiseSource : NoiseSource {
    override fun amplitudes(): Flow<Float> = flow {
        while (true) {
            emit(2200f)
            repeat(3) { delay(800); emit(300f) }
            delay(800)
        }
    }
}

class DemoMotionSource : MotionSource {
    override fun magnitudes(): Flow<Float> = flow {
        delay(1500)
        while (true) {
            emit(6f)
            repeat(3) { delay(800); emit(0.4f) }
            delay(800)
        }
    }
}
