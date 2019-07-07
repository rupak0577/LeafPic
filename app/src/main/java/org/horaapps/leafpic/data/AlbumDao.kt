package org.horaapps.leafpic.data

import androidx.room.*

@Dao
abstract class AlbumDao : BaseDao() {
    @Query("SELECT * FROM albums WHERE status = :status")
    abstract suspend fun getAlbumsWithStatus(status: Int): List<AlbumK>

    @Query("SELECT EXISTS(SELECT 1 FROM albums WHERE path = :path LIMIT 1)")
    abstract suspend fun ifExists(path: String): AlbumK?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun updateAlbum(album: AlbumK)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAlbum(album: AlbumK)
}