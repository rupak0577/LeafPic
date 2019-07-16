package org.horaapps.leafpic.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
abstract class AlbumDao : BaseDao<Album>() {
    @Query("SELECT * FROM albums WHERE status = :status")
    abstract fun getAlbumsWithStatusAsLiveData(status: Int): LiveData<List<Album>>

    @Query("SELECT * FROM albums WHERE status = :status")
    abstract suspend fun getAlbumsWithStatus(status: Int): List<Album>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun updateAlbum(album: Album)
}