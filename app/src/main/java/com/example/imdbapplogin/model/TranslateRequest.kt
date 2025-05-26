package com.example.imdbapplogin.model

// Traducción de entrada
data class TranslateRequest(
    val q: String,
    val source: String = "en",
    val target: String = "es",
    val format: String = "text"
)

// Traducción de salida
data class TranslateResponse(
    val translatedText: String
)
