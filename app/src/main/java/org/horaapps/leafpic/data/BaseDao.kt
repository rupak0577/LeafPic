package org.horaapps.leafpic.data

import androidx.room.*

/**
 * Base Dao containing UPSERT workaround
 * https://tech.bakkenbaeck.com/post/room-insert-update
 */
@Dao
abstract class BaseDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insert(album: Album): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insert(obj: List<Album>): List<Long>

    @Update
    protected abstract fun update(album: Album)

    @Update
    protected abstract fun update(obj: List<Album>)

    @Transaction
    open suspend fun insertOrUpdate(album: Album) {
        val id = insert(album)
        if (id == -1L) update(album)
    }

    @Transaction
    open suspend fun insertOrUpdate(objList: List<Album>) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<Album>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) updateList.add(objList[i])
        }

        if (updateList.isNotEmpty()) update(updateList)
    }
}
