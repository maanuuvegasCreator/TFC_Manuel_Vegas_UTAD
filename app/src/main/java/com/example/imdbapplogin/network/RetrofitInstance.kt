package com.example.imdbapplogin.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // Cliente con logs para OMDb
    private val omdbClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    // Retrofit para OMDb
    val omdbApi: OmdbApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.omdbapi.com/")
            .client(omdbClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OmdbApiService::class.java)
    }
}
