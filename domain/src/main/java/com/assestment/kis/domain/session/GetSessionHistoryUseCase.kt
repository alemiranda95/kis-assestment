package com.assestment.kis.domain.session

import com.assestment.kis.domain.util.DataError
import com.assestment.kis.domain.util.Result

class GetSessionHistoryUseCase(private val repository: FocusSessionRepository) {
    suspend operator fun invoke(): Result<List<FocusSession>, DataError> = repository.getHistory()
}
