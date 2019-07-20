package org.horaapps.leafpic.data

import androidx.room.*
import org.horaapps.leafpic.data.filter.FilterMode
import org.horaapps.leafpic.data.sort.SortingMode
import org.horaapps.leafpic.data.sort.SortingOrder

@Entity(tableName = "albums",
        indices = [Index("id", unique = true)])
data class Album(
        @PrimaryKey
        @ColumnInfo(name = "id") val id: Long,
        @ColumnInfo(name = "path") val path: String,
        @ColumnInfo(name = "album_name") val albumName: String,
        @ColumnInfo(name = "idx_parent") val idxParent: Long = -1,
        @ColumnInfo(name = "file_count") val fileCount: Int = -1,
        @Embedded val albumInfo: AlbumInfo = AlbumInfo()
) {
    @Ignore
    var isSelected: Boolean = false
    @Ignore
    var filterMode: FilterMode = FilterMode.ALL

    fun toggleSelected(): Boolean {
        isSelected = !isSelected
        return isSelected
    }

    fun setSelectedState(newSelectedState: Boolean): Boolean {
        if (newSelectedState == isSelected)
            return false
        this.isSelected = newSelectedState
        return true
    }
}

data class AlbumInfo(
        @ColumnInfo(name = "is_excluded") val isExcluded: Boolean = false,
        @ColumnInfo(name = "is_hidden") val isHidden: Boolean = false,
        @ColumnInfo(name = "is_pinned") val isPinned: Boolean = false,
        @ColumnInfo(name = "date_modified") val dateModified: Long = -1,
        @ColumnInfo(name = "cover_path") val coverPath: String? = null,
        @ColumnInfo(name = "sorting_mode") val sortingMode: SortingMode = SortingMode.DATE,
        @ColumnInfo(name = "sorting_order") val sortingOrder: SortingOrder = SortingOrder.ASCENDING
)