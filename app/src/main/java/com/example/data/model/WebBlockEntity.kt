package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "web_blocks",
    foreignKeys = [
        ForeignKey(
            entity = WebsiteEntity::class,
            parentColumns = ["id"],
            childColumns = ["websiteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("websiteId"), Index("orderIndex")]
)
data class WebBlockEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val websiteId: Long,
    val orderIndex: Int,
    val type: BlockType,
    val title: String = "",
    val subtitle: String = "",
    val content: String = "",
    val buttonText: String = "",
    val buttonUrl: String = "",
    val secondaryButtonText: String = "",
    val secondaryButtonUrl: String = "",
    val imageUrl: String = "",
    val backgroundColorHex: String = "", // empty means follow theme
    val textColorHex: String = "", // empty means follow theme
    val alignment: String = "center", // left, center, right
    val extraDataJson: String = "", // serialized items (features, faqs, links, etc.)
    val isVisible: Boolean = true
)
