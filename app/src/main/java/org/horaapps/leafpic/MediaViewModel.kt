package org.horaapps.leafpic

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.horaapps.leafpic.data.*
import org.horaapps.leafpic.data.sort.SortingMode
import org.horaapps.leafpic.data.sort.SortingOrder
import javax.inject.Inject

class MediaViewModel @Inject constructor(private val application: App,
                                         private val albumRepository: AlbumRepository)
    : AndroidViewModel(application) {

    private val _album = MutableLiveData<Album>()
    private var _sortingMode = SortingMode.DATE
    private var _sortingOrder = SortingOrder.DESCENDING

    private val mediaResult = Transformations.map(_album) { album ->
        albumRepository.getMedia(album.id, _sortingMode, _sortingOrder)
    }

    val media: LiveData<List<Media>> = Transformations.switchMap(mediaResult) {
        it.list
    }

    val mediaLoadingState: LiveData<LoadingState> = Transformations.switchMap(mediaResult) {
        it.loadingState
    }

    fun loadMedia(album: Album, sortingMode: SortingMode, sortingOrder: SortingOrder) {
        _sortingMode = sortingMode
        _sortingOrder = sortingOrder

        if (_album.value != album) {
            _album.value = album
        }
    }

    fun setSortOptions(sortingMode: SortingMode, sortingOrder: SortingOrder) {
        _sortingMode = sortingMode
        _sortingOrder = sortingOrder

        _album.value?.let {
            _album.value = it
        }
    }

    fun refreshMedia(album: Album) {
        viewModelScope.launch {
            albumRepository.loadMedia(application.contentResolver, album)
        }
    }

    fun setSortingMode(album: Album, sortingMode: SortingMode) {
        viewModelScope.launch {
            val newAlbum = album.copy(albumInfo = album.albumInfo.copy(sortingMode = sortingMode))
            albumRepository.updateAlbum(newAlbum)
        }
    }

    fun setSortingOrder(album: Album, sortingOrder: SortingOrder) {
        viewModelScope.launch {
            val newAlbum = album.copy(albumInfo = album.albumInfo.copy(sortingOrder = sortingOrder))
            albumRepository.updateAlbum(newAlbum)
        }
    }

    fun setCover(album: Album, coverPath: String) {
        viewModelScope.launch {
            val newAlbum = album.copy(albumInfo = album.albumInfo.copy(coverPath = coverPath))
            albumRepository.updateAlbum(newAlbum)
        }
    }
}