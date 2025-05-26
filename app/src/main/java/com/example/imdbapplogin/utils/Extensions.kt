package com.example.imdbapplogin.utils

import com.example.imdbapplogin.model.TriviaQuestion

fun TriviaQuestion.shuffledAnswers(): List<String> {
    return (incorrect_answers + correct_answer).shuffled()
}
