package com.assestment.kis.domain.session.notification

import com.assestment.kis.domain.session.model.DistractionEvent

/** Shows a user-facing distraction alert. Implemented in the platform layer. */
fun interface DistractionNotifier {
    fun notifyDistraction(event: DistractionEvent)
}
