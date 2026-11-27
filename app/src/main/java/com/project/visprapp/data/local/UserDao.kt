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

    @Query("SELECT * FROM users LIMIT 1")
    fun getActiveUser(): Flow<User?>

    @Query("SELECT * FROM users WHERE googleId = :googleId LIMIT 1")
    suspend fun getUserByGoogleId(googleId: String): User?

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}
