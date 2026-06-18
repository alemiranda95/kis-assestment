package com.assestment.kis.data.session.remote

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.assestment.kis.domain.core.DataError
import com.assestment.kis.domain.core.Result
import com.assestment.kis.domain.session.FocusSession
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class RetrofitRemoteSessionDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: RetrofitRemoteSessionDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        dataSource = RetrofitRemoteSessionDataSource(retrofit.create(SessionApi::class.java))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getSessions parses the response body`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """[{"id":"s1","startTimeMillis":0,"endTimeMillis":1000,"events":[],"synced":true}]""",
            ),
        )

        val result = dataSource.getSessions()

        assertThat(result).isInstanceOf(Result.Success::class)
        val sessions = (result as Result.Success).data
        assertThat(sessions).hasSize(1)
        assertThat(sessions.first().id).isEqualTo("s1")
    }

    @Test
    fun `postSession issues a POST to the sessions endpoint`() = runTest {
        server.enqueue(MockResponse().setResponseCode(201))

        val result = dataSource.postSession(
            FocusSession(id = "s1", startTimeMillis = 0, endTimeMillis = 1000, events = emptyList(), synced = false),
        )

        assertThat(result).isEqualTo(Result.Success(Unit))
        val recorded = server.takeRequest()
        assertThat(recorded.method).isEqualTo("POST")
        assertThat(recorded.path).isEqualTo("/sessions")
    }

    @Test
    fun `getSession maps a 404 to NOT_FOUND`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val result = dataSource.getSession("missing")

        assertThat(result).isEqualTo(Result.Error(DataError.Network.NOT_FOUND))
    }

    @Test
    fun `a 500 maps to SERVER_ERROR`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = dataSource.getSessions()

        assertThat(result).isEqualTo(Result.Error(DataError.Network.SERVER_ERROR))
    }
}
