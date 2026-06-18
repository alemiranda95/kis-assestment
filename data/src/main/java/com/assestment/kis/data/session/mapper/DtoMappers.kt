package com.assestment.kis.data.session.mapper

import com.assestment.kis.data.session.remote.DistractionEventDto
import com.assestment.kis.data.session.remote.SessionDto
import com.assestment.kis.domain.session.DistractionEvent
import com.assestment.kis.domain.session.DistractionType
import com.assestment.kis.domain.session.FocusSession

fun SessionDto.toDomain(): FocusSession = FocusSession(
    id = id,
    startTimeMillis = startTimeMillis,
    endTimeMillis = endTimeMillis,
    events = events.map { it.toDomain() },
    synced = synced,
)

fun DistractionEventDto.toDomain(): DistractionEvent =
    DistractionEvent(DistractionType.valueOf(type), magnitude, timestampMillis)

fun FocusSession.toDto(): SessionDto = SessionDto(
    id = id,
    startTimeMillis = startTimeMillis,
    endTimeMillis = endTimeMillis,
    events = events.map { it.toDto() },
    synced = synced,
)

fun DistractionEvent.toDto(): DistractionEventDto =
    DistractionEventDto(type = type.name, magnitude = magnitude, timestampMillis = timestampMillis)
