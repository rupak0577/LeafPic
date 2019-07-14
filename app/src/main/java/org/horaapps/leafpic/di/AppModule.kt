package org.horaapps.leafpic.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import org.horaapps.leafpic.data.AlbumDao
import org.horaapps.leafpic.data.AppDatabase
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {
    @Singleton
    @Provides
    fun provideDb(app: Application): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "leafpic-db")
                .fallbackToDestructiveMigration()
                .build()
    }

    @Singleton
    @Provides
    fun provideAlbumDao(db: AppDatabase): AlbumDao {
        return db.albumDao()
    }
}