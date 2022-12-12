package org.cream.daggerhiltred

import android.app.Application
import org.cream.daggerhiltred.data.AppModule
import org.cream.daggerhiltred.di.AppComponent
import org.cream.daggerhiltred.di.DaggerAppComponent

class GameApplication : Application() {

    val appComponent: AppComponent = DaggerAppComponent.builder()
        .appModule(AppModule(this))
        .build()
}