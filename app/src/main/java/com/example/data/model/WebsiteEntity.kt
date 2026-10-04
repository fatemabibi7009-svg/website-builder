package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "websites")
data class WebsiteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val slug: String,
    val description: String = "",
    val themePreset: String = "modern-dark",
    val fontFamily: String = "Inter, sans-serif",
    val customCss: String = "",
    val buttonStyle: String = "gradient",
    val buttonRadius: String = "pill",
    val customPrimaryColor: String = "",
    val customBackgroundColor: String = "",
    val animationStyle: String = "fade-up",
    val enableVisitorThemeToggle: Boolean = true,
    val formEndpoint: String = "",
    val ogImageUrl: String = "",
    val pagesJson: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
