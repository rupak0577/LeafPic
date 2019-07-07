package org.horaapps.leafpic.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import org.horaapps.leafpic.data.AlbumDao
import org.horaapps.leafpic.data.AlbumRepository
import org.horaapps.leafpic.data.AppDatabase
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun albumDao(): AlbumDao
    fun db(): AppDatabase
    fun albumRepository(): AlbumRepository
}