package org.horaapps.leafpic.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class MediaDao : BaseDao<Media>() {
    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY name ASC")
    abstract fun getAllMediaForAlbumSyncSortedByNameAsc(albumId: Long): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY name DESC")
    abstract fun getAllMediaForAlbumSyncSortedByNameDesc(albumId: Long): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY date_modified ASC")
    abstract fun getAllMediaForAlbumSyncSortedByDateAsc(albumId: Long): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY date_modified DESC")
    abstract fun getAllMediaForAlbumSyncSortedByDateDesc(albumId: Long): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY size ASC")
    abstract fun getAllMediaForAlbumSyncSortedBySizeAsc(albumId: Long): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY size DESC")
    abstract fun getAllMediaForAlbumSyncSortedBySizeDesc(albumId: Long): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY mime_type ASC")
    abstract fun getAllMediaForAlbumSyncSortedByTypeAsc(albumId: Long): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId ORDER BY mime_type DESC")
    abstract fun getAllMediaForAlbumSyncSortedByTypeDesc(albumId: Long): LiveData<List<Media>>

    // Queries selecting by media type

    @Query("SELECT * FROM media WHERE album_id = :albumId AND media_type = :mediaType ORDER BY name ASC")
    abstract fun getMediaForAlbumSyncSortedByNameAsc(albumId: Long, mediaType: MediaType): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId AND media_type = :mediaType ORDER BY name DESC")
    abstract fun getMediaForAlbumSyncSortedByNameDesc(albumId: Long, mediaType: MediaType): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId AND media_type = :mediaType ORDER BY date_modified ASC")
    abstract fun getMediaForAlbumSyncSortedByDateAsc(albumId: Long, mediaType: MediaType): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId AND media_type = :mediaType ORDER BY date_modified DESC")
    abstract fun getMediaForAlbumSyncSortedByDateDesc(albumId: Long, mediaType: MediaType): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId AND media_type = :mediaType ORDER BY size ASC")
    abstract fun getMediaForAlbumSyncSortedBySizeAsc(albumId: Long, mediaType: MediaType): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId AND media_type = :mediaType ORDER BY size DESC")
    abstract fun getMediaForAlbumSyncSortedBySizeDesc(albumId: Long, mediaType: MediaType): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId AND media_type = :mediaType ORDER BY mime_type ASC")
    abstract fun getMediaForAlbumSyncSortedByTypeAsc(albumId: Long, mediaType: MediaType): LiveData<List<Media>>

    @Query("SELECT * FROM media WHERE album_id = :albumId AND media_type = :mediaType ORDER BY mime_type DESC")
    abstract fun getMediaForAlbumSyncSortedByTypeDesc(albumId: Long, mediaType: MediaType): LiveData<List<Media>>
}