package com.assestment.kis.presentation.focus

import androidx.compose.runtime.Stable
import com.assestment.kis.presentation.ui.UiText
import com.assestment.kis.presentation.focus.model.SessionUi

@Stable
data class FocusState(
    val sessionActive: Boolean = false,
    val elapsedMillis: Long = 0,
    val noiseCount: Int = 0,
    val movementCount: Int = 0,
    val noiseDetectionAvailable: Boolean = true,
    val historyVisible: Boolean = false,
    val historyLoading: Boolean = false,
    val historyError: UiText? = null,
    val history: List<SessionUi> = emptyList(),
    val selectedSession: SessionUi? = null,
) {
    val distractionCount: Int get() = noiseCount + movementCount
}
