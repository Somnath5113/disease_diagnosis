package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.TriageRepository
import kotlinx.coroutines.runBlocking
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
}
