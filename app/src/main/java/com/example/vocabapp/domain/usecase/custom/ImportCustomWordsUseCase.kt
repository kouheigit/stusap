package com.example.vocabapp.domain.usecase.custom

import com.example.vocabapp.data.repository.CustomImportRepository
import com.example.vocabapp.domain.model.WordImportPreview
import javax.inject.Inject

class ImportCustomWordsUseCase @Inject constructor(
    private val repository: CustomImportRepository
) {
    suspend operator fun invoke(preview: WordImportPreview) = repository.importCustomWords(preview)
}
