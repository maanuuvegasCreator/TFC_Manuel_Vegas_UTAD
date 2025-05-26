package com.example.imdbapplogin.trivial

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.imdbapplogin.TranslateService
import com.example.imdbapplogin.model.TranslateRequest
import com.example.imdbapplogin.model.TriviaQuestion
import com.example.imdbapplogin.repository.TriviaRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
class TrivialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TriviaRepository()
    private val prefs = application.getSharedPreferences("trivial_prefs", Context.MODE_PRIVATE)
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    private val _questions = mutableListOf<TriviaQuestion>()
    private var currentIndex = 0

    private val _currentQuestion = MutableStateFlow<TriviaQuestion?>(null)
    val currentQuestion: StateFlow<TriviaQuestion?> = _currentQuestion

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _timeLeft = MutableStateFlow(30)
    val timeLeft: StateFlow<Int> = _timeLeft

    private val _questionsAnswered = MutableStateFlow(0)
    val questionsAnswered: StateFlow<Int> = _questionsAnswered

    private val _gameOver = MutableStateFlow(false)
    val gameOver: StateFlow<Boolean> = _gameOver

    private val _waitUntilTomorrow = MutableStateFlow<Duration?>(null)
    val waitUntilTomorrow: StateFlow<Duration?> = _waitUntilTomorrow

    private var timerJob: Job? = null
    private var isFetching = false

    init {
        if (userId != null) {
            checkDailyLimit(userId)
        } else {
            _error.value = "Usuario no identificado"
        }
    }

    private fun getPrefsKey(userId: String) = "last_played_$userId"

    private fun checkDailyLimit(userId: String) {
        val lastPlayedStr = prefs.getString(getPrefsKey(userId), null)
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_DATE_TIME

        if (lastPlayedStr != null) {
            val lastPlayed = LocalDateTime.parse(lastPlayedStr, formatter)
            if (lastPlayed.toLocalDate() == now.toLocalDate()) {
                _waitUntilTomorrow.value = Duration.between(now, lastPlayed.plusDays(1))
                _gameOver.value = true
                return
            }
        }

        fetchBatchOfQuestions()
    }

    private fun markAsPlayedToday(userId: String) {
        val now = LocalDateTime.now()
        prefs.edit().putString(getPrefsKey(userId), now.format(DateTimeFormatter.ISO_DATE_TIME))
            .apply()
    }

    private fun fetchBatchOfQuestions() {
        if (isFetching) return

        isFetching = true
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                _questions.clear()
                val originalQuestions = repository.getTriviaQuestionsBatch()

                // 🔁 Traducción usando async + await
                val translatedQuestions = kotlinx.coroutines.coroutineScope {
                    originalQuestions.map { question ->
                        val questionDeferred = async { translateText(question.question) }
                        val correctDeferred = async { translateText(question.correct_answer) }
                        val incorrectDeferreds =
                            question.incorrect_answers.map { async { translateText(it) } }

                        val translatedQuestion = questionDeferred.await()
                        val translatedCorrect = correctDeferred.await()
                        val translatedIncorrect = incorrectDeferreds.map { it.await() }

                        question.copy(
                            question = translatedQuestion,
                            correct_answer = translatedCorrect,
                            incorrect_answers = translatedIncorrect
                        )
                    }
                }

                _questions.addAll(translatedQuestions)
                currentIndex = 0
                _questionsAnswered.value = 0
                _score.value = 0
                _gameOver.value = false
                _currentQuestion.value = _questions[currentIndex]
                _timeLeft.value = 30
                startTimer()
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
                isFetching = false
            }
        }
    }

    fun loadNewQuestion() {
        stopTimer()

        if (_questionsAnswered.value >= 10) {
            _gameOver.value = true
            userId?.let { markAsPlayedToday(it) }
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            delay(500)

            if (currentIndex < _questions.size - 1) {
                currentIndex++
                _questionsAnswered.value += 1
                _currentQuestion.value = _questions[currentIndex]
                _timeLeft.value = 30
                startTimer()
            } else {
                _gameOver.value = true
                userId?.let { markAsPlayedToday(it) }
            }

            _isLoading.value = false
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0) {
                delay(1000)
                _timeLeft.value -= 1
            }
            delay(1500)
            loadNewQuestion()
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
    }

    fun increaseScore() {
        _score.value += 1
    }

    private suspend fun translateText(text: String): String {
        return try {
            val response = TranslateService.api.translate(TranslateRequest(q = text))
            println("🔁 Traducción '$text' ➜ '${response.translatedText}'")
            response.translatedText
        } catch (e: Exception) {
            println("❌ Error al traducir '$text': ${e.localizedMessage}")
            text
        }
    }
}
