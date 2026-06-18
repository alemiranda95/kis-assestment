package com.assestment.kis.data.session.remote

import com.assestment.kis.domain.core.DataError
import com.assestment.kis.domain.core.EmptyResult
import com.assestment.kis.domain.core.Result
import com.assestment.kis.domain.session.DistractionEvent
import com.assestment.kis.domain.session.DistractionType
import com.assestment.kis.domain.session.FocusSession
import kotlinx.coroutines.delay

/**
 * In-memory stand-in for the mock REST API — the runtime default under Option B, so the app is
 * self-contained. The real [RetrofitRemoteSessionDataSource] implements the same contract and is
 * covered by the MockWebServer tests. Seed data keeps history non-empty on first run; it resets
 * each launch, which is realistic for a mock backend (Room is the durable source of truth).
 */
class FakeRemoteSessionDataSource : RemoteSessionDataSource {

    private val store = mutableListOf(
        FocusSession(
            id = "sample-1",
            startTimeMillis = 1_718_600_000_000,
            endTimeMillis = 1_718_601_500_000,
            events = listOf(
                DistractionEvent(DistractionType.NOISE, 2100f, 1_718_600_200_000),
                DistractionEvent(DistractionType.MOVEMENT, 5.5f, 1_718_600_900_000),
            ),
            synced = true,
        ),
    )

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
