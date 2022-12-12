package org.cream.daggerhiltred.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.cream.daggerhiltred.game.GameViewModel

@Module(subcomponents = [GameComponent::class])
interface GameModule {
    @Binds
    @IntoMap
    @ViewModelKey(GameViewModel::class)
    fun bindGameViewModel(viewModel: GameViewModel): ViewModel
}