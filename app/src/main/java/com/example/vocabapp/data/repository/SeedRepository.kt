package com.example.vocabapp.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedRepository @Inject constructor(
    private val vocabRepository: VocabRepository
) {
    suspend fun seedIfNeeded() = vocabRepository.seedIfNeeded()
    suspend fun seedIdiomsIfNeeded() = vocabRepository.seedIdiomsIfNeeded()
}
