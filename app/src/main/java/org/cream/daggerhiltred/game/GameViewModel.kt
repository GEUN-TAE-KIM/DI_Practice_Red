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

import android.text.Spannable
import android.text.SpannableString
import android.text.style.TtsSpan
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.flow.*

// TODO
//  LiveData -> StateFlow
//  = 코루틴을 쓰기 떄문에 라이브데이터보다 앱 성능이 향상되며 Flow API를 사용이 가능해 가능성이 풍부해짐
//  ,
//  SavedStateHandler 도입, 중요 데이터를 저장
//  초기화 구문 개선

class GameViewModel() : ViewModel() {
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int>
        get() = _score

    private val _currentWordCount = MutableStateFlow(0)
    val currentWordCount: StateFlow<Int>
        get() = _currentWordCount

    private val _currentScrambledWord = MutableStateFlow<String>("")
    //Spannable 특정문자를 볼드체로 한다든가 변경해주는거
    val currentScrambledWord: StateFlow<Spannable> = _currentScrambledWord
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
    private var wordsList: MutableList<String> = mutableListOf()
    private lateinit var currentWord: String

    init {
        getNextWord()
    }

    /*
     * Updates currentWord and currentScrambledWord with the next word.
     */
    private fun getNextWord() {
        currentWord = allWordsList.random()
        val tempWord = currentWord.toCharArray()
        // 말그대로 단어 섞게 하는거
        tempWord.shuffle()

        while (String(tempWord).equals(currentWord, false)) {
            tempWord.shuffle()
        }
        if (wordsList.contains(currentWord)) {
            getNextWord()
        } else {
            Log.d("Unscramble", "currentWord= $currentWord")
            // 셔플로 뒤석인 단어들을 저장
            _currentScrambledWord.value = String(tempWord)
            _currentWordCount.value = _currentWordCount.value.inc()
            wordsList.add(currentWord)
        }
    }

    /*
     * Re-initializes the game data to restart the game.
     */
    fun reinitializeData() {
        _score.value = 0
        _currentWordCount.value = 0
        wordsList.clear()
        getNextWord()
    }

    /*
    * Increases the game score if the player’s word is correct.
    */
    private fun increaseScore() {
        _score.value = _score.value.plus(SCORE_INCREASE)
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
        return if (_currentWordCount.value!! < MAX_NO_OF_WORDS) {
            getNextWord()
            true
        } else false
    }
}
