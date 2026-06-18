package com.assestment.kis.domain.session.detection

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.assestment.kis.domain.core.TimeProvider
import com.assestment.kis.domain.session.model.DistractionType
import com.assestment.kis.domain.session.source.MotionSource
import com.assestment.kis.domain.session.source.NoiseSource
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DistractionMonitorTest {

    @Test
    fun `emits one noise event per threshold crossing, debouncing the re-arm`() = runTest {
        val noiseSource = NoiseSource { flowOf(2000f, 500f, 500f, 2000f) }
        val motionSource = MotionSource { emptyFlow() }
        // One now() call per emitted signal; the gap re-arms detection before the last crossing.
        val times = ArrayDeque(listOf(0L, 2000L, 4000L, 6000L))
        val timeProvider = TimeProvider { times.removeFirst() }

        val monitor = DistractionMonitor(noiseSource, motionSource, FocusConfig.Default, timeProvider)

        monitor.observe().test {
            assertThat(awaitItem().type).isEqualTo(DistractionType.NOISE)
            assertThat(awaitItem().type).isEqualTo(DistractionType.NOISE)
            awaitComplete()
        }
    }

    @Test
    fun `emits movement events from the motion source`() = runTest {
        val noiseSource = NoiseSource { emptyFlow() }
        val motionSource = MotionSource { flowOf(5f) }
        val timeProvider = TimeProvider { 0L }

        val monitor = DistractionMonitor(noiseSource, motionSource, FocusConfig.Default, timeProvider)

        monitor.observe().test {
            assertThat(awaitItem().type).isEqualTo(DistractionType.MOVEMENT)
            awaitComplete()
        }
    }
}
