package com.assestment.kis.domain.notification

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.assestment.kis.domain.session.DistractionType
import org.junit.jupiter.api.Test

class NotificationThrottleTest {

    @Test
    fun `allows the first notification of a type`() {
        val throttle = NotificationThrottle(cooldownMillis = 30_000)

        assertThat(throttle.shouldNotify(DistractionType.NOISE, nowMillis = 0)).isTrue()
    }

    @Test
    fun `suppresses notifications within the cooldown window`() {
        val throttle = NotificationThrottle(cooldownMillis = 30_000)

        throttle.shouldNotify(DistractionType.NOISE, nowMillis = 0)

        assertThat(throttle.shouldNotify(DistractionType.NOISE, nowMillis = 29_999)).isFalse()
    }

    @Test
    fun `allows again once the cooldown has elapsed`() {
        val throttle = NotificationThrottle(cooldownMillis = 30_000)

        throttle.shouldNotify(DistractionType.NOISE, nowMillis = 0)

        assertThat(throttle.shouldNotify(DistractionType.NOISE, nowMillis = 30_000)).isTrue()
    }

    @Test
    fun `throttles each type independently`() {
        val throttle = NotificationThrottle(cooldownMillis = 30_000)

        throttle.shouldNotify(DistractionType.NOISE, nowMillis = 0)

        assertThat(throttle.shouldNotify(DistractionType.MOVEMENT, nowMillis = 1_000)).isTrue()
    }
}
