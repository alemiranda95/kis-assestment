package com.assestment.kis.domain.session.repository

import com.assestment.kis.domain.core.DataError
import com.assestment.kis.domain.core.Result
import com.assestment.kis.domain.session.model.FocusSession

interface FocusSessionRepository {
    /** Persists the session locally, then attempts to sync it to the API. Returns the stored session. */
    suspend fun saveAndSync(session: FocusSession): Result<FocusSession, DataError>

    suspend fun getHistory(): Result<List<FocusSession>, DataError>

    suspend fun getSession(id: String): Result<FocusSession, DataError>
}
