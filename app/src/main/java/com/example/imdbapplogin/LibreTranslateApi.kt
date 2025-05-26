package com.example.imdbapplogin

import com.example.imdbapplogin.model.TranslateRequest
import com.example.imdbapplogin.model.TranslateResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LibreTranslateApi {
    @POST("translate")
    suspend fun translate(@Body request: TranslateRequest): TranslateResponse
}
