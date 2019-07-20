package org.horaapps.leafpic.data

import org.horaapps.leafpic.util.StringUtils
import java.io.File
import java.util.*

const val ALL_MEDIA_ALBUM_ID: Long = 8000

fun getAllMediaAlbum(): Album {
    return Album(123, "", "All Media", ALL_MEDIA_ALBUM_ID)
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