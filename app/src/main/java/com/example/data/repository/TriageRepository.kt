package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.TriageDao
import com.example.data.local.TriageRecordEntity
import com.example.data.model.CategoryItem
import com.example.data.model.ConsultationTranscriptItem
import com.example.data.model.TriageConclusion
import com.example.data.model.TriageNode
import com.example.data.model.TriageTreeData
import com.example.data.model.UrgencyLevel
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TriageRepository(
    private val context: Context,
    private val triageDao: TriageDao = AppDatabase.getDatabase(context).triageDao()
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private var cachedTreeData: TriageTreeData? = null

    suspend fun getTreeData(): TriageTreeData = withContext(Dispatchers.IO) {
        cachedTreeData?.let { return@withContext it }

        val jsonString = context.assets.open("triage_tree.json").bufferedReader().use { it.readText() }
        val adapter = moshi.adapter(TriageTreeData::class.java)
        val treeData = adapter.fromJson(jsonString) ?: throw IllegalStateException("Failed to parse triage_tree.json")
        cachedTreeData = treeData
        treeData
    }

    suspend fun getCategories(): List<CategoryItem> {
        return getTreeData().categories
    }

    suspend fun getNodeById(nodeId: String): TriageNode? {
        return getTreeData().nodes[nodeId]
    }

    val allRecords: Flow<List<TriageRecordEntity>> = triageDao.getAllRecords()
    val recentRecords: Flow<List<TriageRecordEntity>> = triageDao.getRecentRecords()

    suspend fun saveTriageSession(
        category: String,
        chiefComplaint: String,
        conclusion: TriageConclusion,
        transcript: List<ConsultationTranscriptItem>
    ): Long = withContext(Dispatchers.IO) {
        val transcriptAdapter = moshi.adapter<List<ConsultationTranscriptItem>>(
            Types.newParameterizedType(List::class.java, ConsultationTranscriptItem::class.java)
        )
        val transcriptJson = transcriptAdapter.toJson(transcript)
        val conditionsText = conclusion.likelyConditions.joinToString(", ")

        val record = TriageRecordEntity(
            category = category,
            chiefComplaint = chiefComplaint,
            urgencyLevel = conclusion.urgencyLevel.name,
            likelyConditions = conditionsText,
            clinicalAdvice = conclusion.clinicalAdvice,
            transcriptJson = transcriptJson
        )
        triageDao.insertRecord(record)
    }

    suspend fun deleteRecord(id: Long) = withContext(Dispatchers.IO) {
        triageDao.deleteRecordById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        triageDao.clearAllRecords()
    }

    fun parseTranscript(json: String): List<ConsultationTranscriptItem> {
        return try {
            val transcriptAdapter = moshi.adapter<List<ConsultationTranscriptItem>>(
                Types.newParameterizedType(List::class.java, ConsultationTranscriptItem::class.java)
            )
            transcriptAdapter.fromJson(json) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun generateDoctorSummaryText(
        categoryName: String,
        chiefComplaint: String,
        urgencyLevel: UrgencyLevel,
        likelyConditions: List<String>,
        clinicalAdvice: String,
        transcript: List<ConsultationTranscriptItem>,
        timestamp: Long = System.currentTimeMillis()
    ): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val dateString = dateFormat.format(Date(timestamp))

        val builder = StringBuilder()
        builder.appendLine("========================================")
        builder.appendLine("🩺 MEDTRIAGE - PATIENT INTAKE SUMMARY")
        builder.appendLine("========================================")
        builder.appendLine("Generated: $dateString")
        builder.appendLine("Specialty System: $categoryName")
        builder.appendLine("Chief Complaint: $chiefComplaint")
        builder.appendLine("Triage Urgency: ${urgencyLevel.displayName}")
        builder.appendLine()
        builder.appendLine("--- CLINICAL CONSIDERATIONS (MedDialog Rules) ---")
        if (likelyConditions.isNotEmpty()) {
            likelyConditions.forEach { builder.appendLine("• $it") }
        } else {
            builder.appendLine("• Unspecified clinical evaluation required")
        }
        builder.appendLine()
        builder.appendLine("--- CLINICAL ADVICE & ASSESSMENT ---")
        builder.appendLine(clinicalAdvice)
        builder.appendLine()
        builder.appendLine("--- MULTI-TURN INTAKE TRANSCRIPT ---")
        transcript.forEachIndexed { index, item ->
            builder.appendLine("[Q${index + 1}] ${item.question}")
            builder.appendLine(" └─ Answer: ${item.answer}${if (item.isRedFlag) " [RED FLAG]" else ""}")
        }
        builder.appendLine()
        builder.appendLine("========================================")
        builder.appendLine("Note: Generated for physician consultation. MedTriage is an offline decision-support intake tool.")
        builder.appendLine("========================================")

        return builder.toString()
    }
}
