package org.horaapps.leafpic.data

import android.os.Bundle
import androidx.room.*

const val PK_ALBUM_NAME = "ALBUM_NAME"
const val PK_ALBUM_FILE_COUNT = "FILE_COUNT"
const val PK_ALBUM_IS_PINNED = "IS_PINNED"
const val PK_ALBUM_IS_EXCLUDED = "IS_EXCLUDED"
const val PK_ALBUM_IS_HIDDEN = "IS_HIDDEN"
const val PK_ALBUM_DATE_MODIFIED = "DATE_MODIFIED"

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
) : DiffableEntity<Album> {

    override fun isSameAs(newItem: Album): Boolean {
        return this.id == newItem.id
    }

    override fun hasSameContentAs(newItem: Album): Boolean {
        return this == newItem
    }

    override fun diffWithAndGetChangePayload(newItem: Album): Any? {
        val diffBundle = Bundle()

        if (this.albumName != newItem.albumName) {
            diffBundle.putString(PK_ALBUM_NAME, newItem.albumName)
        }
        if (this.fileCount != newItem.fileCount) {
            diffBundle.putInt(PK_ALBUM_FILE_COUNT, newItem.fileCount)
        }
        if (this.albumInfo.isPinned != newItem.albumInfo.isPinned) {
            diffBundle.putBoolean(PK_ALBUM_IS_PINNED, newItem.albumInfo.isPinned)
        }
        if (this.albumInfo.isExcluded != newItem.albumInfo.isExcluded) {
            diffBundle.putBoolean(PK_ALBUM_IS_EXCLUDED, newItem.albumInfo.isExcluded)
        }
        if (this.albumInfo.isHidden != newItem.albumInfo.isHidden) {
            diffBundle.putBoolean(PK_ALBUM_IS_HIDDEN, newItem.albumInfo.isHidden)
        }
        if (this.albumInfo.dateModified != newItem.albumInfo.dateModified) {
            diffBundle.putLong(PK_ALBUM_DATE_MODIFIED, newItem.albumInfo.dateModified)
        }

        return if (diffBundle.size() == 0) null else diffBundle
    }
}

data class AlbumInfo(
        @ColumnInfo(name = "is_excluded") val isExcluded: Boolean = false,
        @ColumnInfo(name = "is_hidden") val isHidden: Boolean = false,
        @ColumnInfo(name = "is_pinned") val isPinned: Boolean = false,
        @ColumnInfo(name = "date_modified") val dateModified: Long = -1,
        @ColumnInfo(name = "cover_path") val coverPath: String? = null
)