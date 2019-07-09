package org.horaapps.leafpic.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
abstract class AlbumDao : BaseDao() {
    @Query("SELECT * FROM albums WHERE status = :status")
    abstract fun getAlbumsWithStatus(status: Int): LiveData<List<Album>>

    @Query("SELECT EXISTS(SELECT 1 FROM albums WHERE path = :path LIMIT 1)")
    abstract fun ifExists(path: String): LiveData<Album?>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun updateAlbum(album: Album)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAlbum(album: Album)
}