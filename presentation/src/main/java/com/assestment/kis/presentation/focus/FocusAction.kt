package com.assestment.kis.presentation.focus

sealed interface FocusAction {
    data object StartSession : FocusAction
    data object StopSession : FocusAction
    data object OpenHistory : FocusAction
    data object CloseHistory : FocusAction
    data object RetryHistory : FocusAction
    data class SelectSession(val id: String) : FocusAction
    data object CloseSessionDetail : FocusAction
    data class MicPermissionResult(val granted: Boolean) : FocusAction
    data class NotificationPermissionResult(val granted: Boolean) : FocusAction
}
