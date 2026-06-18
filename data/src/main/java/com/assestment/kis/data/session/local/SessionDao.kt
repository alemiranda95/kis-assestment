package com.assestment.kis.data.session.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: SessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvents(events: List<DistractionEventEntity>)

    @Transaction
    @Query("SELECT * FROM sessions ORDER BY startTimeMillis DESC")
    suspend fun getSessionsWithEvents(): List<SessionWithEvents>

    @Transaction
    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionWithEvents(id: String): SessionWithEvents?
}
