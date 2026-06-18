package com.assestment.kis.data.session.local

import com.assestment.kis.data.session.mapper.toDomain
import com.assestment.kis.data.session.mapper.toEntity
import com.assestment.kis.domain.session.FocusSession

class RoomSessionLocalDataSource(private val dao: SessionDao) : SessionLocalDataSource {

    override suspend fun upsert(session: FocusSession) {
        dao.upsertSession(session.toEntity())
        dao.upsertEvents(session.events.map { it.toEntity(session.id) })
    }

    override suspend fun getAll(): List<FocusSession> =
        dao.getSessionsWithEvents().map { it.toDomain() }

    override suspend fun getById(id: String): FocusSession? =
        dao.getSessionWithEvents(id)?.toDomain()
}
