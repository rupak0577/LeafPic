package org.horaapps.leafpic.data

import androidx.room.*

/**
 * Base Dao containing UPSERT workaround
 * https://tech.bakkenbaeck.com/post/room-insert-update
 */
@Dao
abstract class BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insert(item: T): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insert(objList: List<T>): List<Long>

    @Update
    protected abstract fun update(item: T)

    @Update
    protected abstract fun update(item: List<T>)

    @Transaction
    open suspend fun insertOrUpdate(item: T) {
        val id = insert(item)
        if (id == -1L) update(item)
    }

    @Transaction
    open suspend fun insertOrUpdate(objList: List<T>) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<T>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) updateList.add(objList[i])
        }

        if (updateList.isNotEmpty()) update(updateList)
    }
}
