package org.cream.daggerhiltred.di

import dagger.Component
import org.cream.daggerhiltred.MainActivity
import org.cream.daggerhiltred.data.AppModule

@Component(modules = [AppModule::class, GameModule::class, ViewModelBuilder::class])
interface AppComponent {
    fun inject(activity: MainActivity)

    fun gameComponent(): GameComponent.Factory
}