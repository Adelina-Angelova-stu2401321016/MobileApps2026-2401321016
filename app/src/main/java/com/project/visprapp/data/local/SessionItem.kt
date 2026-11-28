package com.project.visprapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_items")
data class SessionItem(
    @PrimaryKey
    val wordId: String,
    val sessionId: String,
    val dtWord: Long = System.currentTimeMillis(),
    val wordText: String,
    val notes: String? = null
)
