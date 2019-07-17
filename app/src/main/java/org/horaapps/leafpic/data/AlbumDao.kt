package org.horaapps.leafpic.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
abstract class AlbumDao : BaseDao<Album>() {
    @Query("SELECT * FROM albums WHERE is_excluded = 0")
    abstract fun getIncludedAlbumsAsLiveData(): LiveData<List<Album>>

    @Query("SELECT * FROM albums WHERE is_excluded = 1")
    abstract suspend fun getExcludedAlbums(): List<Album>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun updateAlbum(album: Album)
}