package com.assestment.kis.domain.util

/** Injected clock so time-dependent logic (debounce, throttle) stays deterministic in tests. */
fun interface TimeProvider {
    fun now(): Long
}
