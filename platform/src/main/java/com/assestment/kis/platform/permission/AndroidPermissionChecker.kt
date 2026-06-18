package com.assestment.kis.platform.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.assestment.kis.domain.permission.AppPermission
import com.assestment.kis.domain.permission.PermissionChecker

class AndroidPermissionChecker(private val context: Context) : PermissionChecker {

    override fun isGranted(permission: AppPermission): Boolean = when (permission) {
        AppPermission.MICROPHONE -> isGranted(Manifest.permission.RECORD_AUDIO)
        AppPermission.NOTIFICATIONS ->
            // POST_NOTIFICATIONS is only a runtime permission on Android 13+.
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                isGranted(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
