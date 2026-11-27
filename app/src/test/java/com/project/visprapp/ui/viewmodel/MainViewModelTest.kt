package com.project.visprapp.ui.viewmodel

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MainViewModelTest {

    @Test
    fun initialWordLog_isDefault() {
        val app = Application()
        val viewModel = MainViewModel(app)

        val result = viewModel.wordLog.value
        assertNotNull(result)
        assertEquals("Session not started...", result)
    }

    @Test
    fun clearLog_resetsWordLog() {
        val app = Application()
        val viewModel = MainViewModel(app)

        viewModel.clearLog()
        assertEquals("Session not started...", viewModel.wordLog.value)
    }
}
