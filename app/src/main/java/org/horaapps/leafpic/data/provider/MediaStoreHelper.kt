package org.horaapps.leafpic.data.provider

import android.content.ContentResolver
import android.database.Cursor
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.horaapps.leafpic.data.ALL_MEDIA_ALBUM_ID
import org.horaapps.leafpic.data.Album
import org.horaapps.leafpic.data.AlbumInfo
import org.horaapps.leafpic.data.Media
import org.horaapps.leafpic.data.filter.ImageFileFilter
import org.horaapps.leafpic.data.sort.SortingMode
import org.horaapps.leafpic.data.sort.SortingOrder
import org.horaapps.leafpic.util.MimeTypeUtils
import org.horaapps.leafpic.util.StringUtils
import org.horaapps.leafpic.util.preferences.Prefs
import timber.log.Timber
import java.io.File

class MediaStoreHelper {

    companion object {
        private const val mediaType: String = MediaStore.Files.FileColumns.MEDIA_TYPE
        private const val mediaTypeImage: Int = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
        private const val mediaTypeVideo: Int = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO

        private val albumProjection = arrayOf(MediaStore.Files.FileColumns.PARENT,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                "count(*)",
                MediaStore.Images.Media.DATA,
                "max(" + MediaStore.Images.Media.DATE_MODIFIED + ")",
                MediaStore.Images.Media.BUCKET_ID)

        private val mediaProjection = arrayOf(MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_MODIFIED, MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE, MediaStore.Images.Media.ORIENTATION)

        suspend fun getAlbums(contentResolver: ContentResolver, externalFilesDir: Array<File>, excludedAlbums: List<Album>,
                              sortingMode: SortingMode, sortingOrder: SortingOrder): List<Album> {
            return withContext(Dispatchers.IO) {
                val query = Query.Builder()
                        .uri(MediaStore.Files.getContentUri("external"))
                        .projection(albumProjection)
                        .sort(sortingMode.albumsColumn)
                        .ascending(sortingOrder.isAscending)

                val selectionString = StringBuilder()

                if (Prefs.showVideos()) {
                    selectionString.apply {
                        append("$mediaType=$mediaTypeImage")
                        append(" or ")
                        append("$mediaType=$mediaTypeVideo")
                        append(")")
                    }
                } else {
                    selectionString.apply {
                        append("$mediaType=$mediaTypeImage")
                        append(")")
                    }
                }

                val storageRoots = getStorageRoots(externalFilesDir)
                selectionString.append(" group by (${MediaStore.Files.FileColumns.PARENT}) ")
                        .append(getHavingClause(excludedAlbums.size + storageRoots.size))

                query.selection(selectionString.toString())

                // it has a lot of garbage
                storageRoots.forEach {
                    query.args(File(it.path, "Android").path)
                }

                //NOTE: LIKE params for query
                excludedAlbums.forEach {
                    query.args("${it.path}%")
                }

                val builtQuery = query.build()
                Timber.i(builtQuery.toString())

                val cursor: Cursor? = builtQuery.getCursor(contentResolver)
                mutableListOf<Album>().apply {
                    if (cursor != null && cursor.count > 0)
                        while (cursor.moveToNext())
                            add(cursor.toAlbum())
                    cursor?.close()
                }
            }
        }

        suspend fun getMedia(contentResolver: ContentResolver, album: Album): List<Media> {
            return when {
                album.idxParent == -1L -> getMediaFromStorage(album)
                album.idxParent == ALL_MEDIA_ALBUM_ID -> getAllMediaFromMediaStore(contentResolver, album)
                else -> getMediaFromMediaStore(contentResolver, album)
            }
        }

        private suspend fun getAllMediaFromMediaStore(contentResolver: ContentResolver, album: Album): List<Media> {
            return withContext(Dispatchers.IO) {
                val query = Query.Builder()
                        .uri(MediaStore.Files.getContentUri("external"))
                        .projection(mediaProjection)

                val selectionString = StringBuilder()

                if (Prefs.showVideos()) {
                    selectionString.apply {
                        append("$mediaType=$mediaTypeImage")
                        append(" or ")
                        append("$mediaType=$mediaTypeVideo")
                        append(")")
                    }
                } else {
                    selectionString.apply {
                        append("$mediaType=$mediaTypeImage") // to be closed by ContentResolver
                    }
                }

                query.selection(selectionString.toString())

                val builtQuery = query.build()
                Timber.i(builtQuery.toString())

                val cursor: Cursor? = builtQuery.getCursor(contentResolver)
                mutableListOf<Media>().apply {
                    if (cursor != null && cursor.count > 0)
                        while (cursor.moveToNext())
                            add(cursor.toMedia(albumId = album.id))
                    cursor?.close()
                }
            }
        }

        private suspend fun getMediaFromStorage(album: Album): List<Media> {
            return withContext(Dispatchers.Default) {
                val list = mutableListOf<Media>()
                File(album.path)
                        .listFiles(ImageFileFilter(Prefs.showVideos()))
                        ?.let {
                            for (file in it)
                                list.add(file.toMedia(albumId = album.id))
                        }
                list
            }
        }

        private suspend fun getMediaFromMediaStore(contentResolver: ContentResolver, album: Album): List<Media> {
            return withContext(Dispatchers.IO) {
                val query = Query.Builder()
                        .uri(MediaStore.Files.getContentUri("external"))
                        .projection(mediaProjection)

                val selectionString = StringBuilder()

                if (Prefs.showVideos()) {
                    selectionString.apply {
                        append("$mediaType=$mediaTypeImage")
                        append(" or ")
                        append("$mediaType=$mediaTypeVideo")
                        append(")")
                        append(" and ")
                        append("(")
                        append("${MediaStore.Files.FileColumns.PARENT}=${album.idxParent}") // to be closed by ContentResolver
                    }
                } else {
                    selectionString.apply {
                        append("$mediaType=$mediaTypeImage")
                        append(" and ")
                        append("(")
                        append("${MediaStore.Files.FileColumns.PARENT}=${album.idxParent}") // to be closed by ContentResolver
                    }
                }

                query.selection(selectionString.toString())

                val builtQuery = query.build()
                Timber.i(builtQuery.toString())

                val cursor: Cursor? = builtQuery.getCursor(contentResolver)
                mutableListOf<Media>().apply {
                    if (cursor != null && cursor.count > 0)
                        while (cursor.moveToNext())
                            add(cursor.toMedia(albumId = album.id))
                    cursor?.close()
                }
            }
        }

        private fun getHavingClause(excludedCount: Int): String {
            val res = StringBuilder()
            res.append("HAVING (")

            res.append(MediaStore.Images.Media.DATA).append(" NOT LIKE ?")

            for (i in 1 until excludedCount)
                res.append(" AND ")
                        .append(MediaStore.Images.Media.DATA)
                        .append(" NOT LIKE ?")

            // NOTE: dont close ths parenthesis it will be closed by ContentResolver
            //res.append(")");

            return res.toString()
        }

        private fun Cursor.toAlbum(): Album {
            return Album(id = this.getString(4).toLong(),
                    path = StringUtils.getBucketPathByImagePath(this.getString(3)),
                    albumName = this.getString(1),
                    idxParent = this.getLong(0),
                    fileCount = this.getInt(2),
                    albumInfo = AlbumInfo(dateModified = this.getLong(4),
                            coverPath = this.getString(3)))
        }

        private fun Cursor.toMedia(albumId: Long): Media {
            return Media(path = getString(0), name = StringUtils.getPhotoNameByPath(getString(0)),
                    albumId = albumId, size = getLong(3),
                    mimeType = getString(2), dateModified = getLong(1),
                    orientation = getInt(4))
        }

        private fun File.toMedia(albumId: Long): Media {
            return Media(path = this.path, name = StringUtils.getPhotoNameByPath(this.path),
                    albumId = albumId, size = this.length(),
                    mimeType = MimeTypeUtils.getMimeType(this.path),
                    dateModified = this.lastModified())
        }

        private fun getStorageRoots(dir: Array<File>): Array<File> {
            val paths = HashSet<File>()
            for (file in dir) {
                val index = file.absolutePath.lastIndexOf("/Android/data")
                if (index < 0)
                    Timber.w("Unexpected external file dir: %s", file.absolutePath)
                else
                    paths.add(File(file.absolutePath.substring(0, index)))
            }
            return paths.toTypedArray()
        }
    }
}