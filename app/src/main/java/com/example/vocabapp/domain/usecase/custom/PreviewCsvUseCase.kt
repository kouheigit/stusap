package com.example.vocabapp.domain.usecase.custom

import com.example.vocabapp.data.repository.CustomContentRepository
import javax.inject.Inject

class PreviewCsvUseCase @Inject constructor(
    private val repository: CustomContentRepository
) {
    suspend operator fun invoke(csvText: String) = repository.previewCustomWordCsv(csvText)
}
