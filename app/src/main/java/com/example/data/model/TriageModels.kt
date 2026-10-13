package com.example.data.model

import com.squareup.moshi.JsonClass

/**
 * Root container object for the bundled MedDialog triage decision tree.
 */
@JsonClass(generateAdapter = true)
data class TriageTreeData(
    val version: String,
    val metadata: TreeMetadata?,
    val categories: List<CategoryItem>,
    val nodes: Map<String, TriageNode>
)

@JsonClass(generateAdapter = true)
data class TreeMetadata(
    val source: String?,
    val description: String?
)

/**
 * A chief complaint the patient can start from (e.g. "Cough").
 *
 * @property group Body-system id used for card colour and filter chips (e.g. "RESPIRATORY").
 * @property groupName Human-readable body-system label for the filter chip.
 */
@JsonClass(generateAdapter = true)
data class CategoryItem(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val startNodeId: String,
    val group: String = "",
    val groupName: String = ""
)
