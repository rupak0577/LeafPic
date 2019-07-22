package org.horaapps.leafpic.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
abstract class AlbumDao : BaseDao<Album>() {
    @Query("SELECT * FROM albums WHERE is_excluded = 0 ORDER BY album_name ASC")
    abstract fun getIncludedAlbumsSyncSortedByNameAsc(): LiveData<List<Album>>

    @Query("SELECT * FROM albums WHERE is_excluded = 0 ORDER BY date_modified ASC")
    abstract fun getIncludedAlbumsSyncSortedByDateAsc(): LiveData<List<Album>>

    @Query("SELECT * FROM albums WHERE is_excluded = 0 ORDER BY file_count ASC")
    abstract fun getIncludedAlbumsSyncSortedBySizeAsc(): LiveData<List<Album>>

    @Query("SELECT * FROM albums WHERE is_excluded = 0 ORDER BY album_name DESC")
    abstract fun getIncludedAlbumsSyncSortedByNameDesc(): LiveData<List<Album>>

    @Query("SELECT * FROM albums WHERE is_excluded = 0 ORDER BY date_modified DESC")
    abstract fun getIncludedAlbumsSyncSortedByDateDesc(): LiveData<List<Album>>

    @Query("SELECT * FROM albums WHERE is_excluded = 0 ORDER BY file_count DESC")
    abstract fun getIncludedAlbumsSyncSortedBySizeDesc(): LiveData<List<Album>>

    @Query("SELECT * FROM albums WHERE is_excluded = 1")
    abstract suspend fun getExcludedAlbums(): List<Album>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun updateAlbum(album: Album)
}