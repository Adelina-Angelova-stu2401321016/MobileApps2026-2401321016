package com.project.visprapp.ui.viewmodel

import com.project.visprapp.data.model.MainData
import com.project.visprapp.data.repository.MainRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MainViewModelTest {

    private class FakeRepository : MainRepository {
        override fun getMainData(): MainData {
            return MainData(
                title = "Test Title",
                description = "Test Description",
            )
        }
    }

    @Test
    fun loadMainData_updatesStateFlow() {
        val fakeRepository = FakeRepository()
        val viewModel = MainViewModel(fakeRepository)

        val result = viewModel.mainData.value
        assertNotNull(result)
        assertEquals("Test Title", result?.title)
        assertEquals("Test Description", result?.description)
    }
}
