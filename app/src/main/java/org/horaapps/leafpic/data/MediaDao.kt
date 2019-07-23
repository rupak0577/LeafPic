package org.horaapps.leafpic.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class MediaDao : BaseDao<Media>() {
    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY name ASC")
    abstract fun getMediaForAlbumSyncSortedByNameAsc(albumId: Long): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY name DESC")
    abstract fun getMediaForAlbumSyncSortedByNameDesc(albumId: Long): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY date_modified ASC")
    abstract fun getMediaForAlbumSyncSortedByDateAsc(albumId: Long): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY date_modified DESC")
    abstract fun getMediaForAlbumSyncSortedByDateDesc(albumId: Long): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY size ASC")
    abstract fun getMediaForAlbumSyncSortedBySizeAsc(albumId: Long): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY size DESC")
    abstract fun getMediaForAlbumSyncSortedBySizeDesc(albumId: Long): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY mime_type ASC")
    abstract fun getMediaForAlbumSyncSortedByTypeAsc(albumId: Long): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY mime_type DESC")
    abstract fun getMediaForAlbumSyncSortedByTypeDesc(albumId: Long): LiveData<List<Media>>
}