package com.example.geoquiz

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

private const val TAG = "MainActivity"
private const val INDEX = "index"
private const val CHEAT_TOKEN = "cheat_tokens"
private const val REQUEST_CODE_CHEAT = 0
private const val REQUEST_CODE_RESULT = 0
private const val IS_CHEATER = "is_cheater"

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private lateinit var resetButton: Button
    private lateinit var resultButton: Button
    private lateinit var cheatButton: Button
    private lateinit var questionTextView: TextView
    private lateinit var questionStatus: TextView
    private lateinit var cheatsLeft: TextView
    private lateinit var timerTextView: TextView

    private lateinit var quizViewModel: QuizViewModel

    private var cheatTokens = 3

    private var timer: CountDownTimer? = null
    private var timerRunning = false
    private var elapsedSeconds = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        quizViewModel = ViewModelProvider(this).get(QuizViewModel::class.java)

        // Restore saved instance state if any
        val currentIndex = savedInstanceState?.getInt(INDEX, 0) ?: 0
        val isCheater = savedInstanceState?.getBoolean(IS_CHEATER, false) ?: false
        cheatTokens = savedInstanceState?.getInt(CHEAT_TOKEN, 3) ?: 3

        quizViewModel.isCheater = isCheater
        quizViewModel.currentIndex = currentIndex

        // UI elements
        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        previousButton = findViewById(R.id.previous_button)
        resetButton = findViewById(R.id.reset_button)
        resultButton = findViewById(R.id.result_button)
        cheatButton = findViewById(R.id.cheat_button)
        questionTextView = findViewById(R.id.question_text_view)
        questionStatus = findViewById(R.id.question_status)
        cheatsLeft = findViewById(R.id.cheats_left)
        timerTextView = findViewById(R.id.timer_text_view)

        // Button click listeners
        trueButton.setOnClickListener { view: View ->
            checkAnswer(true)
        }

        falseButton.setOnClickListener { view: View ->
            checkAnswer(false)
        }

        nextButton.setOnClickListener { view: View ->
            quizViewModel.moveToNext()
            updateQuestion()
            toggleAnswerButtons()
        }

        previousButton.setOnClickListener { view: View ->
            quizViewModel.moveToPrevious()
            updateQuestion()
            toggleAnswerButtons()
        }

        cheatButton.setOnClickListener {
            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue)
            startActivityForResult(intent, REQUEST_CODE_CHEAT)
        }

        resetButton.setOnClickListener {
            resetQuiz()
        }

        resultButton.setOnClickListener {
            val totalQuestionsAnswered = quizViewModel.countQuestionsAnswered()
            val totalScore = calculateScore()
            val cheatAttempts = 3 - cheatTokens
            val elapsedSeconds = this.elapsedSeconds
            val intent = ResultActivity.newIntent(this@MainActivity, totalQuestionsAnswered, totalScore, cheatAttempts, elapsedSeconds)
            startActivityForResult(intent, REQUEST_CODE_RESULT)
        }

        // Update UI elements
        updateQuestion()
        updateCheatsLeft()

        // Start timer if not already running
        if (savedInstanceState == null) {
            startTimer()
        } else {
            timerRunning = savedInstanceState.getBoolean("timerRunning")
            elapsedSeconds = savedInstanceState.getInt("elapsedSeconds")
            if (timerRunning) {
                startTimer()
            }
        }
    }

    private fun resetQuiz() {
        cheatTokens = 3
        cheatButton.isEnabled = true
        quizViewModel.resetScore()
        quizViewModel.resetAnswerState()
        quizViewModel.resetIsCheater()
        quizViewModel.resetCurrentIndex()
        updateQuestion()
        toggleAnswerButtons()
        updateCheatsLeft()

        // Stop and reset the timer
        timer?.cancel()
        elapsedSeconds = 0
        updateTimerText()
        startTimer()

        Toast.makeText(
            this,
            "Quiz successfully reset",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateQuestion() {
        val questionTextResId = quizViewModel.currentQuestionText
        questionTextView.setText(questionTextResId)

        questionStatus.text = "Question ${quizViewModel.currentIndex + 1} of ${quizViewModel.questionListSize}"
    }

    private fun startTimer() {
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedSeconds++
                updateTimerText()
            }

            override fun onFinish() {

            }
        }.start()

        timerRunning = true
    }

    private fun updateTimerText() {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        val timerText = String.format("%02d:%02d", minutes, seconds)
        timerTextView.text = timerText
    }

    private fun checkAnswer(userAnswer: Boolean) {
        val correctAnswer = quizViewModel.currentQuestionAnswer

        val messageResId = when {
            userAnswer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }

        Toast.makeText(
            this,
            messageResId,
            Toast.LENGTH_SHORT
        ).show()

        if (userAnswer == correctAnswer) {
            quizViewModel.incrementScore()
        }

        if (quizViewModel.isCheater) {
            cheatTokens--
        }

        quizViewModel.updateAnswerState()
        quizViewModel.resetIsCheater()
        toggleAnswerButtons()
        displayScoreToast()
        updateCheatsLeft()
        disableCheatButton()

        if (quizViewModel.allQuestionsAnswered()) {
            val scorePercentage = calculateScore()
            val scoreMessage = "Quiz completed! You scored $scorePercentage%"

            Toast.makeText(
                this,
                scoreMessage,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun toggleAnswerButtons() {
        if (quizViewModel.currentIsQuestionAnswered) {
            trueButton.isEnabled = false
            falseButton.isEnabled = false
        } else {
            trueButton.isEnabled = true
            falseButton.isEnabled = true
        }
    }

    private fun calculateScore(): Int {
        return (quizViewModel.score.toDouble() / quizViewModel.countQuestionsAnswered().toDouble() * 100).toInt()
    }

    private fun displayScoreToast() {
        if (quizViewModel.allQuestionsAnswered()) {
            val scoreMessage = "Quiz completed! You scored ${calculateScore()}%"

            Toast.makeText(
                this,
                scoreMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateCheatsLeft() {
        cheatsLeft.text = "Cheats Left : " + cheatTokens.toString()
    }

    private fun disableCheatButton() {
        if (cheatTokens < 1) {
            cheatButton.isEnabled = false
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt(INDEX, quizViewModel.currentIndex)
        savedInstanceState.putBoolean(IS_CHEATER, quizViewModel.isCheater)
        savedInstanceState.putInt(CHEAT_TOKEN, cheatTokens)
        savedInstanceState.putBoolean("timerRunning", timerRunning)
        savedInstanceState.putInt("elapsedSeconds", elapsedSeconds)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_CODE_CHEAT) {
            quizViewModel.isCheater = data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
        }
        disableCheatButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
