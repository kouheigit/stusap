package com.example.vocabapp.domain.usecase.word

import com.example.vocabapp.data.repository.WordRepository
import javax.inject.Inject

class GetWordDetailUseCase @Inject constructor(
    private val repository: WordRepository
) {
    operator fun invoke(wordId: Int) = repository.observeWordDetail(wordId)
}
