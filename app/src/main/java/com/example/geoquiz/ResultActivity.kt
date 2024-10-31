package com.example.geoquiz

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

private const val EXTRA_TOTAL_QUESTIONS_ANSWERED = "com.example.geoquiz.total_questions_answered"
private const val EXTRA_TOTAL_SCORE = "com.example.geoquiz.total_score"
private const val EXTRA_TOTAL_CHEAT_ATTEMPTS = "com.example.geoquiz.total_cheat_attempts"
private const val EXTRA_ELAPSED_SECONDS = "com.example.geoquiz.elapsed_seconds"

class ResultActivity : AppCompatActivity() {
    private lateinit var totalQuestionsAnsweredText: TextView
    private lateinit var totalScoreText: TextView
    private lateinit var totalCheatAttemptsText: TextView
    private lateinit var elapsedTimeText: TextView
    private lateinit var returnToQuizButton: Button

    private val resultViewModel: ResultViewModel by lazy {
        ViewModelProvider(this).get(ResultViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        totalQuestionsAnsweredText = findViewById(R.id.total_questions_answered)
        totalScoreText = findViewById(R.id.total_score)
        totalCheatAttemptsText = findViewById(R.id.total_cheat_attempts)
        elapsedTimeText = findViewById(R.id.elapsed_time_text)
        returnToQuizButton = findViewById(R.id.return_to_quiz_button)

        val totalQuestionsAnswered = intent.getIntExtra(EXTRA_TOTAL_QUESTIONS_ANSWERED, 0)
        val totalScore = intent.getIntExtra(EXTRA_TOTAL_SCORE, 0)
        val totalCheatAttempts = intent.getIntExtra(EXTRA_TOTAL_CHEAT_ATTEMPTS, 0)
        val elapsedSeconds = intent.getIntExtra(EXTRA_ELAPSED_SECONDS, 0)

        totalQuestionsAnsweredText.text = "Total Questions Answered: $totalQuestionsAnswered"
        totalScoreText.text = "Total Score: $totalScore%"
        totalCheatAttemptsText.text = "Total Cheat Attempts: $totalCheatAttempts"
        elapsedTimeText.text = formatElapsedTime(elapsedSeconds)

        returnToQuizButton.setOnClickListener {
            finish()
        }
    }

    private fun formatElapsedTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "Elapsed Time: $minutes minute(s) $remainingSeconds seconds"
    }

    companion object {
        fun newIntent(
            packageContext: Context,
            totalQuestionsAnswered: Int,
            totalScore: Int,
            cheatAttempts: Int,
            elapsedSeconds: Int
        ): Intent {
            return Intent(packageContext, ResultActivity::class.java).apply {
                putExtra(EXTRA_TOTAL_QUESTIONS_ANSWERED, totalQuestionsAnswered)
                putExtra(EXTRA_TOTAL_SCORE, totalScore)
                putExtra(EXTRA_TOTAL_CHEAT_ATTEMPTS, cheatAttempts)
                putExtra(EXTRA_ELAPSED_SECONDS, elapsedSeconds)
            }
        }
    }
}
