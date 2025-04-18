package com.example.boxboxd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.boxboxd.core.inner.RaceRepository

class RacesViewModelFactory(private val repository: RaceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RacesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RacesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}