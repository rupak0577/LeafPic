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

    fun getAlbums(sortingMode: SortingMode, sortingOrder: SortingOrder): Listing<Album> {
        when(sortingMode) {
            SortingMode.NAME -> return when (sortingOrder) {
                SortingOrder.ASCENDING -> Listing(list = albumDao.getIncludedAlbumsSyncSortedByNameAsc(),
                        loadingState = getLoadingState(true))
                SortingOrder.DESCENDING -> Listing(list = albumDao.getIncludedAlbumsSyncSortedByNameDesc(),
                        loadingState = getLoadingState(true))
            }
            SortingMode.DATE -> return when (sortingOrder) {
                SortingOrder.ASCENDING -> Listing(list = albumDao.getIncludedAlbumsSyncSortedByDateAsc(),
                        loadingState = getLoadingState(true))
                SortingOrder.DESCENDING -> Listing(list = albumDao.getIncludedAlbumsSyncSortedByDateDesc(),
                        loadingState = getLoadingState(true))
            }
            SortingMode.SIZE -> return when (sortingOrder) {
                SortingOrder.ASCENDING -> Listing(list = albumDao.getIncludedAlbumsSyncSortedBySizeAsc(),
                        loadingState = getLoadingState(true))
                SortingOrder.DESCENDING -> Listing(list = albumDao.getIncludedAlbumsSyncSortedBySizeDesc(),
                        loadingState = getLoadingState(true))
            }
            else -> return Listing(list = albumDao.getIncludedAlbumsSyncSortedByNameAsc(),
                    loadingState = getLoadingState(true))
        }

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

    fun getMedia(albumId: Long, sortingMode: SortingMode, sortingOrder: SortingOrder): Listing<Media> {
        when(sortingMode) {
            SortingMode.NAME -> return when (sortingOrder) {
                SortingOrder.ASCENDING -> Listing(list = mediaDao.getMediaForAlbumSyncSortedByNameAsc(albumId),
                        loadingState = getLoadingState(false))
                SortingOrder.DESCENDING -> Listing(list = mediaDao.getMediaForAlbumSyncSortedByNameDesc(albumId),
                        loadingState = getLoadingState(false))
            }
            SortingMode.DATE -> return when (sortingOrder) {
                SortingOrder.ASCENDING -> Listing(list = mediaDao.getMediaForAlbumSyncSortedByDateAsc(albumId),
                        loadingState = getLoadingState(false))
                SortingOrder.DESCENDING -> Listing(list = mediaDao.getMediaForAlbumSyncSortedByDateDesc(albumId),
                        loadingState = getLoadingState(false))
            }
            SortingMode.SIZE -> return when (sortingOrder) {
                SortingOrder.ASCENDING -> Listing(list = mediaDao.getMediaForAlbumSyncSortedBySizeAsc(albumId),
                        loadingState = getLoadingState(false))
                SortingOrder.DESCENDING -> Listing(list = mediaDao.getMediaForAlbumSyncSortedBySizeDesc(albumId),
                        loadingState = getLoadingState(false))
            }
            SortingMode.TYPE -> return when (sortingOrder) {
                SortingOrder.ASCENDING -> Listing(list = mediaDao.getMediaForAlbumSyncSortedByTypeAsc(albumId),
                        loadingState = getLoadingState(false))
                SortingOrder.DESCENDING -> Listing(list = mediaDao.getMediaForAlbumSyncSortedByTypeDesc(albumId),
                        loadingState = getLoadingState(false))
            }
            else -> return Listing(list = mediaDao.getMediaForAlbumSyncSortedByDateDesc(albumId),
                    loadingState = getLoadingState(false))
        }
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