package org.horaapps.leafpic.ui.common

import androidx.lifecycle.ViewModel
import org.horaapps.leafpic.data.Album
import org.horaapps.leafpic.data.Media

class SharedVM : ViewModel() {
    var album: Album? = null
}