package org.horaapps.leafpic.data

import androidx.room.*

/**
 * Base Dao containing UPSERT workaround
 * https://tech.bakkenbaeck.com/post/room-insert-update
 */
@Dao
abstract class BaseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insert(album: AlbumK): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insert(obj: List<AlbumK>): List<Long>

    @Update
    protected abstract fun update(album: AlbumK)

    @Update
    protected abstract fun update(obj: List<AlbumK>)

    @Transaction
    open suspend fun insertOrUpdate(album: AlbumK) {
        val id = insert(album)
        if (id == -1L) update(album)
    }

    @Transaction
    open suspend fun insertOrUpdate(objList: List<AlbumK>) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<AlbumK>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) updateList.add(objList[i])
        }

        if (updateList.isNotEmpty()) update(updateList)
    }
}
