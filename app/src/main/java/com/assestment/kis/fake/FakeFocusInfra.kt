package com.assestment.kis.fake

import com.assestment.kis.domain.session.DistractionEvent
import com.assestment.kis.domain.notification.DistractionNotifier
import com.assestment.kis.domain.permission.AppPermission
import com.assestment.kis.domain.permission.PermissionChecker

/** No-op until the real platform notifier (Phase 5). */
class NoOpDistractionNotifier : DistractionNotifier {
    override fun notifyDistraction(event: DistractionEvent) = Unit
}

/** Treats everything as granted for the demo until the real platform permission checker (Phase 5). */
class GrantAllPermissionChecker : PermissionChecker {
    override fun isGranted(permission: AppPermission): Boolean = true
}
