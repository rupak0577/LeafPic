package org.horaapps.leafpic.fragments

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.horaapps.leafpic.App
import org.horaapps.leafpic.EXCLUDED
import org.horaapps.leafpic.data.Album
import org.horaapps.leafpic.data.AlbumRepository
import org.horaapps.leafpic.data.LoadingState
import org.horaapps.leafpic.data.Media
import org.horaapps.leafpic.data.sort.SortingMode
import org.horaapps.leafpic.data.sort.SortingOrder
import javax.inject.Inject

class AlbumsViewModel @Inject constructor(private val application: App,
                                          private val albumRepository: AlbumRepository)
    : AndroidViewModel(application) {
    private val _status = MutableLiveData<Int>()
    private val _album = MutableLiveData<Album>()

    private val albumsResult = map(_status) { status ->
        albumRepository.getAlbums(status)
    }

    private val mediaResult = map(_album) { album ->
        albumRepository.getMedia(album)
    }

    val albums: LiveData<List<Album>> = switchMap(albumsResult) {
        it.list
    }

    val albumsLoadingState: LiveData<LoadingState> = switchMap(albumsResult) {
        it.loadingState
    }

    val media: LiveData<List<Media>> = switchMap(mediaResult) {
        it.list
    }

    val mediaLoadingState: LiveData<LoadingState> = switchMap(mediaResult) {
        it.loadingState
    }

    fun setStatus(status: Int) {
        if (_status.value != status) {
            _status.value = status

            viewModelScope.launch {
                albumRepository.loadAlbums(application.contentResolver, SortingMode.DATE, SortingOrder.ASCENDING)
            }
        }
    }

    fun setAlbum(album: Album) {
        if (_album.value != album) {
            _album.value = album

            viewModelScope.launch {
                albumRepository.loadMedia(application.contentResolver, album)
            }
        }
    }

    fun setPinned(album: Album, pinned: Boolean) {
        viewModelScope.launch {
            val newAlbum = album.copy(albumInfo = album.albumInfo.copy(pinned = pinned))
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
            val newAlbum = album.copy(albumInfo = album.albumInfo.copy(status = EXCLUDED))
            albumRepository.upsertAlbum(newAlbum)
        }
    }

    fun excludeAlbums(albums: List<Album>) {
        viewModelScope.launch {
            val newAlbums = mutableListOf<Album>()
            albums.forEach { oldAlbum ->
                newAlbums.add(oldAlbum.copy(albumInfo = oldAlbum.albumInfo.copy(status = EXCLUDED)))
            }
            albumRepository.upsertAlbums(newAlbums.toList())
        }
    }

    fun setSortingMode(album: Album, sortingMode: SortingMode) {
        viewModelScope.launch {
            val newAlbum = album.copy(albumInfo = album.albumInfo.copy(sortingMode = sortingMode))
            albumRepository.updateAlbum(newAlbum)
        }
    }

    fun setSortingMode(album: Album, sortingOrder: SortingOrder) {
        viewModelScope.launch {
            val newAlbum = album.copy(albumInfo = album.albumInfo.copy(sortingOrder = sortingOrder))
            albumRepository.updateAlbum(newAlbum)
        }
    }
}
