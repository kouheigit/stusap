package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.repository.SeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class MainViewModel @Inject constructor(
    private val seedRepository: SeedRepository
) : ViewModel() {
    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                seedRepository.seedIfNeeded()
                seedRepository.seedIdiomsIfNeeded()
            }
        }
    }
}
