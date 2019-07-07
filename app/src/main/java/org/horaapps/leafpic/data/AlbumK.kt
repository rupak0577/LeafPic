package org.horaapps.leafpic.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "albums",
        indices = [Index("path", unique = true)])
data class AlbumK(
        @PrimaryKey
        @ColumnInfo(name = "path") val path: String,
        @ColumnInfo(name = "id") val id: Int,
        @ColumnInfo(name = "pinned") val pinned: Int,
        @ColumnInfo(name = "cover_path") val cover_path: String,
        @ColumnInfo(name = "status") val status: Int,
        @ColumnInfo(name = "sorting_mode") val sorting_mode: Int,
        @ColumnInfo(name = "sorting_order") val sorting_order: Int
)