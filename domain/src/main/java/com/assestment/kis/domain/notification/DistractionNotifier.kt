package com.assestment.kis.domain.notification

import com.assestment.kis.domain.session.DistractionEvent

/** Shows a user-facing distraction alert. Implemented in the platform layer. */
fun interface DistractionNotifier {
    fun notifyDistraction(event: DistractionEvent)
}
