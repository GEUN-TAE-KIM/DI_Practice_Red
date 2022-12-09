package org.cream.daggerhiltred.data

import android.app.Application
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(
    application: Application,
    private val dataSources: GameDataSources = GameDataSources(application)
) {

    val highScore: Flow<Int> =
        dataSources.gamePreferencesFlow.map { preferences -> preferences.highScore }

    suspend fun updateScore(score: Int) {
        dataSources.updateHighScore(score)
    }
}