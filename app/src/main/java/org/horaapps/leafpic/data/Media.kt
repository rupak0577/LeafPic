package org.horaapps.leafpic.data

import android.net.Uri
import androidx.room.*
import org.horaapps.leafpic.timeline.data.TimelineItem
import org.horaapps.leafpic.util.MimeTypeUtils
import org.horaapps.leafpic.util.StringUtils
import java.io.File

@Entity(tableName = "media",
        indices = [Index("path", unique = true)])
data class Media(
        @PrimaryKey
        @ColumnInfo val path: String,
        @ColumnInfo val size: Long = -1,
        @ColumnInfo val mimeType: String = MimeTypeUtils.UNKNOWN_MIME_TYPE,
        @ColumnInfo val dateModified: Long = -1,
        @ColumnInfo val orientation: Int = 0
) : TimelineItem {
    @Ignore var isSelected: Boolean = false

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

    fun getName(): String {
        return StringUtils.getPhotoNameByPath(path)
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