package org.horaapps.leafpic.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class MediaDao : BaseDao<Media>() {
    @Query("SELECT * FROM media WHERE album_path = :albumPath")
    abstract fun getMediaForAlbum(albumPath: String): LiveData<List<Media>>
}