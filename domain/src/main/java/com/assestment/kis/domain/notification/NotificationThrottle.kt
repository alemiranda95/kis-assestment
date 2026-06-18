package com.assestment.kis.domain.notification

import com.assestment.kis.domain.session.DistractionType

/**
 * Decouples how often the user is *notified* from how many events are *logged*: at most one
 * notification per type per [cooldownMillis]. Distraction counts are unaffected. Stateful — use
 * one instance per session.
 */
class NotificationThrottle(private val cooldownMillis: Long) {

    private val lastNotifiedAt = mutableMapOf<DistractionType, Long>()

    fun shouldNotify(type: DistractionType, nowMillis: Long): Boolean {
        val last = lastNotifiedAt[type]
        if (last != null && nowMillis - last < cooldownMillis) return false
        lastNotifiedAt[type] = nowMillis
        return true
    }
}
