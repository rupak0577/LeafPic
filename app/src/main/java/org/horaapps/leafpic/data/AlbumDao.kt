package org.horaapps.leafpic.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
abstract class AlbumDao : BaseDao() {
    @Query("SELECT * FROM albums WHERE status = :status")
    abstract fun getAlbumsWithStatus(status: Int): LiveData<List<AlbumK>>

    @Query("SELECT EXISTS(SELECT 1 FROM albums WHERE path = :path LIMIT 1)")
    abstract fun ifExists(path: String): LiveData<AlbumK?>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun updateAlbum(album: AlbumK)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAlbum(album: AlbumK)
}