package com.example.vocabapp.domain.usecase.word

import com.example.vocabapp.domain.model.Word
import javax.inject.Inject

class SearchWordUseCase @Inject constructor() {
    operator fun invoke(words: List<Word>, query: String): List<Word> =
        if (query.isBlank()) words else words.filter {
            it.english.contains(query, ignoreCase = true) || it.meaning.contains(query, ignoreCase = true)
        }
}
