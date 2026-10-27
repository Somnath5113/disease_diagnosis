package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.TriageRepository
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MedTriage", appName)
    }

    @Test
    fun `verify triage tree json loads with categories and valid pathways`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = TriageRepository(context)
        val treeData = repository.getTreeData()

        assertNotNull(treeData)
        // Categories are the chief complaints that were frequent enough in MedDialog.
        assertTrue(treeData.categories.size >= 15)
        val categoryIds = treeData.categories.map { it.id }.toSet()
        assertEquals("Category ids must be unique", treeData.categories.size, categoryIds.size)
        // Safety-critical complaints must always be covered.
        for (id in listOf("CHESTPAIN", "BREATHLESS", "HEADACHE", "ABDPAIN", "COUGH", "FEVER")) {
            assertTrue("Missing category $id", categoryIds.contains(id))
        }
        assertTrue(treeData.categories.all { it.group.isNotBlank() && it.groupName.isNotBlank() })

        // Ensure every start node exists, and is an emergency screen with a red-flag option
        // plus a way to continue.
        for (category in treeData.categories) {
            val startNode = treeData.nodes[category.startNodeId]
            assertNotNull("Start node ${category.startNodeId} for category ${category.id} must exist", startNode)
            assertTrue("${category.id} start node must be an emergency screen", startNode!!.isEmergencyCheck)
            assertTrue("${category.id} must have a red-flag option", startNode.options.any { it.isRedFlag })
            assertTrue("${category.id} must let the patient continue", startNode.options.any { !it.isRedFlag })
        }

        // Every red-flag option must lead straight to an emergency/urgent conclusion.
        for ((nodeId, node) in treeData.nodes) {
            for (option in node.options.filter { it.isRedFlag }) {
                val target = treeData.nodes[option.nextId]
                assertNotNull("Red flag in $nodeId has no target", target)
                assertTrue("Red flag target ${option.nextId} must be a conclusion", target!!.isConclusion)
            }
            // Condition names are joined with ", " in the history record, so they must not contain it.
            node.conclusion?.likelyConditions?.forEach {
                assertTrue("Condition '$it' in $nodeId contains ', '", !it.contains(", "))
            }
        }

        // Longest answer path in screens (emergency screen + questions). Also proves the graph has no cycles.
        val memo = HashMap<String, Int>()
        fun screens(id: String, visiting: MutableSet<String>): Int {
            memo[id]?.let { return it }
            assertTrue("Cycle through $id", visiting.add(id))
            val node = treeData.nodes.getValue(id)
            val depth = if (node.isConclusion) 0 else 1 + (node.options.maxOfOrNull { screens(it.nextId, visiting) } ?: 0)
            visiting.remove(id)
            memo[id] = depth
            return depth
        }
        val longest = treeData.categories.maxOf { screens(it.startNodeId, mutableSetOf()) }
        assertTrue("Deep diagnosis should reach at least 8 screens, was $longest", longest >= 8)
        assertTrue("No path may exceed 10 screens, was $longest", longest <= 10)

        // Validate that every referenced nextId actually points to a real node
        for ((nodeId, node) in treeData.nodes) {
            for (option in node.options) {
                val nextNode = treeData.nodes[option.nextId]
                assertNotNull("Option in node $nodeId points to missing nextId: ${option.nextId}", nextNode)
            }
        }
    }

    @Test
    fun `translated trees have exactly the same structure and urgency as the English tree`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        fun load(name: String) = JSONObject(context.assets.open(name).bufferedReader().use { it.readText() })

        val english = load("triage_tree.json")
        val translated = context.assets.list("").orEmpty()
            .filter { it.startsWith("triage_tree_") && it.endsWith(".json") }
        assertTrue("Expected at least one translated tree", translated.isNotEmpty())

        val enNodes = english.getJSONObject("nodes")
        for (asset in translated) {
            val tree = load(asset)
            val nodes = tree.getJSONObject("nodes")
            assertEquals("$asset: node ids", enNodes.keys().asSequence().toSet(), nodes.keys().asSequence().toSet())
            assertEquals("$asset: category count", english.getJSONArray("categories").length(), tree.getJSONArray("categories").length())

            for (id in enNodes.keys()) {
                val a = enNodes.getJSONObject(id)
                val b = nodes.getJSONObject(id)
                val aOpts = a.optJSONArray("options")
                val bOpts = b.optJSONArray("options")
                assertEquals("$asset/$id: option count", aOpts?.length() ?: 0, bOpts?.length() ?: 0)
                for (i in 0 until (aOpts?.length() ?: 0)) {
                    val ao = aOpts!!.getJSONObject(i)
                    val bo = bOpts!!.getJSONObject(i)
                    assertEquals("$asset/$id: nextId", ao.getString("nextId"), bo.getString("nextId"))
                    assertEquals("$asset/$id: red flag", ao.optBoolean("isRedFlag"), bo.optBoolean("isRedFlag"))
                    assertTrue("$asset/$id: option text empty", bo.getString("text").isNotBlank())
                }
                assertEquals("$asset/$id: emergency screen", a.optBoolean("isEmergencyCheck"), b.optBoolean("isEmergencyCheck"))
                val ac = a.optJSONObject("conclusion")
                val bc = b.optJSONObject("conclusion")
                assertEquals("$asset/$id: has conclusion", ac != null, bc != null)
                if (ac != null && bc != null) {
                    assertEquals("$asset/$id: urgency", ac.getString("urgencyLevel"), bc.getString("urgencyLevel"))
                    for (field in listOf("likelyConditions", "homeCareInstructions", "indianHomeRemedies")) {
                        assertEquals("$asset/$id: $field size", ac.optJSONArray(field)?.length() ?: 0, bc.optJSONArray(field)?.length() ?: 0)
                    }
                    for (i in 0 until bc.getJSONArray("likelyConditions").length()) {
                        val condition = bc.getJSONArray("likelyConditions").getString(i)
                        assertTrue("$asset/$id: condition '$condition' contains ', '", !condition.contains(", "))
                    }
                    assertTrue("$asset/$id: advice empty", bc.getString("clinicalAdvice").isNotBlank())
                    assertTrue("$asset/$id: seek text empty", bc.getString("whenToSeekImmediateHelp").isNotBlank())
                }
            }
        }
    }
}
