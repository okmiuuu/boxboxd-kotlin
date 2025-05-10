package com.example.boxboxd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import com.example.boxboxd.model.AppState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    // Initialize StateFlow with restored state or default
    private val _state = MutableStateFlow(
        AppState(
            searchQuery = savedStateHandle.get<String>("searchQuery") ?: "",
            selectedItem = savedStateHandle.get<String>("selectedItem"),
            isLoading = savedStateHandle.get<Boolean>("isLoading") ?: false
        )
    )
    val state: StateFlow<AppState> = _state.asStateFlow()

    // Update state and save to SavedStateHandle
    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        savedStateHandle["searchQuery"] = query
    }

    fun selectItem(item: String?) {
        _state.update { it.copy(selectedItem = item) }
        savedStateHandle["selectedItem"] = item
    }

    fun setLoading(loading: Boolean) {
        _state.update { it.copy(isLoading = loading) }
        savedStateHandle["isLoading"] = loading
    }
}