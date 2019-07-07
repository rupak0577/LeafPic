package org.horaapps.leafpic.data

import org.horaapps.leafpic.EXCLUDED
import java.util.*
import javax.inject.Inject

class AlbumRepository @Inject constructor(private val albumDao: AlbumDao) {

    suspend fun getFolders(status: Int): List<String> {
        return albumDao.getAlbumsWithStatus(status).map { album -> album.path }
    }

    suspend fun getExcludedFolders(): List<String> {
        val list = ArrayList<String>()
        list.addAll(getFolders(EXCLUDED))
        return list
    }

    suspend fun getSettings(path: String): AlbumSettings {
        return albumDao.ifExists(path)?.let { album ->
            AlbumSettings(album.cover_path, album.sorting_mode, album.sorting_order, album.pinned)
        } ?: AlbumSettings.getDefaults()
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