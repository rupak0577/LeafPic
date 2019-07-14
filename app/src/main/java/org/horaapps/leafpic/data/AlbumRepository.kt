package org.horaapps.leafpic.data

import android.content.ContentResolver
import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.horaapps.leafpic.EXCLUDED
import org.horaapps.leafpic.data.provider.HiddenAlbumsHelper
import org.horaapps.leafpic.data.provider.MediaStoreHelper
import org.horaapps.leafpic.data.sort.SortingMode
import org.horaapps.leafpic.data.sort.SortingOrder
import javax.inject.Inject

class AlbumRepository @Inject constructor(private val albumDao: AlbumDao) {

    fun getFolders(status: Int): LiveData<List<Album>> {
        return albumDao.getAlbumsWithStatusAsLiveData(status)
    }

    suspend fun getFoldersWithStatus(status: Int): List<Album> {
        return albumDao.getAlbumsWithStatus(status)
    }

    suspend fun loadAlbums(contentResolver: ContentResolver, sortingMode: SortingMode,
                           sortingOrder: SortingOrder): List<Album> {
        val albumsFromDb = getFoldersWithStatus(EXCLUDED)
        albumsFromDb.isEmpty().let {
            val storeList = MediaStoreHelper.getAlbums(contentResolver, emptyList(), sortingMode, sortingOrder)
            coroutineScope {
                launch {
                    insertAlbums(storeList)
                }
            }
            storeList
        }
        return albumsFromDb
    }

    suspend fun loadHiddenAlbums(context: Context): List<Album> {
        val albumsFromDb = getFoldersWithStatus(EXCLUDED)
        return HiddenAlbumsHelper.getHiddenAlbums(context, albumsFromDb)
    }

    suspend fun loadMedia(contentResolver: ContentResolver, album: Album): List<Media> {
        return MediaStoreHelper.getMedia(contentResolver, album)
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

    suspend fun insertAlbums(albums: List<Album>) {
        albumDao.insertAlbums(albums)
    }
}