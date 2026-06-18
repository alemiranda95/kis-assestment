package com.assestment.kis.presentation.ds

import androidx.compose.ui.unit.dp

/** Spacing and size values — composables reference these instead of raw dp numbers. */
object Dimens {
    val spaceXs = 4.dp
    val spaceSm = 8.dp
    val spaceMd = 12.dp
    val spaceLg = 16.dp
    val spaceXl = 24.dp
    val spaceXxl = 28.dp

    val hairline = 1.dp

    val timerDiameter = 260.dp
    val ringStroke = 6.dp

    val pillCorner = 20.dp
    val pillPaddingVertical = 18.dp

    val actionHeight = 60.dp
    val actionCorner = 30.dp

    val sheetListMaxHeight = 480.dp
    val detailListMaxHeight = 360.dp
}

/** Animation timings, in milliseconds. */
object Motion {
    const val AlertFlashMillis = 3000
    const val RingRotationMillis = 6000
}

/** Dimensionless layout/geometry values for the focus screen. */
object FocusLayout {
    const val TimerWeightActive = 0.55f
    const val TimerWeightIdle = 1f
    const val ActionWeight = 1.3f

    const val RingStartAngle = -90f
    const val RingSweepAngle = 120f
    const val RingRotationStart = 0f
    const val RingRotationEnd = 360f
}
