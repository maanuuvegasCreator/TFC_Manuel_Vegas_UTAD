package com.example.imdbapplogin.model

data class OMDbSearchResponse(
    val Search: List<Movie>?,
    val totalResults: String?,
    val Response: String
)

data class Movie(
    val Title: String,
    val Year: String,
    val Poster: String
)
