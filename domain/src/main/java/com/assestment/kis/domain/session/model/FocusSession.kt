package com.assestment.kis.domain.session.model

data class FocusSession(
    val id: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val events: List<DistractionEvent>,
    val synced: Boolean = false,
) {
    val isActive: Boolean get() = endTimeMillis == null

    val noiseCount: Int get() = events.count { it.type == DistractionType.NOISE }
    val movementCount: Int get() = events.count { it.type == DistractionType.MOVEMENT }
    val distractionCount: Int get() = events.size

    /** Uses [nowMillis] while the session is still active; the recorded end time once stopped. */
    fun durationMillis(nowMillis: Long): Long = (endTimeMillis ?: nowMillis) - startTimeMillis
}
