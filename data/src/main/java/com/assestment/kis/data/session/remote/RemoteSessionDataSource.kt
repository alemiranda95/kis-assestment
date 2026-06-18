package com.assestment.kis.data.session.remote

import com.assestment.kis.domain.core.DataError
import com.assestment.kis.domain.core.EmptyResult
import com.assestment.kis.domain.core.Result
import com.assestment.kis.domain.session.FocusSession

interface RemoteSessionDataSource {
    suspend fun postSession(session: FocusSession): EmptyResult<DataError>
    suspend fun getSessions(): Result<List<FocusSession>, DataError>
    suspend fun getSession(id: String): Result<FocusSession, DataError>
}
