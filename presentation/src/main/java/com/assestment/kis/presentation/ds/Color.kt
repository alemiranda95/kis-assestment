package com.assestment.kis.presentation.ds

import androidx.compose.ui.graphics.Color

// Focus-screen ambiance — an intentional dark gradient, independent of the system light/dark setting.
val FocusGradientTop = Color(0xFF232859)
val FocusGradientBottom = Color(0xFF0E1024)
val AlertGradientTop = Color(0xFF6E1733)
val AlertGradientBottom = Color(0xFF1A0610)
val Accent = Color(0xFF9FB1FF)
val OnAccent = Color(0xFF10132B)
val OnFocus = Color(0xFFF4F5FF)
val OnFocusMuted = Color(0xFFB7BCE0)
val Warn = Color(0xFFFFB4AB)

// Translucent overlays drawn on top of the focus gradient.
val StatSurface = Color.White.copy(alpha = 0.07f)
val CardSurface = Color.White.copy(alpha = 0.06f)
val ControlSurface = Color.White.copy(alpha = 0.16f)
val FabSurface = Color.White.copy(alpha = 0.12f)
val SubtleBorder = Color.White.copy(alpha = 0.12f)
val TimerRingTrack = Accent.copy(alpha = 0.16f)
val IconDisabled = OnFocusMuted.copy(alpha = 0.5f)

// Material 3 scheme (history sheet, dialogs) — light
val Primary = Color(0xFF4A56C7)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFE0E1FF)
val OnPrimaryContainer = Color(0xFF050B49)
val Secondary = Color(0xFF5B5D72)
val Error = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val Background = Color(0xFFFBF8FF)
val OnBackground = Color(0xFF1A1B21)
val Surface = Color(0xFFFBF8FF)
val OnSurface = Color(0xFF1A1B21)

// Material 3 scheme — dark
val PrimaryDark = Color(0xFFBFC2FF)
val OnPrimaryDark = Color(0xFF13218C)
val PrimaryContainerDark = Color(0xFF313DA6)
val OnPrimaryContainerDark = Color(0xFFE0E1FF)
val SecondaryDark = Color(0xFFC4C5DD)
val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val BackgroundDark = Color(0xFF121318)
val OnBackgroundDark = Color(0xFFE3E1E9)
val SurfaceDark = Color(0xFF121318)
val OnSurfaceDark = Color(0xFFE3E1E9)
