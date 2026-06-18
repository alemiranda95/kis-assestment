package com.assestment.kis.domain.detection

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import com.assestment.kis.domain.util.TimeProvider
import com.assestment.kis.domain.notification.DistractionNotifier
import com.assestment.kis.domain.permission.AppPermission
import com.assestment.kis.domain.permission.PermissionChecker
import com.assestment.kis.domain.session.DistractionEvent
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObserveDistractionsUseCaseTest {

    private val config = FocusConfig.Default
    private val timeProvider = TimeProvider { 0L }

    private fun monitor() = DistractionMonitor(
        noiseSource = NoiseSource { flowOf(2000f) },
        motionSource = MotionSource { emptyFlow() },
        config = config,
        timeProvider = timeProvider,
    )

    private fun permission(granted: Boolean) = object : PermissionChecker {
        override fun isGranted(permission: AppPermission): Boolean = granted
    }

    @Test
    fun `notifies when notifications are granted`() = runTest {
        val notified = mutableListOf<DistractionEvent>()
        val useCase = ObserveDistractionsUseCase(
            distractionMonitor = monitor(),
            notifier = DistractionNotifier { notified.add(it) },
            permissionChecker = permission(granted = true),
            timeProvider = timeProvider,
            config = config,
        )

        useCase().test {
            awaitItem()
            awaitComplete()
        }

        assertThat(notified).hasSize(1)
    }

    @Test
    fun `does not notify when notifications are denied`() = runTest {
        val notified = mutableListOf<DistractionEvent>()
        val useCase = ObserveDistractionsUseCase(
            distractionMonitor = monitor(),
            notifier = DistractionNotifier { notified.add(it) },
            permissionChecker = permission(granted = false),
            timeProvider = timeProvider,
            config = config,
        )

        useCase().test {
            awaitItem()
            awaitComplete()
        }

        assertThat(notified).hasSize(0)
    }
}
