package org.horaapps.leafpic.data

import android.provider.MediaStore
import java.io.File
import java.util.*

const val ALL_MEDIA_ALBUM_ID: Long = 8000

fun Album.isSelected(): Boolean {
    return selected
}

fun Album.getProjection(): Array<String> {
    return arrayOf(MediaStore.Files.FileColumns.PARENT, MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            "count(*)", MediaStore.Images.Media.DATA, "max(" + MediaStore.Images.Media.DATE_MODIFIED + ")")
}

fun getAllMediaAlbum(): Album {
    return Album("", ALL_MEDIA_ALBUM_ID, "All Media", AlbumInfo())
}

fun Album.getCover(): Media? {
    if (this.albumInfo.coverPath != null)
        return Media(this.albumInfo.coverPath)
    return if (this.lastMedia != null) this.lastMedia else Media()
    // TODO: 11/20/16 how should i handle this?
}

fun Album.getParentsFolders(): ArrayList<String> {
    val result = ArrayList<String>()

    var f: File? = File(this.path)
    while (f != null && f.canRead()) {
        result.add(f.path)
        f = f.parentFile
    }
    return result
}