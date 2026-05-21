package com.example.vocabapp.domain.usecase.custom

import com.example.vocabapp.data.repository.CustomImportRepository
import javax.inject.Inject

class PreviewCsvUseCase @Inject constructor(
    private val repository: CustomImportRepository
) {
    suspend operator fun invoke(csvText: String) = repository.previewCustomWordCsv(csvText)
}
