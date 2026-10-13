package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TriageDao {
    @Query("SELECT * FROM triage_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<TriageRecordEntity>>

    @Query("SELECT * FROM triage_records ORDER BY timestamp DESC LIMIT 3")
    fun getRecentRecords(): Flow<List<TriageRecordEntity>>

    @Query("SELECT * FROM triage_records WHERE id = :id")
    suspend fun getRecordById(id: Long): TriageRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TriageRecordEntity): Long

    @Query("DELETE FROM triage_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM triage_records")
    suspend fun clearAllRecords()
}
