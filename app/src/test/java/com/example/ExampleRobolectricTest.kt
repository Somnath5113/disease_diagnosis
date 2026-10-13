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
        assertTrue(treeData.categories.isNotEmpty())
        assertEquals(5, treeData.categories.size)

        // Verify all 5 core clinical systems are mapped
        val categoryIds = treeData.categories.map { it.id }.toSet()
        assertTrue(categoryIds.contains("RESPIRATORY"))
        assertTrue(categoryIds.contains("GASTROINTESTINAL"))
        assertTrue(categoryIds.contains("NEUROLOGICAL"))
        assertTrue(categoryIds.contains("DERMATOLOGICAL"))
        assertTrue(categoryIds.contains("FEVER"))

        // Ensure every start node exists in the node catalog
        for (category in treeData.categories) {
            val startNode = treeData.nodes[category.startNodeId]
            assertNotNull("Start node ${category.startNodeId} for category ${category.id} must exist", startNode)
        }

        // Validate that every referenced nextId actually points to a real node
        for ((nodeId, node) in treeData.nodes) {
            for (option in node.options) {
                val nextNode = treeData.nodes[option.nextId]
                assertNotNull("Option in node $nodeId points to missing nextId: ${option.nextId}", nextNode)
            }
        }
    }
}
