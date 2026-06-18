package com.assestment.kis.data.session.remote

import com.assestment.kis.data.core.network.safeCall
import com.assestment.kis.data.core.network.safeCallEmpty
import com.assestment.kis.data.session.mapper.toDomain
import com.assestment.kis.data.session.mapper.toDto
import com.assestment.kis.domain.core.DataError
import com.assestment.kis.domain.core.EmptyResult
import com.assestment.kis.domain.core.Result
import com.assestment.kis.domain.core.map
import com.assestment.kis.domain.session.FocusSession

class RetrofitRemoteSessionDataSource(private val api: SessionApi) : RemoteSessionDataSource {

    override suspend fun postSession(session: FocusSession): EmptyResult<DataError> =
        safeCallEmpty { api.postSession(session.toDto()) }

    override suspend fun getSessions(): Result<List<FocusSession>, DataError> =
        safeCall { api.getSessions() }.map { dtos -> dtos.map { it.toDomain() } }

    override suspend fun getSession(id: String): Result<FocusSession, DataError> =
        safeCall { api.getSession(id) }.map { it.toDomain() }
}
