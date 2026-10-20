package com.project.visprapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import com.project.visprapp.data.model.MainData
import com.project.visprapp.data.repository.MainRepository
import com.project.visprapp.data.repository.MainRepositoryImpl
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.AndroidViewModel
import com.project.visprapp.data.repository.EmfRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repositoryMain: MainRepository = MainRepositoryImpl()
    private val repositoryEmf = EmfRepository(application)

    val emfProgress: StateFlow<Float> = repositoryEmf.getEmfFlow()
        .map { magnitude ->
            val minEmf = 30f
            val maxEmf = 150f
            ((magnitude - minEmf) / (maxEmf - minEmf)).coerceIn(0f, 1f)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )


}
