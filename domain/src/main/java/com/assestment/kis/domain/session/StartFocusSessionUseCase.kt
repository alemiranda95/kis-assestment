package com.assestment.kis.domain.session

import com.assestment.kis.domain.util.IdGenerator
import com.assestment.kis.domain.util.TimeProvider
import com.assestment.kis.domain.session.FocusSession

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
