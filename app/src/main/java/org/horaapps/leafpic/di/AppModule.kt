package org.horaapps.leafpic.di

import androidx.room.Room
import dagger.Module
import dagger.Provides
import org.horaapps.leafpic.App
import org.horaapps.leafpic.data.AlbumDao
import org.horaapps.leafpic.data.AppDatabase
import org.horaapps.leafpic.data.MediaDao
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {
    @Singleton
    @Provides
    fun provideDb(app: App): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "leafpic-db")
                .fallbackToDestructiveMigration()
                .build()
    }

    @Singleton
    @Provides
    fun provideAlbumDao(db: AppDatabase): AlbumDao {
        return db.albumDao()
    }

    @Singleton
    @Provides
    fun provideMediaDao(db: AppDatabase): MediaDao {
        return db.mediaDao()
    }
}