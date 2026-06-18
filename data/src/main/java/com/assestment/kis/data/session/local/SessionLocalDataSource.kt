package com.assestment.kis.data.session.local

import com.assestment.kis.domain.session.FocusSession

interface SessionLocalDataSource {
    suspend fun upsert(session: FocusSession)
    suspend fun getAll(): List<FocusSession>
    suspend fun getById(id: String): FocusSession?
}
