package com.example.data.model

data class CompiledSite(
    val html: String,
    val css: String,
    val js: String,
    val manifestJson: String,
    val readme: String,
    val fileCount: Int = 5,
    val totalSizeBytes: Long = (html.length + css.length + js.length + manifestJson.length + readme.length).toLong()
)
