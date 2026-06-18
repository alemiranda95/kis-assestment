package com.assestment.kis.domain.session

import com.assestment.kis.domain.core.DataError
import com.assestment.kis.domain.core.Result

class GetSessionHistoryUseCase(private val repository: FocusSessionRepository) {
    suspend operator fun invoke(): Result<List<FocusSession>, DataError> = repository.getHistory()
}
