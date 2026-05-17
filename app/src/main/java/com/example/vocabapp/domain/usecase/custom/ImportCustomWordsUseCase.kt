package com.example.vocabapp.domain.usecase.custom

import com.example.vocabapp.data.repository.CustomContentRepository
import com.example.vocabapp.domain.model.WordImportPreview
import javax.inject.Inject

class ImportCustomWordsUseCase @Inject constructor(
    private val repository: CustomContentRepository
) {
    suspend operator fun invoke(preview: WordImportPreview) = repository.importCustomWords(preview)
}
