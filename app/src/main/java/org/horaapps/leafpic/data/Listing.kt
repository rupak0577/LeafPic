package org.horaapps.leafpic.data

import androidx.lifecycle.LiveData

data class Listing<T>(
        val list: LiveData<List<T>>,
        val loadingState: LiveData<LoadingState>
)