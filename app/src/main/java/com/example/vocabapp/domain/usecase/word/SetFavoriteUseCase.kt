package com.example.vocabapp.domain.usecase.word

import com.example.vocabapp.data.repository.WordRepository
import javax.inject.Inject

class SetFavoriteUseCase @Inject constructor(
    private val repository: WordRepository
) {
    suspend operator fun invoke(wordId: Int, isFavorite: Boolean) = repository.setWordFavorite(wordId, isFavorite)
}
