package com.example.imdbapplogin

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TranslateService {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://translate.argosopentech.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: LibreTranslateApi = retrofit.create(LibreTranslateApi::class.java)
}
