package com.assestment.kis.presentation.focus

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.assestment.kis.presentation.R
import com.assestment.kis.presentation.ds.Accent
import com.assestment.kis.presentation.ds.AlertGradientBottom
import com.assestment.kis.presentation.ds.AlertGradientTop
import com.assestment.kis.presentation.ds.Dimens
import com.assestment.kis.presentation.ds.FabSurface
import com.assestment.kis.presentation.ds.FocusGradientBottom
import com.assestment.kis.presentation.ds.FocusGradientTop
import com.assestment.kis.presentation.ds.FocusLayout
import com.assestment.kis.presentation.ds.ControlSurface
import com.assestment.kis.presentation.ds.IconDisabled
import com.assestment.kis.presentation.ds.Motion
import com.assestment.kis.presentation.ds.OnAccent
import com.assestment.kis.presentation.ds.OnFocus
import com.assestment.kis.presentation.ds.OnFocusMuted
import com.assestment.kis.presentation.ds.StatSurface
import com.assestment.kis.presentation.ds.SubtleBorder
import com.assestment.kis.presentation.ds.TimerRingTrack
import com.assestment.kis.presentation.ui.ObserveAsEvents
import com.assestment.kis.presentation.ui.formatDuration
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

    LaunchedEffect(Unit) { viewModel.onAction(FocusAction.ScreenStarted) }

    FocusScreen(state = state, onAction = viewModel::onAction, snackbarHostState = snackbarHostState)
}

@Composable
fun FocusScreen(
    state: FocusState,
    onAction: (FocusAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    // Flash the background toward red whenever a new distraction is recorded, then ease back.
    val alert = remember { Animatable(0f) }
    LaunchedEffect(state.distractionCount) {
        if (state.distractionCount > 0) {
            alert.snapTo(1f)
            alert.animateTo(0f, animationSpec = tween(durationMillis = Motion.AlertFlashMillis))
        } else {
            // Count reset (e.g. session stopped mid-flash) cancels the fade above — return to calm.
            alert.snapTo(0f)
        }
    }
    val background = Brush.verticalGradient(
        listOf(
            lerp(FocusGradientTop, AlertGradientTop, alert.value),
            lerp(FocusGradientBottom, AlertGradientBottom, alert.value),
        ),
    )

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().background(background)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = Dimens.spaceXl, vertical = Dimens.spaceLg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val topWeight by animateFloatAsState(
                    if (state.sessionActive) FocusLayout.TimerWeightActive else FocusLayout.TimerWeightIdle,
                    label = "topWeight",
                )
                Spacer(Modifier.weight(topWeight))

                TimerCircle(elapsedLabel = formatDuration(state.elapsedMillis), active = state.sessionActive)

                AnimatedVisibility(
                    visible = state.sessionActive,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(Dimens.spaceXxl))
                        StatsRow(
                            noise = state.noiseCount,
                            movement = state.movementCount,
                            noiseAvailable = state.noiseDetectionAvailable,
                        )
                    }
                }

                Spacer(Modifier.weight(FocusLayout.ActionWeight))

                StartStopButton(active = state.sessionActive) {
                    onAction(if (state.sessionActive) FocusAction.StopSession else FocusAction.StartSession)
                }
            }

            SmallFloatingActionButton(
                onClick = { onAction(FocusAction.OpenHistory) },
                containerColor = FabSurface,
                contentColor = OnFocus,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(innerPadding)
                    .padding(top = Dimens.spaceSm, end = Dimens.spaceMd)
                    .minimumInteractiveComponentSize(),
            ) {
                Icon(Icons.Filled.History, contentDescription = stringResource(R.string.cd_history))
            }
        }
    }

    if (state.historyVisible) {
        HistorySheet(state = state, onAction = onAction)
    }
}

@Composable
private fun TimerCircle(elapsedLabel: String, active: Boolean) {
    val rotation by rememberInfiniteTransition(label = "ring").animateFloat(
        initialValue = FocusLayout.RingRotationStart,
        targetValue = FocusLayout.RingRotationEnd,
        animationSpec = infiniteRepeatable(tween(durationMillis = Motion.RingRotationMillis, easing = LinearEasing)),
        label = "rotation",
    )

    Box(modifier = Modifier.size(Dimens.timerDiameter), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = Dimens.ringStroke.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            drawCircle(color = TimerRingTrack, radius = diameter / 2f, style = Stroke(stroke))
            if (active) {
                rotate(rotation) {
                    drawArc(
                        color = Accent,
                        startAngle = FocusLayout.RingStartAngle,
                        sweepAngle = FocusLayout.RingSweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(stroke, cap = StrokeCap.Round),
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(if (active) R.string.status_focusing else R.string.status_ready),
                style = MaterialTheme.typography.labelLarge,
                color = OnFocusMuted,
            )
            Spacer(Modifier.height(Dimens.spaceXs))
            Text(
                text = elapsedLabel,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Medium,
                color = OnFocus,
            )
        }
    }
}

@Composable
private fun StatsRow(noise: Int, movement: Int, noiseAvailable: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spaceLg),
        ) {
            StatPill(
                icon = Icons.Filled.GraphicEq,
                label = stringResource(R.string.focus_noise_label),
                value = noise,
                dimmed = !noiseAvailable,
            )
            StatPill(
                icon = Icons.Filled.Vibration,
                label = stringResource(R.string.focus_movement_label),
                value = movement,
                dimmed = false,
            )
        }
        if (!noiseAvailable) {
            Spacer(Modifier.height(Dimens.spaceSm))
            Text(
                text = stringResource(R.string.focus_noise_unavailable),
                style = MaterialTheme.typography.bodySmall,
                color = OnFocusMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RowScope.StatPill(icon: ImageVector, label: String, value: Int, dimmed: Boolean) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(Dimens.pillCorner))
            .background(StatSurface)
            .border(Dimens.hairline, SubtleBorder, RoundedCornerShape(Dimens.pillCorner))
            .padding(vertical = Dimens.pillPaddingVertical)
            .clearAndSetSemantics { contentDescription = "$label: $value" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (dimmed) IconDisabled else Accent,
        )
        Spacer(Modifier.height(Dimens.spaceSm))
        Text(value.toString(), style = MaterialTheme.typography.headlineMedium, color = OnFocus)
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnFocusMuted)
    }
}

@Composable
private fun StartStopButton(active: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(Dimens.actionHeight),
        shape = RoundedCornerShape(Dimens.actionCorner),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) ControlSurface else Accent,
            contentColor = if (active) OnFocus else OnAccent,
        ),
    ) {
        Icon(if (active) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = null)
        Text(
            text = stringResource(if (active) R.string.focus_stop else R.string.focus_start),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = Dimens.spaceSm),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FocusScreenIdlePreview() {
    FocusScreen(state = FocusState(), onAction = {})
}

@Preview(showBackground = true)
@Composable
private fun FocusScreenActivePreview() {
    FocusScreen(
        state = FocusState(sessionActive = true, elapsedMillis = 125_000, noiseCount = 3, movementCount = 1),
        onAction = {},
    )
}
