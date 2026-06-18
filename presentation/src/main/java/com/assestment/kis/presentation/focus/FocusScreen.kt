package com.assestment.kis.presentation.focus

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.assestment.kis.presentation.R
import com.assestment.kis.presentation.core.designsystem.theme.KisTheme
import com.assestment.kis.presentation.core.ui.ObserveAsEvents
import com.assestment.kis.presentation.core.ui.formatDuration
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun FocusRoot(viewModel: FocusViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onAction(FocusAction.MicPermissionResult(granted))
    }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onAction(FocusAction.NotificationPermissionResult(granted))
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is FocusEvent.ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
            FocusEvent.RequestMicPermission -> micLauncher.launch(Manifest.permission.RECORD_AUDIO)
            FocusEvent.RequestNotificationPermission ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
        }
    }

    FocusScreen(state = state, onAction = viewModel::onAction, snackbarHostState = snackbarHostState)
}

@Composable
fun FocusScreen(
    state: FocusState,
    onAction: (FocusAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.focus_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    if (state.sessionActive) R.string.focus_status_active else R.string.focus_status_idle,
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(32.dp))
            DurationDisplay(state.elapsedMillis)
            Spacer(Modifier.height(24.dp))
            StatsRow(state)

            if (!state.noiseDetectionAvailable) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.focus_noise_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    onAction(if (state.sessionActive) FocusAction.StopSession else FocusAction.StartSession)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text(
                    stringResource(if (state.sessionActive) R.string.focus_stop else R.string.focus_start),
                )
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { onAction(FocusAction.OpenHistory) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Text(stringResource(R.string.focus_history_button))
            }
        }
    }

    if (state.historyVisible) {
        HistorySheet(state = state, onAction = onAction)
    }
}

@Composable
private fun DurationDisplay(elapsedMillis: Long) {
    val value = formatDuration(elapsedMillis)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.focus_duration_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.displayMedium)
    }
}

@Composable
private fun StatsRow(state: FocusState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(label = stringResource(R.string.focus_distractions_label), value = state.distractionCount)
        StatCard(label = stringResource(R.string.focus_noise_label), value = state.noiseCount)
        StatCard(label = stringResource(R.string.focus_movement_label), value = state.movementCount)
    }
}

@Composable
private fun RowScope.StatCard(label: String, value: Int) {
    Card(
        modifier = Modifier
            .weight(1f)
            // One spoken label per card instead of two disconnected texts.
            .clearAndSetSemantics { contentDescription = "$label: $value" },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = value.toString(), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FocusScreenIdlePreview() {
    KisTheme {
        FocusScreen(state = FocusState(), onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun FocusScreenActivePreview() {
    KisTheme {
        FocusScreen(
            state = FocusState(sessionActive = true, elapsedMillis = 125_000, noiseCount = 3, movementCount = 1),
            onAction = {},
        )
    }
}
