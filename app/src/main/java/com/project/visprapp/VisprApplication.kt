package com.project.visprapp

import android.app.Application
import com.project.visprapp.data.repository.UserRepository
import com.project.visprapp.utils.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VisprApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ThemeManager(this).applySavedTheme()

        CoroutineScope(Dispatchers.IO).launch {
            UserRepository(this@VisprApplication).ensureDefaultTestUserExists()
        }
    }
}
