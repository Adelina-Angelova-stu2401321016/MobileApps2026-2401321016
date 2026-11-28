package com.project.visprapp.ui.viewmodel

import android.app.Application
import android.icu.text.SimpleDateFormat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project.visprapp.data.model.WordBank
import com.project.visprapp.data.repository.CombinedSensRepository
import com.project.visprapp.data.repository.EmfRepository
import com.project.visprapp.data.repository.MainRepository
import com.project.visprapp.data.repository.MainRepositoryImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale

data class CapturedWord(
    val wordText: String,
    val timestamp: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repositoryMain: MainRepository = MainRepositoryImpl()
    private val repositoryEmf = EmfRepository(application)
    private val wordBank: List<String> = loadWordBankFromAssets(application)
    private val sensorRepository = CombinedSensRepository(application, wordBank.size)

    private val _wordLog = MutableStateFlow("Session not started...")
    val wordLog: StateFlow<String> = _wordLog.asStateFlow()

    private val _capturedWords = MutableStateFlow<List<CapturedWord>>(emptyList())
    val capturedWords: StateFlow<List<CapturedWord>> = _capturedWords.asStateFlow()

    private var sessionJob: Job? = null
    private val logBuilder = StringBuilder()
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private fun loadWordBankFromAssets(context: Application): List<String> {
        return WordBank.words
    }

    fun startSession() {
        if (sessionJob?.isActive == true) return

        _capturedWords.value = emptyList()
        logBuilder.clear()
        logBuilder.append("--- Session Started (Words: ${wordBank.size}) ---\n")
        _wordLog.value = logBuilder.toString()

        sessionJob = viewModelScope.launch {
            sensorRepository.getCombinedWordIndexFlow()
                .collect { index ->
                    val word = wordBank.getOrElse(index) { "Unknown" }
                    val now = System.currentTimeMillis()
                    val timeStamp = dateFormat.format(Date(now))

                    _capturedWords.value = _capturedWords.value + CapturedWord(wordText = word, timestamp = now)

                    logBuilder.append("[$timeStamp] $word\n")
                    _wordLog.value = logBuilder.toString()
                }
        }
    }

    fun endSession() {
        sessionJob?.cancel()
        logBuilder.append("\n--- Session Ended ---")
        _wordLog.value = logBuilder.toString()
    }

    fun clearLog() {
        sessionJob?.cancel()
        _capturedWords.value = emptyList()
        logBuilder.clear()
        _wordLog.value = "Session not started..."
    }

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
