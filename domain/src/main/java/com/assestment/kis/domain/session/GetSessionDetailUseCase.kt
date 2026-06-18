package com.assestment.kis.domain.session

import com.assestment.kis.domain.util.DataError
import com.assestment.kis.domain.util.Result

class GetSessionDetailUseCase(private val repository: FocusSessionRepository) {
    suspend operator fun invoke(id: String): Result<FocusSession, DataError> = repository.getSession(id)
}
