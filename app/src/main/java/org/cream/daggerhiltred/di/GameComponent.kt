package org.cream.daggerhiltred.di

import dagger.Subcomponent
import org.cream.daggerhiltred.game.GameFragment

@Subcomponent
interface GameComponent {
    @Subcomponent.Factory
    interface  Factory {
        fun create(): GameComponent
    }

    fun inject(gameFragment: GameFragment)
}