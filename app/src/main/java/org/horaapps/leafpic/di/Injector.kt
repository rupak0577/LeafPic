package org.horaapps.leafpic.di

import org.horaapps.leafpic.App

class Injector private constructor() {
    companion object {
        fun get(): AppComponent =
                App.getAppComponent()
    }
}
