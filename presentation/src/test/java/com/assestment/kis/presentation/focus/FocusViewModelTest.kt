package com.assestment.kis.presentation.focus

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.assestment.kis.domain.detection.DistractionMonitor
import com.assestment.kis.domain.detection.FocusConfig
import com.assestment.kis.domain.detection.MotionSource
import com.assestment.kis.domain.detection.NoiseSource
import com.assestment.kis.domain.detection.ObserveDistractionsUseCase
import com.assestment.kis.domain.permission.CheckPermissionUseCase
import com.assestment.kis.domain.session.FocusSession
import com.assestment.kis.domain.session.GetSessionDetailUseCase
import com.assestment.kis.domain.session.GetSessionHistoryUseCase
import com.assestment.kis.domain.session.StartFocusSessionUseCase
import com.assestment.kis.domain.session.StopFocusSessionUseCase
import com.assestment.kis.domain.util.IdGenerator
import com.assestment.kis.domain.util.TimeProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FocusViewModelTest {

    private val noiseFlow = MutableSharedFlow<Float>(extraBufferCapacity = 16)
    private val repository = FakeFocusSessionRepository()
    private val notifier = FakeDistractionNotifier()
    private val permissionChecker = FakePermissionChecker(granted = true)
    private val timeProvider = TimeProvider { 1_000L }

    private lateinit var viewModel: FocusViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val config = FocusConfig.Default
        val monitor = DistractionMonitor(
            noiseSource = { noiseFlow },
            motionSource = { emptyFlow() },
            config = config,
            timeProvider = timeProvider,
        )
        viewModel = FocusViewModel(
            startFocusSession = StartFocusSessionUseCase(timeProvider, IdGenerator { "test-id" }),
            stopFocusSession = StopFocusSessionUseCase(repository, timeProvider),
            observeDistractions = ObserveDistractionsUseCase(monitor, notifier, permissionChecker, timeProvider, config),
            getSessionHistory = GetSessionHistoryUseCase(repository),
            getSessionDetail = GetSessionDetailUseCase(repository),
            checkPermission = CheckPermissionUseCase(permissionChecker),
            timeProvider = timeProvider,
            savedStateHandle = SavedStateHandle(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Cancels the ViewModel's perpetual session coroutines (timer/monitor) at the end of each test,
    // so runTest's final advanceUntilIdle doesn't loop on the ticking timer forever.
    private fun vmTest(body: suspend TestScope.() -> Unit) = runTest {
        try {
            body()
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    @Test
    fun `start activates the session`() = vmTest {
        viewModel.onAction(FocusAction.StartSession)

        assertThat(viewModel.state.value.sessionActive).isTrue()
    }

    @Test
    fun `a distraction increments the count and notifies the user`() = vmTest {
        viewModel.onAction(FocusAction.StartSession)

        noiseFlow.emit(2000f)

        assertThat(viewModel.state.value.noiseCount).isEqualTo(1)
        assertThat(notifier.notified).hasSize(1)
    }

    @Test
    fun `notifications are not sent when the permission is denied`() = vmTest {
        permissionChecker.granted = false
        viewModel.onAction(FocusAction.StartSession)

        noiseFlow.emit(2000f)

        assertThat(viewModel.state.value.noiseCount).isEqualTo(1)
        assertThat(notifier.notified).hasSize(0)
    }

    @Test
    fun `stop sync failure surfaces a message and returns to idle`() = vmTest {
        repository.shouldReturnError = true

        viewModel.events.test {
            viewModel.onAction(FocusAction.StartSession)
            viewModel.onAction(FocusAction.StopSession)
            assertThat(awaitItem()).isInstanceOf(FocusEvent.ShowMessage::class)
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(viewModel.state.value.sessionActive).isFalse()
    }

    @Test
    fun `opening history loads past sessions`() = vmTest {
        repository.history = listOf(
            FocusSession(id = "s1", startTimeMillis = 0, endTimeMillis = 1_000, events = emptyList(), synced = true),
        )

        viewModel.onAction(FocusAction.OpenHistory)

        assertThat(viewModel.state.value.historyVisible).isTrue()
        assertThat(viewModel.state.value.history).hasSize(1)
    }

    @Test
    fun `history load failure sets an error`() = vmTest {
        repository.shouldReturnError = true

        viewModel.onAction(FocusAction.OpenHistory)

        assertThat(viewModel.state.value.historyError).isNotNull()
        assertThat(viewModel.state.value.historyLoading).isFalse()
    }

    @Test
    fun `denying the microphone disables noise detection`() = vmTest {
        viewModel.onAction(FocusAction.MicPermissionResult(granted = false))

        assertThat(viewModel.state.value.noiseDetectionAvailable).isFalse()
    }

    @Test
    fun `granting the microphone enables noise detection`() = vmTest {
        viewModel.onAction(FocusAction.MicPermissionResult(granted = true))

        assertThat(viewModel.state.value.noiseDetectionAvailable).isTrue()
    }

    @Test
    fun `screen start requests the permissions that are missing`() = vmTest {
        permissionChecker.granted = false

        viewModel.events.test {
            viewModel.onAction(FocusAction.ScreenStarted)
            val emitted = listOf(awaitItem(), awaitItem())
            assertThat(emitted).contains(FocusEvent.RequestMicPermission)
            assertThat(emitted).contains(FocusEvent.RequestNotificationPermission)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
