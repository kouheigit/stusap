package com.example.vocabapp.data.repository

import com.example.vocabapp.data.local.dao.AppDao
import com.example.vocabapp.data.seed.IdiomSeedData
import com.example.vocabapp.data.seed.SeedData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedRepository @Inject constructor(
    private val dao: AppDao
) {
    suspend fun seedIfNeeded() {
        dao.seedIfNeeded(
            SeedData.lessons,
            SeedData.trainings,
            SeedData.words,
            SeedData.choices,
            SeedData.relations
        )
    }

    suspend fun seedIdiomsIfNeeded() {
        dao.seedIdiomsIfNeeded(
            IdiomSeedData.lessons,
            IdiomSeedData.trainings,
            IdiomSeedData.words,
            IdiomSeedData.choices,
            IdiomSeedData.relations
        )
    }
}
