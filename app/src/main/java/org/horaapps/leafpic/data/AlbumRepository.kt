package org.horaapps.leafpic.data

import androidx.lifecycle.LiveData
import javax.inject.Inject

class AlbumRepository @Inject constructor(private val albumDao: AlbumDao) {

    fun getFolders(status: Int): LiveData<List<Album>> {
        return albumDao.getAlbumsWithStatus(status)
    }

    fun getSettings(path: String): LiveData<Album?> {
        return albumDao.ifExists(path)
    }

    suspend fun updateAlbum(album: Album) {
        albumDao.updateAlbum(album)
    }

    suspend fun upsertAlbum(album: Album) {
        albumDao.insertOrUpdate(album)
    }

    suspend fun upsertAlbums(albums: List<Album>) {
        albumDao.insertOrUpdate(albums)
    }

    suspend fun insertAlbum(album: Album) {
        albumDao.insertAlbum(album)
    }
}