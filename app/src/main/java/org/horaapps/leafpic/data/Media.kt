package org.horaapps.leafpic.data

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
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
) {
    val uri: Uri
        get() {
            return Uri.fromFile(File(path))
        }
    var selected: Boolean = false

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
        return uri.encodedPath
    }

    fun getName(): String {
        return StringUtils.getPhotoNameByPath(path)
    }
}