//package org.horaapps.leafpic.data.provider
//
//import android.content.Context
//import com.orhanobut.hawk.Hawk
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import org.horaapps.leafpic.data.Album
//import org.horaapps.leafpic.data.AlbumInfo
//import org.horaapps.leafpic.data.Media
//import org.horaapps.leafpic.data.StorageHelper
//import org.horaapps.leafpic.data.filter.FoldersFileFilter
//import org.horaapps.leafpic.data.filter.ImageFileFilter
//import org.horaapps.leafpic.util.StringUtils
//import org.horaapps.leafpic.util.preferences.Prefs
//import java.io.File
//import java.util.*
//
//class HiddenAlbumsHelper {
//
//    companion object {
//        suspend fun getHiddenAlbums(context: Context, excludedAlbums: List<Album>): List<Album> {
//            return withContext(Dispatchers.Default) {
//                val includeVideo = Prefs.showVideos()
//                val list = mutableListOf<Album>()
//
//                val lastHidden = Hawk.get("h", ArrayList<String>())
//                val lastHiddenList = mutableListOf<Album>()
//                for (s in lastHidden)
//                    checkIfValidFolder(File(s), includeVideo)?.let {
//                        lastHiddenList.add(it)
//                    }
//
//                lastHiddenList.addAll(excludedAlbums)
//
//                for (storage in StorageHelper.getStorageRoots(context))
//                    fetchRecursivelyHiddenFolder(storage, list, lastHiddenList, includeVideo)
//                list
//            }
//        }
//
//        private fun fetchRecursivelyHiddenFolder(dir: File, albumList: MutableList<Album>, excludedAlbums: List<Album>,
//                                                 includeVideo: Boolean) {
//            if (!isExcluded(dir.path, excludedAlbums)) {
//                val folders = dir.listFiles(FoldersFileFilter())
//                if (folders != null) {
//                    for (temp in folders) {
//                        val nomedia = File(temp, ".nomedia")
//                        if (!isExcluded(temp.path, excludedAlbums) && (nomedia.exists() || temp.isHidden))
//                            checkIfValidFolder(temp, includeVideo)?.let {
//                                albumList.add(it)
//                            }
//
//                        fetchRecursivelyHiddenFolder(temp, albumList, excludedAlbums, includeVideo)
//                    }
//                }
//            }
//        }
//
//        private fun checkIfValidFolder(dir: File, includeVideo: Boolean): Album? {
//            val files = dir.listFiles(ImageFileFilter(includeVideo))
//            var album: Album? = null
//            if (files != null && files.isNotEmpty()) {
//                //valid folder
//                var lastMod = -1L
//                var choice: File? = null
//                for (file in files) {
//                    if (file.lastModified() > lastMod) {
//                        choice = file
//                        lastMod = file.lastModified()
//                    }
//                }
//                if (choice != null) {
//                    album = Album(path = dir.absolutePath, albumName = dir.name)
//                    album.fileCount = files.size
//                    album.albumInfo = AlbumInfo(dateModified = lastMod, coverPath = choice.absolutePath)
//                    return album
//                }
//            }
//            return album
//        }
//
//        private fun isExcluded(path: String, excludedAlbums: List<Album>): Boolean {
//            for (album in excludedAlbums)
//                if (path.startsWith(album.path))
//                    return true
//            return false
//        }
//    }
//}
