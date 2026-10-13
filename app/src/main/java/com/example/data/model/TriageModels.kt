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

@JsonClass(generateAdapter = true)
data class CategoryItem(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val startNodeId: String
)
