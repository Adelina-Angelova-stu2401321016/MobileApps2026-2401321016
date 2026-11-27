package com.project.visprapp.data.repository

import android.content.Context
import com.project.visprapp.data.local.AppDatabase
import com.project.visprapp.data.local.User
import kotlinx.coroutines.flow.Flow

class UserRepository(context: Context) {

    private val userDao = AppDatabase.getInstance(context).userDao()

    val activeUser: Flow<User?> = userDao.getActiveUser()

    suspend fun saveUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun getUserByGoogleId(googleId: String): User? {
        return userDao.getUserByGoogleId(googleId)
    }

    suspend fun logout() {
        userDao.deleteAllUsers()
    }
}
