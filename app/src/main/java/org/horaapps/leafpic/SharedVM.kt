package org.horaapps.leafpic

import androidx.lifecycle.ViewModel
import org.horaapps.leafpic.data.Album
import org.horaapps.leafpic.data.Media

class SharedVM : ViewModel() {
    var media: Media? = null
    var album: Album? = null
    var mediaList: ArrayList<Media>? = null
}