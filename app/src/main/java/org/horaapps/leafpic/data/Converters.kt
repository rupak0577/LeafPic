package org.horaapps.leafpic.data

import androidx.room.TypeConverter
import org.horaapps.leafpic.data.filter.FilterMode
import org.horaapps.leafpic.data.sort.SortingMode
import org.horaapps.leafpic.data.sort.SortingOrder

class Converters {
    @TypeConverter
    fun fromFilterMode(value: FilterMode): Int {
        return when (value) {
            FilterMode.ALL -> 0
            FilterMode.IMAGES -> 1
            FilterMode.GIF -> 2
            FilterMode.VIDEO -> 3
            FilterMode.NO_VIDEO -> 4
        }
    }

    @TypeConverter
    fun toFilterMode(value: Int): FilterMode {
        return when (value) {
            0 -> FilterMode.ALL
            1 -> FilterMode.IMAGES
            2 -> FilterMode.GIF
            3 -> FilterMode.VIDEO
            4 -> FilterMode.NO_VIDEO
            else -> FilterMode.ALL
        }
    }

    @TypeConverter
    fun fromSortingOrder(value: SortingOrder): Int {
        return when (value) {
            SortingOrder.DESCENDING -> 0
            SortingOrder.ASCENDING -> 1
        }
    }

    @TypeConverter
    fun toSortingOrder(value: Int): SortingOrder {
        return when (value) {
            0 -> SortingOrder.DESCENDING
            1 -> SortingOrder.ASCENDING
            else -> SortingOrder.ASCENDING
        }
    }

    @TypeConverter
    fun fromSortingMode(value: SortingMode): Int {
        return when (value) {
            SortingMode.NAME -> 0
            SortingMode.DATE -> 1
            SortingMode.SIZE -> 2
            SortingMode.TYPE -> 3
            SortingMode.NUMERIC -> 4
        }
    }

    @TypeConverter
    fun toSortingMode(value: Int): SortingMode {
        return when (value) {
            0 -> SortingMode.NAME
            1 -> SortingMode.DATE
            2 -> SortingMode.SIZE
            3 -> SortingMode.TYPE
            4 -> SortingMode.NUMERIC
            else -> SortingMode.DATE
        }
    }
}