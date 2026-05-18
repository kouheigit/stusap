package com.example.vocabapp.data.repository

import javax.inject.Inject

class QuizRuntime @Inject constructor() {
    fun nowMillis(): Long = System.currentTimeMillis()

    fun <T> shuffled(items: List<T>): List<T> = items.shuffled()

    fun randomInt(from: Int, to: Int): Int = (from..to).random()
}
