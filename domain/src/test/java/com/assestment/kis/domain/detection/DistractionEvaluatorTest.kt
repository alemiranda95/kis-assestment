package com.assestment.kis.domain.detection

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.assestment.kis.domain.session.DistractionType
import org.junit.jupiter.api.Test

class DistractionEvaluatorTest {

    private val config = FocusConfig(
        noiseThreshold = 1500f,
        movementThreshold = 3f,
        rearmWindowMillis = 1500,
        notificationCooldownMillis = 30_000,
    )

    private fun signal(type: DistractionType, magnitude: Float, time: Long) =
        SensorSignal(type, magnitude, time)

    @Test
    fun `fires when a signal first crosses the threshold`() {
        val evaluator = DistractionEvaluator(config)

        val event = evaluator.evaluate(signal(DistractionType.NOISE, 2000f, 0))

        assertThat(event).isNotNull()
        assertThat(event!!.type).isEqualTo(DistractionType.NOISE)
        assertThat(event.magnitude).isEqualTo(2000f)
        assertThat(event.timestampMillis).isEqualTo(0L)
    }

    @Test
    fun `does not re-fire while the signal stays above the threshold`() {
        val evaluator = DistractionEvaluator(config)

        assertThat(evaluator.evaluate(signal(DistractionType.NOISE, 2000f, 0))).isNotNull()
        assertThat(evaluator.evaluate(signal(DistractionType.NOISE, 2500f, 100))).isNull()
        assertThat(evaluator.evaluate(signal(DistractionType.NOISE, 1800f, 200))).isNull()
    }

    @Test
    fun `re-fires only after staying continuously below the threshold for the rearm window`() {
        val evaluator = DistractionEvaluator(config) // rearmWindowMillis = 1500

        // First crossing fires and disarms NOISE.
        assertThat(evaluator.evaluate(signal(DistractionType.NOISE, 2000f, 0))).isNotNull()

        // Drops below for only 1000ms, then crosses again -> still disarmed, so no event;
        // the brief crossing also resets the below-threshold timer.
        assertThat(evaluator.evaluate(signal(DistractionType.NOISE, 500f, 100))).isNull()
        assertThat(evaluator.evaluate(signal(DistractionType.NOISE, 2000f, 1100))).isNull()

        // Now stays below continuously for >= 1500ms, which re-arms NOISE...
        assertThat(evaluator.evaluate(signal(DistractionType.NOISE, 500f, 1200))).isNull()
        assertThat(evaluator.evaluate(signal(DistractionType.NOISE, 500f, 2800))).isNull()

        // ...so the next crossing fires again.
        assertThat(evaluator.evaluate(signal(DistractionType.NOISE, 2000f, 2900))).isNotNull()
    }

    @Test
    fun `tracks each distraction type independently`() {
        val evaluator = DistractionEvaluator(config)

        assertThat(evaluator.evaluate(signal(DistractionType.NOISE, 2000f, 0))).isNotNull()
        // Movement is unaffected by the noise type being disarmed.
        assertThat(evaluator.evaluate(signal(DistractionType.MOVEMENT, 5f, 10))).isNotNull()
        assertThat(evaluator.evaluate(signal(DistractionType.MOVEMENT, 5f, 20))).isNull()
    }

    @Test
    fun `does not fire below the threshold`() {
        val evaluator = DistractionEvaluator(config)

        assertThat(evaluator.evaluate(signal(DistractionType.MOVEMENT, 2.9f, 0))).isNull()
    }
}
