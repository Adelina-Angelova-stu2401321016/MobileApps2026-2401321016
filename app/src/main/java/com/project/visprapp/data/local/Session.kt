package com.project.visprapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey
    val sessionId: String,
    val userId: String?,
    val dtSaved: Long = System.currentTimeMillis(),
    val notes: String? = null
)
