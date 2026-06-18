package com.assestment.kis.domain.permission

class CheckPermissionUseCase(private val permissionChecker: PermissionChecker) {
    operator fun invoke(permission: AppPermission): Boolean = permissionChecker.isGranted(permission)
}
