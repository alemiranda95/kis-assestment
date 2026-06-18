package com.assestment.kis.presentation.core.ui

/** Formats a millisecond duration as mm:ss (used for both the live timer and history rows). */
fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}
