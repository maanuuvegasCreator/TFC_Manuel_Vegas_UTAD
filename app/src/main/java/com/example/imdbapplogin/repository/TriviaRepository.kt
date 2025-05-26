package com.example.imdbapplogin.repository

import com.example.imdbapplogin.model.TriviaQuestion
import com.example.imdbapplogin.network.RetrofitTriviaInstance

class TriviaRepository {
    suspend fun getTriviaQuestionsBatch(): List<TriviaQuestion> {
        val response = RetrofitTriviaInstance.api.getQuestions(amount = 10, category = 11, type = "multiple")
        return response.results
    }

}
