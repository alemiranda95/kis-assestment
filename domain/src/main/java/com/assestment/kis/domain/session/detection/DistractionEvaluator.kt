package com.assestment.kis.domain.session.detection

import com.assestment.kis.domain.session.model.DistractionEvent
import com.assestment.kis.domain.session.model.DistractionType

/**
 * Turns a stream of raw [SensorSignal]s into discrete [DistractionEvent]s using edge-triggered
 * detection: an event fires only when a signal first crosses its threshold. After firing, that
 * type is disarmed and will not fire again until the signal has stayed below the threshold for
 * [FocusConfig.rearmWindowMillis] — so a sustained noise counts as one event, not hundreds, and
 * flapping around the threshold is debounced.
 *
 * Holds per-type state, so use one instance per session and drive it from a single coroutine.
 */
class DistractionEvaluator(private val config: FocusConfig) {

    private class TypeState(
        var armed: Boolean = true,
        var belowSinceMillis: Long? = null,
    )

    private val states = mutableMapOf<DistractionType, TypeState>()

    fun evaluate(signal: SensorSignal): DistractionEvent? {
        val threshold = config.thresholdFor(signal.type)
        val state = states.getOrPut(signal.type) { TypeState() }

        if (signal.magnitude >= threshold) {
            state.belowSinceMillis = null
            if (state.armed) {
                state.armed = false
                return DistractionEvent(signal.type, signal.magnitude, signal.timestampMillis)
            }
            return null
        }

        // Below threshold: re-arm once it has stayed below for the debounce window.
        val belowSince = state.belowSinceMillis ?: signal.timestampMillis.also {
            state.belowSinceMillis = it
        }
        if (!state.armed && signal.timestampMillis - belowSince >= config.rearmWindowMillis) {
            state.armed = true
        }
        return null
    }
}
