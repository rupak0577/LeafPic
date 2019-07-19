package org.horaapps.leafpic.data

import android.content.ContentResolver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.horaapps.leafpic.data.provider.MediaStoreHelper
import org.horaapps.leafpic.data.sort.SortingMode
import org.horaapps.leafpic.data.sort.SortingOrder
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class AlbumRepository @Inject constructor(private val albumDao: AlbumDao,
                                          private val mediaDao: MediaDao) {

    private val albumLoadingState = MutableLiveData<LoadingState>()
    private val mediaLoadingState = MutableLiveData<LoadingState>()

    private fun getLoadingState(albumState: Boolean): LiveData<LoadingState> {
        return when (albumState) {
            true -> albumLoadingState
            false -> mediaLoadingState
        }
    }

    fun getAlbums(): Listing<Album> {
        return Listing(list = albumDao.getIncludedAlbumsAsLiveData(),
                loadingState = getLoadingState(true))
    }

    suspend fun loadAlbums(contentResolver: ContentResolver, externalFilesDir: Array<File>, sortingMode: SortingMode,
                           sortingOrder: SortingOrder) {
        albumLoadingState.value = LoadingState.LOADING
        try {
            coroutineScope {
                val albumsFromDb = albumDao.getExcludedAlbums()
                val storeList = MediaStoreHelper.getAlbums(contentResolver, externalFilesDir, albumsFromDb,
                        sortingMode, sortingOrder)

                launch {
                    upsertAlbums(storeList)
                    albumLoadingState.value = LoadingState.LOADED
                }
            }
        } catch (exception: Exception) {
            Timber.e(exception)
            albumLoadingState.value = LoadingState.error(exception.message)
        }
    }

    fun getMedia(album: Album): Listing<Media> {
        return Listing(list = mediaDao.getMediaForAlbum(albumPath = album.path),
                loadingState = getLoadingState(false))
    }

    suspend fun loadMedia(contentResolver: ContentResolver, album: Album) {
        mediaLoadingState.value = LoadingState.LOADING
        try {
            coroutineScope {
                val storeList = MediaStoreHelper.getMedia(contentResolver, album)

                launch {
                    upsertMediaList(storeList)
                    mediaLoadingState.value = LoadingState.LOADED
                }
            }
        } catch (exception: Exception) {
            Timber.e(exception)
            mediaLoadingState.value = LoadingState.error(exception.message)
        }
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

    suspend fun upsertMediaList(mediaList: List<Media>) {
        mediaDao.insertOrUpdate(mediaList)
    }
}