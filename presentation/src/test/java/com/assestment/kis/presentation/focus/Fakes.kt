package com.assestment.kis.presentation.focus

import com.assestment.kis.domain.util.DataError
import com.assestment.kis.domain.util.Result
import com.assestment.kis.domain.session.DistractionEvent
import com.assestment.kis.domain.session.FocusSession
import com.assestment.kis.domain.notification.DistractionNotifier
import com.assestment.kis.domain.permission.AppPermission
import com.assestment.kis.domain.permission.PermissionChecker
import com.assestment.kis.domain.session.FocusSessionRepository

class FakeFocusSessionRepository : FocusSessionRepository {
    var shouldReturnError = false
    var history = emptyList<FocusSession>()
    val saved = mutableListOf<FocusSession>()

    override suspend fun saveAndSync(session: FocusSession): Result<FocusSession, DataError> {
        if (shouldReturnError) return Result.Error(DataError.Network.NO_INTERNET)
        val stored = session.copy(synced = true)
        saved.add(stored)
        return Result.Success(stored)
    }

    override suspend fun getHistory(): Result<List<FocusSession>, DataError> =
        if (shouldReturnError) Result.Error(DataError.Network.NO_INTERNET) else Result.Success(history)

    override suspend fun getSession(id: String): Result<FocusSession, DataError> =
        history.find { it.id == id }?.let { Result.Success(it) } ?: Result.Error(DataError.Local.NOT_FOUND)
}

class FakeDistractionNotifier : DistractionNotifier {
    val notified = mutableListOf<DistractionEvent>()
    override fun notifyDistraction(event: DistractionEvent) {
        notified.add(event)
    }
}

class FakePermissionChecker(var granted: Boolean = true) : PermissionChecker {
    override fun isGranted(permission: AppPermission): Boolean = granted
}
