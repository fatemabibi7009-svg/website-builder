package com.example.ui.editor

import com.example.data.model.WebBlockEntity
import com.example.data.model.WebsiteEntity

/**
 * Represents an immutable snapshot of the page layout, content, and styles at a specific point in time.
 */
data class EditorSnapshot(
    val website: WebsiteEntity,
    val blocks: List<WebBlockEntity>,
    val actionTitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Manages undo and redo history for website layout and style modifications.
 * Preserves deep copies of WebsiteEntity and WebBlockEntity list to protect against mutable references.
 */
class EditorHistoryManager(
    private val maxHistorySize: Int = 50
) {
    private val undoStack = ArrayDeque<EditorSnapshot>()
    private val redoStack = ArrayDeque<EditorSnapshot>()

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    val undoActionTitle: String?
        get() = undoStack.lastOrNull()?.actionTitle

    val redoActionTitle: String?
        get() = redoStack.lastOrNull()?.actionTitle

    val undoCount: Int
        get() = undoStack.size

    val redoCount: Int
        get() = redoStack.size

    /**
     * Returns a list of past actions in chronological order (oldest to most recent).
     */
    val undoHistoryList: List<EditorSnapshot>
        get() = undoStack.toList()

    val redoHistoryList: List<EditorSnapshot>
        get() = redoStack.toList()

    /**
     * Records the state *prior* to a change along with a descriptive title.
     * Clears the redo stack on any new user action.
     */
    @Synchronized
    fun recordAction(
        previousWebsite: WebsiteEntity,
        previousBlocks: List<WebBlockEntity>,
        actionTitle: String
    ) {
        if (undoStack.size >= maxHistorySize) {
            undoStack.removeFirst()
        }
        val snapshot = EditorSnapshot(
            website = previousWebsite.copy(),
            blocks = previousBlocks.map { it.copy() },
            actionTitle = actionTitle.ifBlank { "Edit changes" }
        )
        undoStack.addLast(snapshot)
        redoStack.clear()
    }

    /**
     * Undoes the last action.
     * Takes the current state to save onto the redo stack, pops the previous snapshot from the undo stack,
     * and returns the restored snapshot.
     */
    @Synchronized
    fun undo(
        currentWebsite: WebsiteEntity,
        currentBlocks: List<WebBlockEntity>
    ): EditorSnapshot? {
        if (undoStack.isEmpty()) return null
        val snapshotToRestore = undoStack.removeLast()
        redoStack.addLast(
            EditorSnapshot(
                website = currentWebsite.copy(),
                blocks = currentBlocks.map { it.copy() },
                actionTitle = snapshotToRestore.actionTitle
            )
        )
        return snapshotToRestore
    }

    /**
     * Redoes the previously undone action.
     * Takes the current state to save onto the undo stack, pops the snapshot from the redo stack,
     * and returns the restored snapshot.
     */
    @Synchronized
    fun redo(
        currentWebsite: WebsiteEntity,
        currentBlocks: List<WebBlockEntity>
    ): EditorSnapshot? {
        if (redoStack.isEmpty()) return null
        val snapshotToRestore = redoStack.removeLast()
        undoStack.addLast(
            EditorSnapshot(
                website = currentWebsite.copy(),
                blocks = currentBlocks.map { it.copy() },
                actionTitle = snapshotToRestore.actionTitle
            )
        )
        return snapshotToRestore
    }

    /**
     * Clears all history (e.g. upon new site creation or reset).
     */
    @Synchronized
    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
