package com.assestment.kis.domain.session.usecase

import com.assestment.kis.domain.core.DataError
import com.assestment.kis.domain.core.Result
import com.assestment.kis.domain.core.TimeProvider
import com.assestment.kis.domain.session.model.FocusSession
import com.assestment.kis.domain.session.repository.FocusSessionRepository

class StopFocusSessionUseCase(
    private val repository: FocusSessionRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(session: FocusSession): Result<FocusSession, DataError> {
        val completed = session.copy(endTimeMillis = timeProvider.now())
        return repository.saveAndSync(completed)
    }
}
