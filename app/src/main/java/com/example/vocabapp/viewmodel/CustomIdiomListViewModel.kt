package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.local.entity.CustomIdiomEntity
import com.example.vocabapp.data.repository.CustomContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
/**
 * カスタム英熟語一覧を管理するViewModel。
 */
class CustomIdiomListViewModel @Inject constructor(
    private val repository: CustomContentRepository
) : ViewModel() {
    val idioms: StateFlow<List<CustomIdiomEntity>> = repository.observeCustomIdioms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Int) {
        viewModelScope.launch { repository.deleteCustomIdiom(id) }
    }
}

