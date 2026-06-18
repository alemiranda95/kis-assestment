package com.assestment.kis.presentation.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.assestment.kis.domain.session.DistractionType
import com.assestment.kis.presentation.R
import com.assestment.kis.presentation.focus.model.DistractionEventUi
import com.assestment.kis.presentation.focus.model.SessionUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(state: FocusState, onAction: (FocusAction) -> Unit) {
    ModalBottomSheet(onDismissRequest = { onAction(FocusAction.CloseHistory) }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            val selected = state.selectedSession
            if (selected != null) {
                SessionDetail(selected, onBack = { onAction(FocusAction.CloseSessionDetail) })
            } else {
                HistoryList(state, onAction)
            }
        }
    }
}

@Composable
private fun HistoryList(state: FocusState, onAction: (FocusAction) -> Unit) {
    Text(stringResource(R.string.focus_history_title), style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(16.dp))
    when {
        state.historyLoading -> Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
            CircularProgressIndicator()
        }

        state.historyError != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = state.historyError.asString(),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = { onAction(FocusAction.RetryHistory) }) {
                Text(stringResource(R.string.focus_history_retry))
            }
        }

        state.history.isEmpty() -> Text(stringResource(R.string.focus_history_empty))

        else -> LazyColumn(
            modifier = Modifier.heightIn(max = 480.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.history, key = { it.id }) { session ->
                SessionRow(session) { onAction(FocusAction.SelectSession(session.id)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionRow(session: SessionUi, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(session.startLabel, style = MaterialTheme.typography.titleSmall)
                SyncBadge(session.synced)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${stringResource(R.string.focus_duration_label)} ${session.durationLabel}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "${stringResource(R.string.focus_noise_label)} ${session.noiseCount}" +
                    " · ${stringResource(R.string.focus_movement_label)} ${session.movementCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SyncBadge(synced: Boolean) {
    val label = stringResource(if (synced) R.string.focus_session_synced else R.string.focus_session_unsynced)
    val description = stringResource(if (synced) R.string.cd_session_synced else R.string.cd_session_unsynced)
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = if (synced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        modifier = Modifier.semantics { contentDescription = description },
    )
}

@Composable
private fun SessionDetail(session: SessionUi, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onBack) { Text(stringResource(R.string.focus_back)) }
        Text(session.startLabel, style = MaterialTheme.typography.titleLarge)
    }
    Spacer(Modifier.height(8.dp))
    Text(
        text = "${stringResource(R.string.focus_session_distractions)}: ${session.totalCount}" +
            " (${stringResource(R.string.focus_noise_label)} ${session.noiseCount}," +
            " ${stringResource(R.string.focus_movement_label)} ${session.movementCount})",
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(Modifier.height(16.dp))
    LazyColumn(
        modifier = Modifier.heightIn(max = 360.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(session.events) { event -> EventRow(event) }
    }
}

@Composable
private fun EventRow(event: DistractionEventUi) {
    val label = stringResource(
        if (event.type == DistractionType.NOISE) R.string.distraction_noise else R.string.distraction_movement,
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(event.timeLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
