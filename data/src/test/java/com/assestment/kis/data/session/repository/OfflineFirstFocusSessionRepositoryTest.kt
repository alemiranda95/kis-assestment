package com.assestment.kis.data.session.repository

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.assestment.kis.data.session.local.SessionLocalDataSource
import com.assestment.kis.data.session.remote.RemoteSessionDataSource
import com.assestment.kis.domain.core.DataError
import com.assestment.kis.domain.core.EmptyResult
import com.assestment.kis.domain.core.Result
import com.assestment.kis.domain.session.FocusSession
import kotlinx.coroutines.test.runTest
import org.junit.Test

private class FakeLocal : SessionLocalDataSource {
    val stored = linkedMapOf<String, FocusSession>()
    override suspend fun upsert(session: FocusSession) {
        stored[session.id] = session
    }
    override suspend fun getAll(): List<FocusSession> = stored.values.toList()
    override suspend fun getById(id: String): FocusSession? = stored[id]
}

private class FakeRemote : RemoteSessionDataSource {
    var failPost = false
    var failGet = false
    var sessions = emptyList<FocusSession>()

    override suspend fun postSession(session: FocusSession): EmptyResult<DataError> =
        if (failPost) Result.Error(DataError.Network.NO_INTERNET) else Result.Success(Unit)

    override suspend fun getSessions(): Result<List<FocusSession>, DataError> =
        if (failGet) Result.Error(DataError.Network.NO_INTERNET) else Result.Success(sessions)

    override suspend fun getSession(id: String): Result<FocusSession, DataError> =
        sessions.find { it.id == id }?.let { Result.Success(it) } ?: Result.Error(DataError.Network.NOT_FOUND)
}

class OfflineFirstFocusSessionRepositoryTest {

    private val local = FakeLocal()
    private val remote = FakeRemote()
    private val repository = OfflineFirstFocusSessionRepository(local, remote)

    private fun session(id: String) =
        FocusSession(id = id, startTimeMillis = 0, endTimeMillis = 1000, events = emptyList(), synced = false)

    @Test
    fun `saveAndSync persists locally and marks synced on success`() = runTest {
        val result = repository.saveAndSync(session("s1"))

        assertThat(result).isEqualTo(Result.Success(session("s1").copy(synced = true)))
        assertThat(local.stored["s1"]?.synced).isEqualTo(true)
    }

    @Test
    fun `saveAndSync keeps the session locally as unsynced when sync fails`() = runTest {
        remote.failPost = true

        val result = repository.saveAndSync(session("s1"))

        assertThat(result).isInstanceOf(Result.Error::class)
        assertThat(local.stored.containsKey("s1")).isTrue()
        assertThat(local.stored["s1"]?.synced).isEqualTo(false)
    }

    @Test
    fun `getHistory caches the remote sessions into local and returns them`() = runTest {
        remote.sessions = listOf(session("r1").copy(synced = true))

        val result = repository.getHistory()

        assertThat((result as Result.Success).data.map { it.id }).contains("r1")
        assertThat(local.stored.containsKey("r1")).isTrue()
    }

    @Test
    fun `getHistory falls back to the local cache on network failure`() = runTest {
        local.stored["c1"] = session("c1").copy(synced = true)
        remote.failGet = true

        val result = repository.getHistory()

        assertThat((result as Result.Success).data.map { it.id }).contains("c1")
    }
}
