package org.horaapps.leafpic.data

import java.io.File
import java.util.*

const val ALL_MEDIA_ALBUM_ID: Long = 8000

fun getAllMediaAlbum(): Album {
    return Album("", ALL_MEDIA_ALBUM_ID, "All Media", AlbumInfo())
}

fun Album.getCover(): Media? {
    if (this.albumInfo.coverPath != null)
        return Media(this.albumInfo.coverPath)
    return this.lastMedia
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