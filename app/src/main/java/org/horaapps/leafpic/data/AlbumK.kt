package org.horaapps.leafpic.data

import androidx.room.*
import org.horaapps.leafpic.INCLUDED
import org.horaapps.leafpic.data.filter.FilterMode
import org.horaapps.leafpic.data.sort.SortingMode
import org.horaapps.leafpic.data.sort.SortingOrder

@Entity(tableName = "albums",
        indices = [Index("path", unique = true)])
data class AlbumK(
        @PrimaryKey
        @ColumnInfo(name = "path") val path: String,
        @ColumnInfo(name = "id") val id: Long = -1,
        @ColumnInfo(name = "album_name") val albumName: String,
        @Embedded var albumInfo: AlbumInfo
)

data class AlbumInfo(
        @ColumnInfo(name = "status") val status: Int = INCLUDED,
        @ColumnInfo(name = "pinned") val pinned: Boolean = false,
        @ColumnInfo(name = "file_count") val fileCount: Int = -1,
        @ColumnInfo(name = "date_modified") val dateModified: Long = -1,
        @ColumnInfo(name = "cover_path") val coverPath: String? = null,
        @ColumnInfo(name = "sorting_mode") val sortingMode: SortingMode = SortingMode.DATE,
        @ColumnInfo(name = "sorting_order") val sortingOrder: SortingOrder = SortingOrder.ASCENDING,
        @ColumnInfo(name = "filter_mode") val filterMode: FilterMode = FilterMode.ALL
)