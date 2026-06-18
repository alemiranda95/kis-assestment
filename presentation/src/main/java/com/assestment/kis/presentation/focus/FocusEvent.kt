package com.assestment.kis.presentation.focus

import com.assestment.kis.presentation.core.ui.UiText

sealed interface FocusEvent {
    data class ShowMessage(val message: UiText) : FocusEvent
    data object RequestMicPermission : FocusEvent
    data object RequestNotificationPermission : FocusEvent
}
