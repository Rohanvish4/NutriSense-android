package com.nutrisense.ai.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room TypeConverter for serializing and deserializing list types to JSON strings.
 */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return Json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
