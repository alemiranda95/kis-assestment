package com.assestment.kis.data.session.remote

import com.assestment.kis.domain.util.DataError
import com.assestment.kis.domain.util.EmptyResult
import com.assestment.kis.domain.util.Result
import com.assestment.kis.domain.session.FocusSession
import kotlinx.coroutines.delay

/**
 * In-memory stand-in for the mock REST API — the runtime default under Option B, so the app is
 * self-contained. The real [RetrofitRemoteSessionDataSource] implements the same contract and is
 * covered by the MockWebServer tests. Starts empty (history shows the empty state until the user
 * completes a session); it resets each launch, which is realistic for a mock backend (Room is the
 * durable source of truth).
 */
class FakeRemoteSessionDataSource : RemoteSessionDataSource {

    private val store = mutableListOf<FocusSession>()

    override suspend fun postSession(session: FocusSession): EmptyResult<DataError> {
        store.add(0, session.copy(synced = true))
        return Result.Success(Unit)
    }

    override suspend fun getSessions(): Result<List<FocusSession>, DataError> {
        delay(SIMULATED_LATENCY_MILLIS)
        return Result.Success(store.toList())
    }

    override suspend fun getSession(id: String): Result<FocusSession, DataError> =
        store.find { it.id == id }?.let { Result.Success(it) } ?: Result.Error(DataError.Network.NOT_FOUND)

    private companion object {
        const val SIMULATED_LATENCY_MILLIS = 200L
    }
}
