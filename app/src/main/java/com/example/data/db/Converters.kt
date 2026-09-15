package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.BlockType

class Converters {
    @TypeConverter
    fun fromBlockType(value: BlockType): String = value.name

    @TypeConverter
    fun toBlockType(value: String): BlockType = try {
        BlockType.valueOf(value)
    } catch (e: Exception) {
        BlockType.CUSTOM_HTML
    }
}
