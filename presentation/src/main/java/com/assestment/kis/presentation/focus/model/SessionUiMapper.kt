package com.assestment.kis.presentation.focus.model

import com.assestment.kis.domain.session.DistractionEvent
import com.assestment.kis.domain.session.FocusSession
import com.assestment.kis.presentation.core.ui.formatDuration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val zone: ZoneId = ZoneId.systemDefault()
private val dateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, HH:mm")
private val clockFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

fun FocusSession.toSessionUi(): SessionUi {
    val end = endTimeMillis ?: startTimeMillis
    return SessionUi(
        id = id,
        startLabel = format(startTimeMillis, dateTimeFormat),
        durationLabel = formatDuration(end - startTimeMillis),
        noiseCount = noiseCount,
        movementCount = movementCount,
        totalCount = distractionCount,
        synced = synced,
        events = events.map { it.toUi() },
    )
}

private fun DistractionEvent.toUi(): DistractionEventUi =
    DistractionEventUi(type = type, timeLabel = format(timestampMillis, clockFormat))

private fun format(millis: Long, formatter: DateTimeFormatter): String =
    Instant.ofEpochMilli(millis).atZone(zone).format(formatter)
