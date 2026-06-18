package com.assestment.kis.domain.session.usecase

import com.assestment.kis.domain.core.IdGenerator
import com.assestment.kis.domain.core.TimeProvider
import com.assestment.kis.domain.session.model.FocusSession

class StartFocusSessionUseCase(
    private val timeProvider: TimeProvider,
    private val idGenerator: IdGenerator,
) {
    operator fun invoke(): FocusSession = FocusSession(
        id = idGenerator.newId(),
        startTimeMillis = timeProvider.now(),
        endTimeMillis = null,
        events = emptyList(),
    )
}
