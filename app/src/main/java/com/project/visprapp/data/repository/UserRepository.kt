package com.project.visprapp.data.repository

import android.content.Context
import com.project.visprapp.data.local.AppDatabase
import com.project.visprapp.data.local.User
import kotlinx.coroutines.flow.Flow

class UserRepository(context: Context) {

    private val userDao = AppDatabase.getInstance(context).userDao()

    val activeUser: Flow<User?> = userDao.getActiveUser()

    suspend fun ensureDefaultTestUserExists(): User {
        val existingTestUser = userDao.getUserByEmail("test@vispr.com")
        if (existingTestUser != null) {
            return existingTestUser
        }

        val defaultUser = User(
            userId = "testUser",
            googleId = "testUser",
            email = "test@vispr.com",
            dtCreated = System.currentTimeMillis(),
            lastLogin = System.currentTimeMillis(),
            isLoggedIn = true,
            username = "Sadie",
            avatarColor = "#00FFCC"
        )
        userDao.insertUser(defaultUser)
        return defaultUser
    }

    suspend fun loginAsTestUser(): User {
        val testUser = ensureDefaultTestUserExists()
        val activeTestUser = testUser.copy(
            lastLogin = System.currentTimeMillis(),
            isLoggedIn = true
        )
        saveUser(activeTestUser)
        return activeTestUser
    }

    suspend fun saveUser(user: User) {
        userDao.logoutAllUsers()
        userDao.insertUser(user)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email.trim().lowercase())
    }

    suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers()
    }

    suspend fun logout() {
        userDao.logoutAllUsers()
    }
}
