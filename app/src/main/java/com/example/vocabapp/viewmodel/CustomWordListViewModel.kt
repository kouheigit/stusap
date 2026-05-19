package com.example.vocabapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vocabapp.data.local.entity.CustomWordEntity
import com.example.vocabapp.data.repository.CustomContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
/**
 * ユーザーが登録したカスタム英単語一覧を管理するViewModel。
 */
class CustomWordListViewModel @Inject constructor(
    private val repository: CustomContentRepository
) : ViewModel() {
    val words: StateFlow<List<CustomWordEntity>> = repository.observeCustomWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * 指定したカスタム英単語を削除する。
     *
     * @param id カスタム英単語ID
     */
    fun delete(id: Int) {
        viewModelScope.launch { repository.deleteCustomWord(id) }
    }

    /**
     * 指定したカスタム英単語のお気に入り状態を更新する。
     *
     * @param id カスタム英単語ID
     * @param isFavorite trueの場合はお気に入りにする
     */
    fun setFavorite(id: Int, isFavorite: Boolean) {
        viewModelScope.launch { repository.setCustomWordFavorite(id, isFavorite) }
    }

    /**
     * 指定したカスタム英単語の学習済み状態を更新する。
     *
     * @param id カスタム英単語ID
     * @param isLearned trueの場合は学習済みにする
     */
    fun setLearned(id: Int, isLearned: Boolean) {
        viewModelScope.launch { repository.setCustomWordLearned(id, isLearned) }
    }
}

