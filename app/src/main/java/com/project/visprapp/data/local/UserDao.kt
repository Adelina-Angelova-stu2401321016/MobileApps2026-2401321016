package com.project.visprapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 ORDER BY lastLogin DESC LIMIT 1")
    fun getActiveUser(): Flow<User?>

    @Query("SELECT * FROM users WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)) OR LOWER(TRIM(userId)) = LOWER(TRIM(:email)) OR LOWER(TRIM(googleId)) = LOWER(TRIM(:email)) OR (email != '' AND LOWER(TRIM(email)) LIKE '%' || LOWER(TRIM(:email)) || '%') LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
