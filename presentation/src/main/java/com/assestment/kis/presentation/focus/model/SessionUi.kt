package com.assestment.kis.presentation.focus.model

import com.assestment.kis.domain.session.DistractionType

data class SessionUi(
    val id: String,
    val startLabel: String,
    val durationLabel: String,
    val noiseCount: Int,
    val movementCount: Int,
    val totalCount: Int,
    val synced: Boolean,
    val events: List<DistractionEventUi>,
)

data class DistractionEventUi(
    val type: DistractionType,
    val timeLabel: String,
)
