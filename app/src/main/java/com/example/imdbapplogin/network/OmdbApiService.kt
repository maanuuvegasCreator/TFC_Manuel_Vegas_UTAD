package com.example.imdbapplogin.network

import com.example.imdbapplogin.model.OMDbSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OmdbApiService {
    @GET("/")
    suspend fun searchMovies(
        @Query("s") query: String,
        @Query("apikey") apiKey: String
    ): OMDbSearchResponse
}