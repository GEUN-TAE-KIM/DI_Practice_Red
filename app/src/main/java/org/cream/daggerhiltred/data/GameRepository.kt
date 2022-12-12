package org.cream.daggerhiltred.data

import android.app.Application
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GameRepository @Inject constructor(
    application: Application,
    private val dataSources: GameDataSources
) {

    val highScore: Flow<Int> =
        dataSources.gamePreferencesFlow.map { preferences -> preferences.highScore }

    suspend fun updateScore(score: Int) {
        dataSources.updateHighScore(score)
    }
}