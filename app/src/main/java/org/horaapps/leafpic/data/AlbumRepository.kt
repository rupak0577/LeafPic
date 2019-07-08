package org.horaapps.leafpic.data

import androidx.lifecycle.LiveData
import javax.inject.Inject

class AlbumRepository @Inject constructor(private val albumDao: AlbumDao) {

    fun getFolders(status: Int): LiveData<List<AlbumK>> {
        return albumDao.getAlbumsWithStatus(status)
    }

    fun getSettings(path: String): LiveData<AlbumK?> {
        return albumDao.ifExists(path)
    }

    suspend fun updateAlbum(album: AlbumK) {
        albumDao.updateAlbum(album)
    }

    suspend fun upsertAlbum(album: AlbumK) {
        albumDao.insertOrUpdate(album)
    }

    suspend fun upsertAlbums(albums: List<AlbumK>) {
        albumDao.insertOrUpdate(albums)
    }

    suspend fun insertAlbum(album: AlbumK) {
        albumDao.insertAlbum(album)
    }
}