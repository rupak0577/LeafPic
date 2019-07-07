package org.horaapps.leafpic.data

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.horaapps.leafpic.EXCLUDED
import java.util.*

class AlbumRepository private constructor(private val albumDao: AlbumDao) {

    /**
     *
     * @param status 1 for EXCLUDED, 2 for INCLUDED
     * @return
     */
    fun getFolders(status: Int): List<String> {
        return albumDao.getAlbumsWithStatus(status).map { album -> album.path }
    }

    fun getExcludedFolders(): List<String> {
        val list = ArrayList<String>()
        list.addAll(getFolders(EXCLUDED))
        return list
    }

    fun getSettings(path: String): AlbumSettings {
        return albumDao.ifExists(path)?.let { album ->
            AlbumSettings(album.cover_path, album.sorting_mode, album.sorting_order, album.pinned)
        } ?: AlbumSettings.getDefaults()
    }

    suspend fun updateAlbum(album: AlbumK) {
        withContext(IO) {
            albumDao.updateAlbum(album)
        }
    }

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: AlbumRepository? = null

        fun getInstance(albumDao: AlbumDao) =
                instance ?: synchronized(this) {
                    instance ?: AlbumRepository(albumDao).also { instance = it }
                }
    }
}