package com.project.visprapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val userId: String,
    val googleId: String,
    val email: String,
    val dtCreated: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis(),
    val isLoggedIn: Boolean = true,
    val username: String,
    val avatarColor: String
)
