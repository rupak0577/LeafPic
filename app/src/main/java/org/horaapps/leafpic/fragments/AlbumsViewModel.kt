package org.horaapps.leafpic.fragments

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.horaapps.leafpic.App
import org.horaapps.leafpic.EXCLUDED
import org.horaapps.leafpic.data.Album
import org.horaapps.leafpic.data.AlbumRepository
import org.horaapps.leafpic.data.Media
import org.horaapps.leafpic.data.Resource
import org.horaapps.leafpic.data.sort.SortingMode
import org.horaapps.leafpic.data.sort.SortingOrder
import javax.inject.Inject

class AlbumsViewModel @Inject constructor(private val application: App,
                                          private val albumRepository: AlbumRepository)
    : AndroidViewModel(application) {
    private val _hidden = MutableLiveData<Boolean>()
    private val _album = MutableLiveData<Album>()

    val albums: LiveData<Resource<List<Album>>> = _hidden.switchMap { showHidden ->
        liveData {
            emit(Resource.loading(null))

            if (showHidden) {
                try {
                    val list = albumRepository.loadHiddenAlbums(application)
                    emit(Resource.success(list))
                } catch (e: Exception) {
                    emit(Resource.error(e.toString(), null))
                }
            } else {
                try {
                    val list = albumRepository.loadAlbums(application.contentResolver, SortingMode.DATE,
                            SortingOrder.ASCENDING)
                    emit(Resource.success(list))
                } catch (exception: Exception) {
                    emit(Resource.error(exception.toString(), null))
                }
            }
        }
    }

    val media: LiveData<Resource<List<Media>>> = _album.switchMap { album ->
        liveData {
            emit(Resource.loading(null))

            try {
                val list = albumRepository.loadMedia(application.contentResolver, album)
                emit(Resource.success(list))
            } catch (e: Exception) {
                emit(Resource.error(e.toString(), null))
            }
        }
    }

    fun setShowHidden(hidden: Boolean) {
        if (_hidden.value != hidden) {
            _hidden.value = hidden
        }
    }

    fun setAlbum(album: Album) {
        if (_album.value != album) {
            _album.value = album
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
