package com.assestment.kis.data.session.mapper

import com.assestment.kis.data.session.local.DistractionEventEntity
import com.assestment.kis.data.session.local.SessionEntity
import com.assestment.kis.data.session.local.SessionWithEvents
import com.assestment.kis.domain.session.DistractionEvent
import com.assestment.kis.domain.session.DistractionType
import com.assestment.kis.domain.session.FocusSession

fun SessionWithEvents.toDomain(): FocusSession = FocusSession(
    id = session.id,
    startTimeMillis = session.startTimeMillis,
    endTimeMillis = session.endTimeMillis,
    events = events.map { it.toDomain() },
    synced = session.synced,
)

fun DistractionEventEntity.toDomain(): DistractionEvent =
    DistractionEvent(DistractionType.valueOf(type), magnitude, timestampMillis)

fun FocusSession.toEntity(): SessionEntity =
    SessionEntity(id = id, startTimeMillis = startTimeMillis, endTimeMillis = endTimeMillis, synced = synced)

fun DistractionEvent.toEntity(sessionId: String): DistractionEventEntity = DistractionEventEntity(
    sessionId = sessionId,
    type = type.name,
    magnitude = magnitude,
    timestampMillis = timestampMillis,
)
