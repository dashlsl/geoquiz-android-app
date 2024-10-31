package com.example.geoquiz

import androidx.lifecycle.ViewModel

private const val TAG = "QuizViewModel"

class QuizViewModel : ViewModel() {
    // Initializing questions
    private val questionList = listOf(
        Question(R.string.question_id_01, false, false),
        Question(R.string.question_id_02, true, false),
        Question(R.string.question_id_03, true, false),
        Question(R.string.question_id_04, false, false),
        Question(R.string.question_id_05, true, false),
        Question(R.string.question_id_06, true, false),
        Question(R.string.question_id_07, true, false),
        Question(R.string.question_id_08, false, false),
        Question(R.string.question_id_09, true, false),
        Question(R.string.question_id_10, false, false)
    )

    /*
    VARIABLES & PROPERTIES
     */
    var currentIndex = 0
    var isCheater = false
    var score = 0

    val currentQuestionAnswer: Boolean
        get() = questionList[currentIndex].answer
    val currentQuestionText: Int
        get() = questionList[currentIndex].textResId
    val currentIsQuestionAnswered: Boolean
        get() = questionList[currentIndex].isQuestionAnswered
    val questionListSize: Int
        get() = questionList.size

    /*
    FUNCTIONS
     */

    fun moveToNext() {
        currentIndex = (currentIndex + 1) % questionList.size
    }

    fun moveToPrevious() {
        currentIndex = (currentIndex - 1 + questionList.size) % questionList.size
    }

    fun updateAnswerState() {
        questionList[currentIndex].isQuestionAnswered = true
    }

    fun allQuestionsAnswered(): Boolean {
        return questionList.all { it.isQuestionAnswered }
    }

    fun countQuestionsAnswered(): Int {
        return questionList.count { it.isQuestionAnswered }
    }

    fun incrementScore() {
        score++
    }

    // Reset functions

    fun resetScore() {
        score = 0
    }

    fun resetAnswerState() {
        questionList.forEach {
            it.isQuestionAnswered = false
        }
    }

    fun resetCurrentIndex() {
        currentIndex = 0
    }

    fun resetIsCheater() {
        isCheater = false
    }
}