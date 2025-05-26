package com.example.imdbapplogin.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitTriviaInstance {
    val api: TriviaApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TriviaApiService::class.java)
    }
}
