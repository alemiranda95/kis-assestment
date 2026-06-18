package com.assestment.kis.domain.session

import com.assestment.kis.domain.util.DataError
import com.assestment.kis.domain.util.Result
import com.assestment.kis.domain.session.FocusSession

interface FocusSessionRepository {
    /** Persists the session locally, then attempts to sync it to the API. Returns the stored session. */
    suspend fun saveAndSync(session: FocusSession): Result<FocusSession, DataError>

    suspend fun getHistory(): Result<List<FocusSession>, DataError>

    suspend fun getSession(id: String): Result<FocusSession, DataError>
}
