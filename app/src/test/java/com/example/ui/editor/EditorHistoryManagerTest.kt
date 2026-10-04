package com.example.ui.editor

import com.example.data.model.BlockType
import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EditorHistoryManagerTest {

    private lateinit var historyManager: EditorHistoryManager
    private val initialWebsite = WebsiteEntity(
        id = 1,
        title = "Test Site",
        slug = "test-site",
        description = "A test website",
        themePreset = "modern",
        fontFamily = "inter"
    )

    private val initialBlocks = listOf(
        WebBlockEntity(
            id = "block-1",
            websiteId = 1,
            orderIndex = 0,
            type = BlockType.HERO,
            title = "Welcome"
        ),
        WebBlockEntity(
            id = "block-2",
            websiteId = 1,
            orderIndex = 1,
            type = BlockType.FEATURES,
            title = "Key Features"
        )
    )

    @Before
    fun setUp() {
        historyManager = EditorHistoryManager(maxHistorySize = 10)
    }

    @Test
    fun testInitialState() {
        assertFalse(historyManager.canUndo)
        assertFalse(historyManager.canRedo)
        assertNull(historyManager.undoActionTitle)
        assertNull(historyManager.redoActionTitle)
        assertTrue(historyManager.undoHistoryList.isEmpty())
    }

    @Test
    fun testRecordActionAndUndo() {
        // User changes block title
        val modifiedBlocks = initialBlocks.map {
            if (it.id == "block-1") it.copy(title = "New Hero Title") else it
        }

        // Record initial state prior to mutation
        historyManager.recordAction(initialWebsite, initialBlocks, "Edit Hero Title")

        assertTrue(historyManager.canUndo)
        assertFalse(historyManager.canRedo)
        assertEquals("Edit Hero Title", historyManager.undoActionTitle)

        // Perform undo
        val restored = historyManager.undo(initialWebsite, modifiedBlocks)
        assertNotNull(restored)
        assertEquals("Welcome", restored!!.blocks.first().title)
        assertFalse(historyManager.canUndo)
        assertTrue(historyManager.canRedo)
        assertEquals("Edit Hero Title", historyManager.redoActionTitle)

        // Perform redo
        val redone = historyManager.redo(restored.website, restored.blocks)
        assertNotNull(redone)
        assertEquals("New Hero Title", redone!!.blocks.first().title)
        assertTrue(historyManager.canUndo)
        assertFalse(historyManager.canRedo)
    }

    @Test
    fun testNewActionClearsRedoStack() {
        historyManager.recordAction(initialWebsite, initialBlocks, "Action 1")
        val state2Blocks = initialBlocks.map { it.copy(title = "State 2") }

        historyManager.undo(initialWebsite, state2Blocks)
        assertTrue(historyManager.canRedo)

        // New action recorded
        historyManager.recordAction(initialWebsite, initialBlocks, "Action 2")
        assertFalse(historyManager.canRedo)
    }

    @Test
    fun testClearHistory() {
        historyManager.recordAction(initialWebsite, initialBlocks, "Action 1")
        historyManager.clear()
        assertFalse(historyManager.canUndo)
        assertFalse(historyManager.canRedo)
    }
}
