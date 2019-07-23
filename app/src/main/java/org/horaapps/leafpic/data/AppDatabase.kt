package org.horaapps.leafpic.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Album::class, Media::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun mediaDao(): MediaDao
}