package com.assestment.kis.data.session.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val synced: Boolean,
)

@Entity(
    tableName = "distraction_events",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class DistractionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val type: String,
    val magnitude: Float,
    val timestampMillis: Long,
)

data class SessionWithEvents(
    @Embedded val session: SessionEntity,
    @Relation(parentColumn = "id", entityColumn = "sessionId")
    val events: List<DistractionEventEntity>,
)
