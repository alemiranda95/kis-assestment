package com.assestment.kis.domain.session.detection

import com.assestment.kis.domain.core.TimeProvider
import com.assestment.kis.domain.session.model.DistractionEvent
import com.assestment.kis.domain.session.model.DistractionType
import com.assestment.kis.domain.session.source.MotionSource
import com.assestment.kis.domain.session.source.NoiseSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge

/**
 * Merges the noise and movement signal streams and runs them through a [DistractionEvaluator]
 * to emit discrete distraction events. A fresh evaluator is created per [observe] call, so each
 * session starts with clean detection state.
 */
class DistractionMonitor(
    private val noiseSource: NoiseSource,
    private val motionSource: MotionSource,
    private val config: FocusConfig,
    private val timeProvider: TimeProvider,
) {
    fun observe(): Flow<DistractionEvent> {
        val evaluator = DistractionEvaluator(config)
        val noise = noiseSource.amplitudes().map {
            SensorSignal(DistractionType.NOISE, it, timeProvider.now())
        }
        val motion = motionSource.magnitudes().map {
            SensorSignal(DistractionType.MOVEMENT, it, timeProvider.now())
        }
        return merge(noise, motion).mapNotNull(evaluator::evaluate)
    }
}
