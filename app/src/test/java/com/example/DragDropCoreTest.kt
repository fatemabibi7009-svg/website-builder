package com.example

import com.example.data.model.BlockType
import com.example.data.model.WebBlockEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class DragDropCoreTest {

    @Test
    fun testReorderingElements() {
        val blocks = listOf(
            createDummyBlock("navbar", BlockType.NAVBAR, 0),
            createDummyBlock("hero", BlockType.HERO, 1),
            createDummyBlock("features", BlockType.FEATURES, 2),
            createDummyBlock("footer", BlockType.FOOTER, 3)
        )

        // Move item 1 (hero) to index 2 (after features)
        val mutable = blocks.toMutableList()
        val moved = mutable.removeAt(1)
        mutable.add(2, moved)

        assertEquals("navbar", mutable[0].id)
        assertEquals("features", mutable[1].id)
        assertEquals("hero", mutable[2].id)
        assertEquals("footer", mutable[3].id)

        // Move item 3 (footer) to index 0 (top)
        val movedTop = mutable.removeAt(3)
        mutable.add(0, movedTop)

        assertEquals("footer", mutable[0].id)
        assertEquals("navbar", mutable[1].id)
        assertEquals("features", mutable[2].id)
        assertEquals("hero", mutable[3].id)
    }

    private fun createDummyBlock(id: String, type: BlockType, order: Int): WebBlockEntity {
        return WebBlockEntity(
            id = id,
            websiteId = 1L,
            orderIndex = order,
            type = type,
            title = type.displayName,
            subtitle = "",
            content = "Sample content",
            buttonText = "",
            buttonUrl = "",
            imageUrl = "",
            backgroundColorHex = "",
            textColorHex = "",
            alignment = "center",
            extraDataJson = "",
            isVisible = true
        )
    }
}
