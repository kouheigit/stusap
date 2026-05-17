package com.example.vocabapp.domain.usecase.word

import com.example.vocabapp.data.repository.WordRepository
import javax.inject.Inject

class SetLearnedUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(wordId: Int, isLearned: Boolean) = repository.setWordLearned(wordId, isLearned)
}
