package org.horaapps.leafpic.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class MediaDao : BaseDao<Media>() {
    @Query("SELECT * FROM media WHERE album_id = :albumId")
    abstract fun getMediaForAlbum(albumId: Long): LiveData<List<Media>>
}