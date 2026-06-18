package com.assestment.kis.presentation.focus

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assestment.kis.domain.util.TimeProvider
import com.assestment.kis.domain.util.onFailure
import com.assestment.kis.domain.util.onSuccess
import com.assestment.kis.domain.detection.ObserveDistractionsUseCase
import com.assestment.kis.domain.permission.AppPermission
import com.assestment.kis.domain.permission.CheckPermissionUseCase
import com.assestment.kis.domain.session.DistractionEvent
import com.assestment.kis.domain.session.FocusSession
import com.assestment.kis.domain.session.GetSessionDetailUseCase
import com.assestment.kis.domain.session.GetSessionHistoryUseCase
import com.assestment.kis.domain.session.StartFocusSessionUseCase
import com.assestment.kis.domain.session.StopFocusSessionUseCase
import com.assestment.kis.presentation.ui.toUiText
import com.assestment.kis.presentation.focus.model.toSessionUi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FocusViewModel(
    private val startFocusSession: StartFocusSessionUseCase,
    private val stopFocusSession: StopFocusSessionUseCase,
    private val observeDistractions: ObserveDistractionsUseCase,
    private val getSessionHistory: GetSessionHistoryUseCase,
    private val getSessionDetail: GetSessionDetailUseCase,
    private val checkPermission: CheckPermissionUseCase,
    private val timeProvider: TimeProvider,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(FocusState())
    val state = _state.asStateFlow()

    private val _events = Channel<FocusEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var activeSession: FocusSession? = null
    private var timerJob: Job? = null
    private var monitorJob: Job? = null

    init {
        restoreActiveSessionIfPresent()
    }

    fun onAction(action: FocusAction) {
        when (action) {
            FocusAction.StartSession -> startSession()
            FocusAction.StopSession -> stopSession()
            FocusAction.OpenHistory -> openHistory()
            FocusAction.CloseHistory -> _state.update { it.copy(historyVisible = false, selectedSession = null) }
            FocusAction.RetryHistory -> loadHistory()
            is FocusAction.SelectSession -> selectSession(action.id)
            FocusAction.CloseSessionDetail -> _state.update { it.copy(selectedSession = null) }
            is FocusAction.MicPermissionResult ->
                _state.update { it.copy(noiseDetectionAvailable = action.granted) }
            is FocusAction.NotificationPermissionResult -> Unit
        }
    }

    private fun startSession() {
        if (_state.value.sessionActive) return
        val session = startFocusSession()
        activeSession = session
        persistActiveSession(session)
        _state.update {
            it.copy(
                sessionActive = true,
                elapsedMillis = 0,
                noiseCount = 0,
                movementCount = 0,
                noiseDetectionAvailable = checkPermission(AppPermission.MICROPHONE),
            )
        }
        requestMissingPermissions()
        startTimer(session.startTimeMillis)
        startMonitoring()
    }

    private fun stopSession() {
        val session = activeSession ?: return
        stopMonitoring()
        activeSession = null
        clearPersistedSession()
        _state.update { it.copy(sessionActive = false, elapsedMillis = 0, noiseCount = 0, movementCount = 0) }
        viewModelScope.launch {
            stopFocusSession(session).onFailure { error ->
                _events.trySend(FocusEvent.ShowMessage(error.toUiText()))
            }
        }
    }

    private fun onDistraction(event: DistractionEvent) {
        val current = activeSession ?: return
        val updated = current.copy(events = current.events + event)
        activeSession = updated
        _state.update { it.copy(noiseCount = updated.noiseCount, movementCount = updated.movementCount) }
    }

    private fun openHistory() {
        _state.update { it.copy(historyVisible = true) }
        loadHistory()
    }

    private fun loadHistory() {
        _state.update { it.copy(historyLoading = true, historyError = null) }
        viewModelScope.launch {
            getSessionHistory()
                .onSuccess { sessions ->
                    _state.update { it.copy(historyLoading = false, history = sessions.map { s -> s.toSessionUi() }) }
                }
                .onFailure { error ->
                    _state.update { it.copy(historyLoading = false, historyError = error.toUiText()) }
                }
        }
    }

    private fun selectSession(id: String) {
        viewModelScope.launch {
            getSessionDetail(id)
                .onSuccess { session -> _state.update { it.copy(selectedSession = session.toSessionUi()) } }
                .onFailure { error -> _events.trySend(FocusEvent.ShowMessage(error.toUiText())) }
        }
    }

    private fun requestMissingPermissions() {
        if (!checkPermission(AppPermission.MICROPHONE)) {
            _events.trySend(FocusEvent.RequestMicPermission)
        }
        if (!checkPermission(AppPermission.NOTIFICATIONS)) {
            _events.trySend(FocusEvent.RequestNotificationPermission)
        }
    }

    private fun startTimer(startMillis: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                _state.update { it.copy(elapsedMillis = timeProvider.now() - startMillis) }
                delay(TIMER_TICK_MILLIS)
            }
        }
    }

    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = viewModelScope.launch {
            observeDistractions().collect(::onDistraction)
        }
    }

    private fun stopMonitoring() {
        timerJob?.cancel()
        timerJob = null
        monitorJob?.cancel()
        monitorJob = null
    }

    // A session survives process death with elapsed-time continuity; in-flight distraction events
    // are not restored (acceptable for the MVP — a known limitation).
    private fun persistActiveSession(session: FocusSession) {
        savedStateHandle[KEY_SESSION_ID] = session.id
        savedStateHandle[KEY_SESSION_START] = session.startTimeMillis
    }

    private fun clearPersistedSession() {
        savedStateHandle.remove<String>(KEY_SESSION_ID)
        savedStateHandle.remove<Long>(KEY_SESSION_START)
    }

    private fun restoreActiveSessionIfPresent() {
        val id = savedStateHandle.get<String>(KEY_SESSION_ID) ?: return
        val start = savedStateHandle.get<Long>(KEY_SESSION_START) ?: return
        activeSession = FocusSession(id = id, startTimeMillis = start, endTimeMillis = null, events = emptyList())
        _state.update {
            it.copy(sessionActive = true, noiseDetectionAvailable = checkPermission(AppPermission.MICROPHONE))
        }
        startTimer(start)
        startMonitoring()
    }

    private companion object {
        const val TIMER_TICK_MILLIS = 1000L
        const val KEY_SESSION_ID = "active_session_id"
        const val KEY_SESSION_START = "active_session_start"
    }
}
