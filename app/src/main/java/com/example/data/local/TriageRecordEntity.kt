package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "triage_records")
data class TriageRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String,
    val chiefComplaint: String,
    val urgencyLevel: String,
    val likelyConditions: String,
    val clinicalAdvice: String,
    val transcriptJson: String
)
