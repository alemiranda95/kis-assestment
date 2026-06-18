package com.assestment.kis.domain.detection

import com.assestment.kis.domain.core.TimeProvider
import com.assestment.kis.domain.notification.DistractionNotifier
import com.assestment.kis.domain.notification.NotificationThrottle
import com.assestment.kis.domain.permission.AppPermission
import com.assestment.kis.domain.permission.PermissionChecker
import com.assestment.kis.domain.session.DistractionEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

/**
 * Streams distraction events for the UI to count, and as a side effect notifies the user —
 * subject to notification permission and a per-type cooldown. A fresh throttle per [invoke]
 * keeps each session's notification state independent.
 */
class ObserveDistractionsUseCase(
    private val distractionMonitor: DistractionMonitor,
    private val notifier: DistractionNotifier,
    private val permissionChecker: PermissionChecker,
    private val timeProvider: TimeProvider,
    private val config: FocusConfig,
) {
    operator fun invoke(): Flow<DistractionEvent> {
        val throttle = NotificationThrottle(config.notificationCooldownMillis)
        return distractionMonitor.observe().onEach { event ->
            if (permissionChecker.isGranted(AppPermission.NOTIFICATIONS) &&
                throttle.shouldNotify(event.type, timeProvider.now())
            ) {
                notifier.notifyDistraction(event)
            }
        }
    }
}
