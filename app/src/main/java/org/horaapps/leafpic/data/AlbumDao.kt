package org.horaapps.leafpic.data

import androidx.room.*

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums WHERE status = :status")
    fun getAlbumsWithStatus(status: Int): List<AlbumK>

    @Query("SELECT * FROM albums WHERE path = :path LIMIT 1")
    fun ifExists(path: String): AlbumK?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateAlbum(album: AlbumK)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlbum(album: AlbumK)
}