/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cream.daggerhiltred.game

import android.app.Application
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.TtsSpan
import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.cream.daggerhiltred.data.GameRepository
import java.util.*
import kotlin.random.Random

// TODO
//  LiveData -> StateFlow
//  = 코루틴을 쓰기 떄문에 라이브데이터보다 앱 성능이 향상되며 Flow API를 사용이 가능해 가능성이 풍부해짐
//  ,
//  SavedStateHandler 도입, 중요 데이터를 저장
//  ,
//  초기화 구문 개선

class SaveableMutableStateFlow<T>(
    private val savedStateHandle: SavedStateHandle,
    private val key: String,
    initialValue: T
) {
    private val state: StateFlow<T> = savedStateHandle.getStateFlow(key, initialValue)
    var value: T
        get() = state.value
        set(value) {
            savedStateHandle[key] = value
        }

    fun asStatedFlow(): StateFlow<T> = state
}

fun <T> SavedStateHandle.getMutableStateFlow(
    key: String,
    initialValue: T
): SaveableMutableStateFlow<T> =
    SaveableMutableStateFlow(this, key, initialValue)

class GameViewModel(
    private val stateHandler: SavedStateHandle,
    private val repository: GameRepository
) : ViewModel() {

    private val _score = stateHandler.getMutableStateFlow("score", 0)
    val score: StateFlow<Int>
        get() = _score.asStatedFlow()

    val highScore: StateFlow<Int> = repository.highScore.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), 0
    )

    /*private val _score = stateHandler.getStateFlow("score", 0)
    private fun setScore(value: Int) {
        stateHandler["score"] = value
    }*/

    private val _currentWordCount = stateHandler.getMutableStateFlow("currentWordCount", 0)
    val currentWordCount: StateFlow<Int>
        get() = _currentWordCount.asStatedFlow()

    private val _currentScrambledWord = stateHandler.getMutableStateFlow("currentScrambledWord", "")

    //Spannable 특정문자를 볼드체로 한다든가 변경해주는거
    val currentScrambledWord: StateFlow<Spannable> = _currentScrambledWord
        .asStatedFlow()
        .onSubscription {
            if (currentWord.isEmpty())
                nextWord()
        }
        // map -> 말그대로 형태 필터해서 바꾸는거
        .map {
            val scrambledWord = it.toString()
            val spannable: Spannable = SpannableString(scrambledWord)
            spannable.setSpan(
                TtsSpan.VerbatimBuilder(scrambledWord).build(),
                0,
                scrambledWord.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            spannable
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpannableString(""))


    // List of words used in the game
    private var wordsList: List<String>
        get() = stateHandler["wordsList"] ?: emptyList()
        set(value) {
            stateHandler["wordsList"] = value
        }

    private var currentWord: String
        get() = stateHandler["currentWord"] ?: ""
        set(value) {

            val tempWord = value.toCharArray()
            // 말그대로 단어 섞게 하는거
            do {
                tempWord.shuffle()
            } while (String(tempWord) == value)

            Log.d("Unscramble", "currentWord= $value")
            // 셔플로 뒤석인 단어들을 저장
            _currentScrambledWord.value = String(tempWord)
            _currentWordCount.value += 1
            wordsList = wordsList + currentWord

            stateHandler["currentWord"] = value
        }

    /*
     * Updates currentWord and currentScrambledWord with the next word.
     */
    /* private fun getNextWord() {

         var nextWord: String

         do {
             nextWord = allWordsList.random(Random(Calendar.getInstance().timeInMillis))
         } while (wordsList.contains(currentWord))
         currentWord = nextWord

     }*/

    /*
     * Re-initializes the game data to restart the game.
     */
    fun reinitializeData() {
        _score.value = 0
        _currentWordCount.value = 0
        wordsList = emptyList()
        nextWord()
    }

    /*
    * Increases the game score if the player’s word is correct.
    */
    private fun increaseScore() {
        _score.value += SCORE_INCREASE

        viewModelScope.launch {
            repository.updateScore(_score.value)
        }
    }

    /*
    * Returns true if the player word is correct.
    * Increases the score accordingly.
    */
    fun isUserWordCorrect(playerWord: String): Boolean {
        if (playerWord.equals(currentWord, true)) {
            increaseScore()
            return true
        }
        return false
    }

    /*
    * Returns true if the current word count is less than MAX_NO_OF_WORDS
    */
    fun nextWord(): Boolean {
        return if (_currentWordCount.value < MAX_NO_OF_WORDS) {
            var nextWord: String

            do {
                nextWord = allWordsList.random(Random(Calendar.getInstance().timeInMillis))
            } while (wordsList.contains(currentWord))
            currentWord = nextWord

            true
        } else false
    }
}

class GameViewModelFactory(
    private val application: Application, owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null
) :
    AbstractSavedStateViewModelFactory(owner,defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        require(modelClass.isAssignableFrom(GameViewModel::class.java)) {
            "Unknown ViewModel class"
        }
        @Suppress("UNCHECKED_CAST")
        return GameViewModel(
            stateHandler = handle,
            repository = GameRepository(application)
        ) as T
    }

}
