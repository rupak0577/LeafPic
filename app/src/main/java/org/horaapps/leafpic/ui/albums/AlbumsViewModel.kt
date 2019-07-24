package org.horaapps.leafpic.ui.albums

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.horaapps.leafpic.App
import org.horaapps.leafpic.data.Album
import org.horaapps.leafpic.data.AlbumRepository
import org.horaapps.leafpic.data.DoubleTrigger
import org.horaapps.leafpic.data.LoadingState
import org.horaapps.leafpic.data.sort.SortingMode
import org.horaapps.leafpic.data.sort.SortingOrder
import javax.inject.Inject

class AlbumsViewModel @Inject constructor(private val application: App,
                                          private val albumRepository: AlbumRepository)
    : AndroidViewModel(application) {
    private val _sortingMode = MutableLiveData<SortingMode>()
    private val _sortingOrder = MutableLiveData<SortingOrder>()

    private val albumsResult = map(DoubleTrigger(_sortingMode, _sortingOrder)) {
        albumRepository.getAlbums(it.first ?: SortingMode.DATE,
                it.second ?: SortingOrder.DESCENDING)
    }

    val albums: LiveData<List<Album>> = switchMap(albumsResult) {
        it.list
    }

    val albumsLoadingState: LiveData<LoadingState> = switchMap(albumsResult) {
        it.loadingState
    }

    fun loadAlbums(sortingMode: SortingMode, sortingOrder: SortingOrder) {
        if (_sortingMode.value != sortingMode)
            _sortingMode.value = sortingMode
        if (_sortingOrder.value != sortingOrder)
            _sortingOrder.value = sortingOrder
    }

    fun refreshAlbums() {
        viewModelScope.launch {
            albumRepository.loadAlbums(application.contentResolver, application.getExternalFilesDirs("external"),
                    _sortingMode.value ?: SortingMode.DATE,
                    _sortingOrder.value ?: SortingOrder.DESCENDING)
        }
    }

    fun setPinned(album: Album, pinned: Boolean) {
        viewModelScope.launch {
            val newAlbum = album.copy(albumInfo = album.albumInfo.copy(isPinned = pinned))
            albumRepository.updateAlbum(newAlbum)
        }
    }

    fun setCover(album: Album, coverPath: String) {
        viewModelScope.launch {
            val newAlbum = album.copy(albumInfo = album.albumInfo.copy(coverPath = coverPath))
            albumRepository.updateAlbum(newAlbum)
        }
    }

    fun excludeAlbum(album: Album) {
        viewModelScope.launch {
            val newAlbum = album.copy(albumInfo = album.albumInfo.copy(isExcluded = true))
            albumRepository.upsertAlbum(newAlbum)
        }
    }

    fun excludeAlbums(albums: List<Album>) {
        viewModelScope.launch {
            val newAlbums = mutableListOf<Album>()
            albums.forEach { oldAlbum ->
                newAlbums.add(oldAlbum.copy(albumInfo = oldAlbum.albumInfo.copy(isExcluded = true)))
            }
            albumRepository.upsertAlbums(newAlbums.toList())
        }
    }
}
