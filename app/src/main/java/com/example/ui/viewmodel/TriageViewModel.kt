package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TriageRecordEntity
import com.example.data.model.CategoryItem
import com.example.data.model.ConsultationTranscriptItem
import com.example.data.model.TriageConclusion
import com.example.data.model.TriageNode
import com.example.data.model.TriageOption
import com.example.data.model.UrgencyLevel
import com.example.data.repository.TriageRepository
import com.example.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConsultationStep(
    val node: TriageNode,
    val selectedOption: TriageOption
)

data class ConsultationState(
    val isActive: Boolean = false,
    val category: CategoryItem? = null,
    val currentNode: TriageNode? = null,
    val stepHistory: List<ConsultationStep> = emptyList(),
    val isRedFlagAlertVisible: Boolean = false,
    val pendingRedFlagOption: TriageOption? = null,
    val conclusion: TriageConclusion? = null,
    val isSavedLocally: Boolean = false,
    val savedRecordId: Long? = null
)

private const val TAG = "TriageViewModel"

class TriageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TriageRepository(application.applicationContext)
    private val settingsRepository = SettingsRepository.get(application)

    val allRecords: StateFlow<List<TriageRecordEntity>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentRecords: StateFlow<List<TriageRecordEntity>> = repository.recentRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _consultationState = MutableStateFlow(ConsultationState())
    val consultationState: StateFlow<ConsultationState> = _consultationState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val cats = repository.getCategories()
                _categories.value = cats
            } catch (e: Exception) {
                Log.e(TAG, "Could not load the triage tree", e)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun startConsultation(category: CategoryItem) {
        viewModelScope.launch {
            val startNode = repository.getNodeById(category.startNodeId)
            _consultationState.value = ConsultationState(
                isActive = true,
                category = category,
                currentNode = startNode,
                stepHistory = emptyList(),
                isRedFlagAlertVisible = false,
                conclusion = null,
                isSavedLocally = false,
                savedRecordId = null
            )
        }
    }

    fun onOptionSelected(option: TriageOption) {
        val currentState = _consultationState.value
        val currentNode = currentState.currentNode ?: return

        // If red-flag triggered, immediately show emergency intercept modal
        if (option.isRedFlag) {
            _consultationState.value = currentState.copy(
                isRedFlagAlertVisible = true,
                pendingRedFlagOption = option
            )
            return
        }

        proceedToNextNode(currentNode, option)
    }

    fun acknowledgeRedFlagEmergency() {
        val currentState = _consultationState.value
        val currentNode = currentState.currentNode ?: return
        val redFlagOption = currentState.pendingRedFlagOption ?: return

        // Move to emergency conclusion node
        _consultationState.value = currentState.copy(
            isRedFlagAlertVisible = false,
            pendingRedFlagOption = null
        )
        proceedToNextNode(currentNode, redFlagOption)
    }

    fun dismissRedFlagModal() {
        _consultationState.value = _consultationState.value.copy(
            isRedFlagAlertVisible = false,
            pendingRedFlagOption = null
        )
    }

    private fun proceedToNextNode(currentNode: TriageNode, option: TriageOption) {
        viewModelScope.launch {
            val nextNode = repository.getNodeById(option.nextId)
            val updatedHistory = _consultationState.value.stepHistory + ConsultationStep(
                node = currentNode,
                selectedOption = option
            )

            if (nextNode != null && nextNode.isConclusion && nextNode.conclusion != null) {
                // Conclusion reached!
                _consultationState.value = _consultationState.value.copy(
                    currentNode = nextNode,
                    stepHistory = updatedHistory,
                    conclusion = nextNode.conclusion
                )
                // Automatically persist assessment to local Room DB
                autoSaveRecord(updatedHistory, nextNode.conclusion)
            } else {
                _consultationState.value = _consultationState.value.copy(
                    currentNode = nextNode,
                    stepHistory = updatedHistory
                )
            }
        }
    }

    fun backtrack() {
        val currentState = _consultationState.value
        if (currentState.stepHistory.isEmpty()) {
            resetConsultation()
            return
        }

        val history = currentState.stepHistory.toMutableList()
        val lastStep = history.removeAt(history.lastIndex)

        _consultationState.value = currentState.copy(
            currentNode = lastStep.node,
            stepHistory = history,
            conclusion = null,
            isRedFlagAlertVisible = false,
            pendingRedFlagOption = null
        )
    }

    private fun autoSaveRecord(history: List<ConsultationStep>, conclusion: TriageConclusion) {
        if (!settingsRepository.settings.value.saveHistory) return
        viewModelScope.launch {
            val category = _consultationState.value.category?.name ?: "General Intake"
            // The first node is the emergency screen, so its answer ("None of these") is not the complaint.
            val chiefComplaint = _consultationState.value.category?.name ?: "Unspecified symptom"

            val transcript = history.map { step ->
                ConsultationTranscriptItem(
                    question = step.node.question,
                    answer = step.selectedOption.text,
                    isRedFlag = step.selectedOption.isRedFlag
                )
            }

            val id = repository.saveTriageSession(
                category = category,
                chiefComplaint = chiefComplaint,
                conclusion = conclusion,
                transcript = transcript
            )

            _consultationState.value = _consultationState.value.copy(
                isSavedLocally = true,
                savedRecordId = id
            )
        }
    }

    fun resetConsultation() {
        _consultationState.value = ConsultationState()
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteRecord(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun buildSummaryText(
        categoryName: String,
        chiefComplaint: String,
        conclusion: TriageConclusion,
        stepHistory: List<ConsultationStep>
    ): String {
        val transcript = stepHistory.map { step ->
            ConsultationTranscriptItem(
                question = step.node.question,
                answer = step.selectedOption.text,
                isRedFlag = step.selectedOption.isRedFlag
            )
        }

        return repository.generateDoctorSummaryText(
            categoryName = categoryName,
            chiefComplaint = chiefComplaint,
            urgencyLevel = conclusion.urgencyLevel,
            likelyConditions = conclusion.likelyConditions,
            clinicalAdvice = conclusion.clinicalAdvice,
            transcript = transcript
        )
    }

    fun buildSummaryFromRecord(record: TriageRecordEntity): String {
        val transcript = repository.parseTranscript(record.transcriptJson)
        val urgency = try {
            UrgencyLevel.valueOf(record.urgencyLevel)
        } catch (_: Exception) {
            UrgencyLevel.ROUTINE_DOCTOR
        }

        val conditions = if (record.likelyConditions.isNotBlank()) {
            record.likelyConditions.split(", ")
        } else {
            emptyList()
        }

        return repository.generateDoctorSummaryText(
            categoryName = record.category,
            chiefComplaint = record.chiefComplaint,
            urgencyLevel = urgency,
            likelyConditions = conditions,
            clinicalAdvice = record.clinicalAdvice,
            transcript = transcript,
            timestamp = record.timestamp
        )
    }

    fun parseTranscript(json: String): List<ConsultationTranscriptItem> {
        return repository.parseTranscript(json)
    }
}
