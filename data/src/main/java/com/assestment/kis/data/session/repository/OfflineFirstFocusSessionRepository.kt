package com.assestment.kis.data.session.repository

import com.assestment.kis.data.session.local.SessionLocalDataSource
import com.assestment.kis.data.session.remote.RemoteSessionDataSource
import com.assestment.kis.domain.core.DataError
import com.assestment.kis.domain.core.Result
import com.assestment.kis.domain.session.FocusSession
import com.assestment.kis.domain.session.FocusSessionRepository

/**
 * Room is the source of truth. On save we persist locally first (so a session is never lost) then
 * best-effort sync to the API. Reads fetch from the API, cache into Room, and return from Room,
 * falling back to the cache when the network fails.
 */
class OfflineFirstFocusSessionRepository(
    private val local: SessionLocalDataSource,
    private val remote: RemoteSessionDataSource,
) : FocusSessionRepository {

    override suspend fun saveAndSync(session: FocusSession): Result<FocusSession, DataError> {
        local.upsert(session.copy(synced = false))
        return when (val sync = remote.postSession(session)) {
            is Result.Success -> {
                val synced = session.copy(synced = true)
                local.upsert(synced)
                Result.Success(synced)
            }
            is Result.Error -> Result.Error(sync.error)
        }
    }

    override suspend fun getHistory(): Result<List<FocusSession>, DataError> =
        when (val remoteResult = remote.getSessions()) {
            is Result.Success -> {
                remoteResult.data.forEach { local.upsert(it) }
                Result.Success(local.getAll())
            }
            is Result.Error -> {
                val cached = local.getAll()
                if (cached.isNotEmpty()) Result.Success(cached) else Result.Error(remoteResult.error)
            }
        }

    override suspend fun getSession(id: String): Result<FocusSession, DataError> =
        when (val remoteResult = remote.getSession(id)) {
            is Result.Success -> {
                local.upsert(remoteResult.data)
                Result.Success(remoteResult.data)
            }
            is Result.Error -> local.getById(id)?.let { Result.Success(it) } ?: Result.Error(remoteResult.error)
        }
}
