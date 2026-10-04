package com.example

import com.example.data.model.BlockType
import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity
import com.example.ui.editor.EditorHistoryManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EditorHistoryManagerTest {

    private lateinit var historyManager: EditorHistoryManager
    private lateinit var sampleWebsite: WebsiteEntity
    private lateinit var sampleBlocks: List<WebBlockEntity>

    @Before
    fun setUp() {
        historyManager = EditorHistoryManager(maxHistorySize = 10)
        sampleWebsite = WebsiteEntity(
            id = 1,
            title = "Test Site",
            slug = "test-site",
            themePreset = "clean-light"
        )
        sampleBlocks = listOf(
            WebBlockEntity(
                id = "b1",
                websiteId = 1,
                orderIndex = 0,
                type = BlockType.HERO,
                title = "Hero Title"
            ),
            WebBlockEntity(
                id = "b2",
                websiteId = 1,
                orderIndex = 1,
                type = BlockType.FEATURES,
                title = "Features Title"
            )
        )
    }

    @Test
    fun initialState_cannotUndoOrRedo() {
        assertFalse(historyManager.canUndo)
        assertFalse(historyManager.canRedo)
        assertEquals(0, historyManager.undoCount)
        assertEquals(0, historyManager.redoCount)
        assertNull(historyManager.undoActionTitle)
        assertNull(historyManager.redoActionTitle)
    }

    @Test
    fun recordAction_enablesUndo_clearsRedo() {
        historyManager.recordAction(sampleWebsite, sampleBlocks, "Add Pricing Block")

        assertTrue(historyManager.canUndo)
        assertFalse(historyManager.canRedo)
        assertEquals(1, historyManager.undoCount)
        assertEquals("Add Pricing Block", historyManager.undoActionTitle)
    }

    @Test
    fun undo_restoresPreviousState_andEnablesRedo() {
        historyManager.recordAction(sampleWebsite, sampleBlocks, "Change Theme to Dark")

        val modifiedWebsite = sampleWebsite.copy(themePreset = "modern-dark")
        val modifiedBlocks = sampleBlocks + WebBlockEntity(
            id = "b3",
            websiteId = 1,
            orderIndex = 2,
            type = BlockType.PRICING,
            title = "Pricing"
        )

        val restored = historyManager.undo(modifiedWebsite, modifiedBlocks)

        assertNotNull(restored)
        assertEquals("clean-light", restored!!.website.themePreset)
        assertEquals(2, restored.blocks.size)
        assertEquals("Hero Title", restored.blocks[0].title)

        assertFalse(historyManager.canUndo)
        assertTrue(historyManager.canRedo)
        assertEquals("Change Theme to Dark", historyManager.redoActionTitle)
    }

    @Test
    fun redo_restoresNextState_andReenablesUndo() {
        historyManager.recordAction(sampleWebsite, sampleBlocks, "Add Pricing Block")

        val modifiedWebsite = sampleWebsite.copy(title = "Updated Title")
        val modifiedBlocks = sampleBlocks + WebBlockEntity(
            id = "b3",
            websiteId = 1,
            orderIndex = 2,
            type = BlockType.PRICING,
            title = "Pricing"
        )

        // Undo
        historyManager.undo(modifiedWebsite, modifiedBlocks)

        // Redo
        val redoRestored = historyManager.redo(sampleWebsite, sampleBlocks)

        assertNotNull(redoRestored)
        assertEquals("Updated Title", redoRestored!!.website.title)
        assertEquals(3, redoRestored.blocks.size)

        assertTrue(historyManager.canUndo)
        assertFalse(historyManager.canRedo)
    }

    @Test
    fun newAction_afterUndo_clearsRedoBranch() {
        historyManager.recordAction(sampleWebsite, sampleBlocks, "Action 1")
        val state1Website = sampleWebsite.copy(title = "State 1")

        val restored = historyManager.undo(state1Website, sampleBlocks)
        assertNotNull(restored)
        assertTrue(historyManager.canRedo)

        // New action branched
        historyManager.recordAction(sampleWebsite, sampleBlocks, "Action 2")
        assertFalse(historyManager.canRedo)
        assertEquals(1, historyManager.undoCount)
        assertEquals("Action 2", historyManager.undoActionTitle)
    }

    @Test
    fun maxHistorySize_isRespected() {
        for (i in 1..15) {
            historyManager.recordAction(
                sampleWebsite.copy(title = "Title $i"),
                sampleBlocks,
                "Action $i"
            )
        }

        assertEquals(10, historyManager.undoCount)
        assertEquals("Action 15", historyManager.undoActionTitle)
    }
}
