package com.example.data.model

data class CompiledSite(
    val html: String,
    val css: String,
    val js: String,
    val manifestJson: String,
    val readme: String,
    val additionalPages: Map<String, String> = emptyMap(),
    val fileCount: Int = 5 + additionalPages.size,
    val totalSizeBytes: Long = (html.length + css.length + js.length + manifestJson.length + readme.length + additionalPages.values.sumOf { it.length }).toLong()
)
