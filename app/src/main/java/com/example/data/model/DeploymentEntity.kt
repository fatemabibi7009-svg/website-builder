package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "deployments",
    foreignKeys = [
        ForeignKey(
            entity = WebsiteEntity::class,
            parentColumns = ["id"],
            childColumns = ["websiteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("websiteId")]
)
data class DeploymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val websiteId: Long,
    val siteId: String,
    val siteName: String,
    val deployUrl: String,
    val adminUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "LIVE"
)
