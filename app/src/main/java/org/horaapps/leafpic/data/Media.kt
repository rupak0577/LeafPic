package org.horaapps.leafpic.data

import android.net.Uri
import android.os.Bundle
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import org.horaapps.leafpic.timeline.data.TimelineItem
import org.horaapps.leafpic.util.MimeTypeUtils
import java.io.File

const val PK_MEDIA_NAME = "MEDIA_NAME"
const val PK_MEDIA_DATE_MODIFIED = "MEDIA_DATE_MODIFIED"
const val PK_MEDIA_ORIENTATION = "MEDIA_ORIENTATION"

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
        @ColumnInfo(name = "media_type") val mediaType: MediaType,
        @ColumnInfo(name = "mime_type") val mimeType: String = MimeTypeUtils.UNKNOWN_MIME_TYPE,
        @ColumnInfo(name = "date_modified") val dateModified: Long = -1,
        @ColumnInfo(name = "orientation") val orientation: Int = 0
) : TimelineItem, DiffableEntity<Media> {

    override fun isSameAs(newItem: Media): Boolean {
        return this.path == newItem.path
    }

    override fun hasSameContentAs(newItem: Media): Boolean {
        return this == newItem
    }

    override fun diffWithAndGetChangePayload(newItem: Media): Any? {
        val diffBundle = Bundle()

        if (this.size != newItem.size) {
            diffBundle.putLong(PK_MEDIA_NAME, newItem.size)
        }
        if (this.dateModified != newItem.dateModified) {
            diffBundle.putLong(PK_MEDIA_DATE_MODIFIED, newItem.dateModified)
        }
        if (this.orientation != newItem.orientation) {
            diffBundle.putInt(PK_MEDIA_ORIENTATION, newItem.orientation)
        }

        return if (diffBundle.size() == 0) null else diffBundle
    }

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

    fun getFile(): File? {
        val file = File(path)
        if (file.exists()) return file
        return null
    }
}