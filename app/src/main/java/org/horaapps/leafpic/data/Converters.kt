package org.horaapps.leafpic.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromMediaType(value: MediaType): Int {
        return when (value) {
            MediaType.IMAGE -> 0
            MediaType.VIDEO -> 1
            MediaType.GIF -> 2
            MediaType.UNKNOWN -> 3
        }
    }

    @TypeConverter
    fun toMediaType(value: Int): MediaType {
        return when (value) {
            0 -> MediaType.IMAGE
            1 -> MediaType.VIDEO
            2 -> MediaType.GIF
            else -> MediaType.UNKNOWN
        }
    }
}