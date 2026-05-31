package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.repository.SeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class MainViewModel @Inject constructor(
    private val seedRepositoryProvider: Provider<SeedRepository>
) : ViewModel() {
    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val seedRepository = seedRepositoryProvider.get()
                seedRepository.seedIfNeeded()
                seedRepository.seedIdiomsIfNeeded()
            }
        }
    }
}
