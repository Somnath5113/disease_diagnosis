package com.example.data.model

import com.squareup.moshi.JsonClass

enum class UrgencyLevel(val displayName: String, val levelCode: String) {
    EMERGENCY("Critical Emergency", "CRITICAL"),
    URGENT_CARE("Urgent Medical Attention", "URGENT"),
    ROUTINE_DOCTOR("Routine Physician Visit", "ROUTINE"),
    SELF_CARE("Supportive Self-Care", "SELF_CARE")
}

/**
 * Represents a single decision-tree step in the MedDialog intake workflow.
 *
 * @property id Unique identifier for this node (e.g., "resp_chief_complaint", "gastro_emergency_appendicitis").
 * @property category Medical specialty domain (e.g., "RESPIRATORY", "GASTROINTESTINAL", "NEUROLOGICAL").
 * @property question Clinical question prompted to the patient.
 * @property explanation Medical rationale explaining why a physician asks this question.
 * @property isEmergencyCheck Whether this node represents a critical acute safety screening step.
 * @property options List of selectable response choices leading to downstream branches.
 * @property isConclusion True if this node concludes the branch and delivers final triage guidance.
 * @property conclusion Final clinical assessment, likely conditions, and care recommendations if [isConclusion] is true.
 */
@JsonClass(generateAdapter = true)
data class TriageNode(
    val id: String,
    val category: String,
    val question: String,
    val explanation: String? = null,
    val isEmergencyCheck: Boolean = false,
    val options: List<TriageOption> = emptyList(),
    val isConclusion: Boolean = false,
    val conclusion: TriageConclusion? = null
)

/**
 * An individual selectable response choice within a [TriageNode].
 *
 * @property text Primary response label displayed to the patient.
 * @property subtitle Detailed clinical descriptors, examples, or clarifying sensation descriptions.
 * @property nextId ID of the next [TriageNode] to navigate to when this option is selected.
 * @property isRedFlag Flag indicating that this response signifies an acute, potentially life-threatening emergency.
 */
@JsonClass(generateAdapter = true)
data class TriageOption(
    val text: String,
    val subtitle: String? = null,
    val nextId: String,
    val isRedFlag: Boolean = false
)

/**
 * Diagnostic triage outcome, including urgency level, matched conditions, and home-care guidance.
 */
@JsonClass(generateAdapter = true)
data class TriageConclusion(
    val urgencyLevel: UrgencyLevel,
    val likelyConditions: List<String> = emptyList(),
    val clinicalAdvice: String,
    val homeCareInstructions: List<String> = emptyList(),
    val whenToSeekImmediateHelp: String
)

/**
 * Represents an individual question-and-answer pair stored in the local audit log.
 */
data class ConsultationTranscriptItem(
    val question: String,
    val answer: String,
    val isRedFlag: Boolean = false
)
