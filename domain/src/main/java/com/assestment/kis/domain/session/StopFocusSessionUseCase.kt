package com.assestment.kis.domain.session

import com.assestment.kis.domain.util.DataError
import com.assestment.kis.domain.util.Result
import com.assestment.kis.domain.util.TimeProvider
import com.assestment.kis.domain.session.FocusSession
import com.assestment.kis.domain.session.FocusSessionRepository

class StopFocusSessionUseCase(
    private val repository: FocusSessionRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(session: FocusSession): Result<FocusSession, DataError> {
        val completed = session.copy(endTimeMillis = timeProvider.now())
        return repository.saveAndSync(completed)
    }
}
