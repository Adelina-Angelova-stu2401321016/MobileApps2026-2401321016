package com.project.visprapp.data.repository

import com.project.visprapp.data.model.MainData

/**
 * Interface defining data operations for the repository.
 */
interface MainRepository {
    fun getMainData(): MainData
}

/**
 * Default implementation of [MainRepository].
 */
class MainRepositoryImpl : MainRepository {
    override fun getMainData(): MainData {
        return MainData(
            title = "Welcome to VisprApp",
            description = "App successfully structured using MVVM (Model-View-ViewModel) architecture with Kotlin and Gradle.",
        )
    }
}
