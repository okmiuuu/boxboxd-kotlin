package com.example.boxboxd.model

data class AppState(
    val searchQuery: String = "",
    val selectedItem: String? = null,
    val isLoading: Boolean = false
)