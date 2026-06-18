package com.assestment.kis.domain.session.permission

enum class AppPermission {
    MICROPHONE,
    NOTIFICATIONS,
}

/** Queries current grant state of runtime permissions. Implemented in the platform layer. */
interface PermissionChecker {
    fun isGranted(permission: AppPermission): Boolean
}
