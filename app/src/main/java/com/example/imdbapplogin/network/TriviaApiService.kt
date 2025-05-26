package com.example.imdbapplogin.network

import com.example.imdbapplogin.model.TriviaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaApiService {
    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int = 1,
        @Query("category") category: Int = 11, // 🎬 Películas
        @Query("type") type: String = "multiple"
    ): TriviaResponse
}
