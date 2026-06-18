package com.assestment.kis.presentation.focus

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.assestment.kis.domain.core.IdGenerator
import com.assestment.kis.domain.core.TimeProvider
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

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
            noiseSource = NoiseSource { noiseFlow },
            motionSource = MotionSource { emptyFlow() },
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

    @Test
    fun `start activates the session`() = runTest {
        viewModel.onAction(FocusAction.StartSession)

        assertThat(viewModel.state.value.sessionActive).isTrue()
    }

    @Test
    fun `a distraction increments the count and notifies the user`() = runTest {
        viewModel.onAction(FocusAction.StartSession)

        noiseFlow.emit(2000f)

        assertThat(viewModel.state.value.noiseCount).isEqualTo(1)
        assertThat(notifier.notified).hasSize(1)
    }

    @Test
    fun `notifications are not sent when the permission is denied`() = runTest {
        permissionChecker.granted = false
        viewModel.onAction(FocusAction.StartSession)

        noiseFlow.emit(2000f)

        assertThat(viewModel.state.value.noiseCount).isEqualTo(1)
        assertThat(notifier.notified).hasSize(0)
    }

    @Test
    fun `stop sync failure surfaces a message and returns to idle`() = runTest {
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
    fun `opening history loads past sessions`() = runTest {
        repository.history = listOf(
            FocusSession(id = "s1", startTimeMillis = 0, endTimeMillis = 1_000, events = emptyList(), synced = true),
        )

        viewModel.onAction(FocusAction.OpenHistory)

        assertThat(viewModel.state.value.historyVisible).isTrue()
        assertThat(viewModel.state.value.history).hasSize(1)
    }

    @Test
    fun `history load failure sets an error`() = runTest {
        repository.shouldReturnError = true

        viewModel.onAction(FocusAction.OpenHistory)

        assertThat(viewModel.state.value.historyError).isNotNull()
        assertThat(viewModel.state.value.historyLoading).isFalse()
    }

    @Test
    fun `denying the microphone disables noise detection`() = runTest {
        viewModel.onAction(FocusAction.MicPermissionResult(granted = false))

        assertThat(viewModel.state.value.noiseDetectionAvailable).isFalse()
    }
}
