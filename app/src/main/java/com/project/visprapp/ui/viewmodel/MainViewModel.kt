package com.project.visprapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.project.visprapp.data.model.MainData
import com.project.visprapp.data.repository.MainRepository
import com.project.visprapp.data.repository.MainRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel class that manages UI-related data using StateFlow.
 */
class MainViewModel(
    private val repository: MainRepository = MainRepositoryImpl(),
) : ViewModel() {

    private val _mainData = MutableStateFlow<MainData?>(null)
    val mainData: StateFlow<MainData?> = _mainData.asStateFlow()

    init {
        loadMainData()
    }

    fun loadMainData() {
        _mainData.value = repository.getMainData()
    }
}
