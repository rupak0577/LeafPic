package org.horaapps.leafpic.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.horaapps.leafpic.fragments.AlbumsViewModel
import org.horaapps.leafpic.util.LeafPicViewModelFactory

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(AlbumsViewModel::class)
    abstract fun bindAlbumsViewModel(userViewModel: AlbumsViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: LeafPicViewModelFactory): ViewModelProvider.Factory
}
