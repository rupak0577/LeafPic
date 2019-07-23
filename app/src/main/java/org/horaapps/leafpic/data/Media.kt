package org.horaapps.leafpic.data

import android.net.Uri
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import org.horaapps.leafpic.timeline.data.TimelineItem
import org.horaapps.leafpic.util.MimeTypeUtils
import org.horaapps.leafpic.util.StringUtils
import java.io.File

@Entity(tableName = "media",
        foreignKeys = [ForeignKey(entity = Album::class, parentColumns = arrayOf("id"),
                childColumns = arrayOf("album_id"), onDelete = CASCADE, onUpdate = CASCADE)],
        indices = [Index("path", unique = true), Index("album_id", unique = false)])
data class Media(
        @PrimaryKey
        @ColumnInfo(name = "path") val path: String,
        @ColumnInfo(name = "name") val name: String,
        @ColumnInfo(name = "album_id") val albumId: Long,
        @ColumnInfo(name = "size") val size: Long = -1,
        @ColumnInfo(name = "mime_type") val mimeType: String = MimeTypeUtils.UNKNOWN_MIME_TYPE,
        @ColumnInfo(name = "date_modified") val dateModified: Long = -1,
        @ColumnInfo(name = "orientation") val orientation: Int = 0
) : TimelineItem {
    @Ignore
    var isSelected: Boolean = false

    override fun getTimelineType(): Int {
        return TimelineItem.TYPE_MEDIA
    }

    fun getUri(): Uri? {
        return Uri.fromFile(File(path))
    }

    fun isGif(): Boolean {
        return mimeType.endsWith("gif")
    }

    fun isImage(): Boolean {
        return mimeType.startsWith("image")
    }

    fun isVideo(): Boolean {
        return mimeType.startsWith("video")
    }

    fun getDisplayPath(): String? {
        return getUri()?.encodedPath ?: path
    }

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

    fun getFile(): File? {
        val file = File(path)
        if (file.exists()) return file
        return null
    }
}