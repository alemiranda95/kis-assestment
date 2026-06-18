package com.assestment.kis.data.network

import com.assestment.kis.domain.util.DataError
import com.assestment.kis.domain.util.EmptyResult
import com.assestment.kis.domain.util.Result
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.coroutineContext

/** Wraps a Retrofit call that returns a body, mapping transport/HTTP failures to [DataError.Network]. */
suspend inline fun <T> safeCall(execute: () -> Response<T>): Result<T, DataError.Network> {
    val response = runCatching { execute() }.getOrElse { throwable ->
        coroutineContext.ensureActive()
        return Result.Error(throwable.toNetworkError())
    }
    return if (response.isSuccessful) {
        response.body()?.let { Result.Success(it) } ?: Result.Error(DataError.Network.UNKNOWN)
    } else {
        Result.Error(response.code().toNetworkError())
    }
}

/** Variant for calls with no meaningful body (e.g. POST); success is decided by the HTTP status alone. */
suspend inline fun safeCallEmpty(execute: () -> Response<*>): EmptyResult<DataError.Network> {
    val response = runCatching { execute() }.getOrElse { throwable ->
        coroutineContext.ensureActive()
        return Result.Error(throwable.toNetworkError())
    }
    return if (response.isSuccessful) Result.Success(Unit) else Result.Error(response.code().toNetworkError())
}

fun Throwable.toNetworkError(): DataError.Network = when (this) {
    is IOException -> DataError.Network.NO_INTERNET
    is SerializationException -> DataError.Network.SERIALIZATION
    else -> DataError.Network.UNKNOWN
}

fun Int.toNetworkError(): DataError.Network = when (this) {
    401 -> DataError.Network.UNAUTHORIZED
    403 -> DataError.Network.FORBIDDEN
    404 -> DataError.Network.NOT_FOUND
    408 -> DataError.Network.REQUEST_TIMEOUT
    409 -> DataError.Network.CONFLICT
    413 -> DataError.Network.PAYLOAD_TOO_LARGE
    429 -> DataError.Network.TOO_MANY_REQUESTS
    in 500..599 -> DataError.Network.SERVER_ERROR
    else -> DataError.Network.UNKNOWN
}
