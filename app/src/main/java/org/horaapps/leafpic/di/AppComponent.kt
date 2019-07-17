package org.horaapps.leafpic.di

import androidx.lifecycle.ViewModelProvider
import dagger.BindsInstance
import dagger.Component
import org.horaapps.leafpic.App
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
        fun application(application: App): Builder

        fun build(): AppComponent
    }

    fun albumDao(): AlbumDao
    fun db(): AppDatabase
    fun albumRepository(): AlbumRepository
    fun viewModelFactory(): ViewModelProvider.Factory
}