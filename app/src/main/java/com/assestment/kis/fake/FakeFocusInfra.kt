package com.assestment.kis.fake

import com.assestment.kis.domain.core.DataError
import com.assestment.kis.domain.core.Result
import com.assestment.kis.domain.session.DistractionEvent
import com.assestment.kis.domain.session.DistractionType
import com.assestment.kis.domain.session.FocusSession
import com.assestment.kis.domain.notification.DistractionNotifier
import com.assestment.kis.domain.permission.AppPermission
import com.assestment.kis.domain.permission.PermissionChecker
import com.assestment.kis.domain.session.FocusSessionRepository
import kotlinx.coroutines.delay

/** No-op until the real platform notifier (Phase 5). */
class NoOpDistractionNotifier : DistractionNotifier {
    override fun notifyDistraction(event: DistractionEvent) = Unit
}

/** Treats everything as granted for the Phase 3 demo. */
class GrantAllPermissionChecker : PermissionChecker {
    override fun isGranted(permission: AppPermission): Boolean = true
}

/** In-memory repository standing in for Room + remote until Phase 4; seeded so history is non-empty. */
class InMemoryFocusSessionRepository : FocusSessionRepository {

    private val sessions = mutableListOf(
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

    override suspend fun saveAndSync(session: FocusSession): Result<FocusSession, DataError> {
        val stored = session.copy(synced = true)
        sessions.add(0, stored)
        return Result.Success(stored)
    }

    override suspend fun getHistory(): Result<List<FocusSession>, DataError> {
        delay(300)
        return Result.Success(sessions.toList())
    }

    override suspend fun getSession(id: String): Result<FocusSession, DataError> {
        delay(150)
        return sessions.find { it.id == id }
            ?.let { Result.Success(it) }
            ?: Result.Error(DataError.Local.NOT_FOUND)
    }
}
