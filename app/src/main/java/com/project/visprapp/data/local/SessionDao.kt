package com.project.visprapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session)

    @Update
    suspend fun updateSession(session: Session)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionItem(item: SessionItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionItems(items: List<SessionItem>)

    @Query("SELECT * FROM sessions ORDER BY dtSaved DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY dtSaved DESC")
    fun getSessionsForUser(userId: String): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): Session?

    @Query("SELECT * FROM session_items WHERE sessionId = :sessionId ORDER BY dtWord ASC")
    fun getItemsForSession(sessionId: String): Flow<List<SessionItem>>

    @Transaction
    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId LIMIT 1")
    fun getSessionWithItems(sessionId: String): Flow<SessionWithItems?>

    @Transaction
    @Query("SELECT * FROM sessions ORDER BY dtSaved DESC")
    fun getAllSessionsWithItems(): Flow<List<SessionWithItems>>

    @Transaction
    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY dtSaved DESC")
    fun getSessionsWithItemsForUser(userId: String): Flow<List<SessionWithItems>>

    @Query("UPDATE sessions SET notes = :notes WHERE sessionId = :sessionId")
    suspend fun updateSessionNotes(sessionId: String, notes: String?)

    @Query("UPDATE session_items SET notes = :notes WHERE wordId = :wordId")
    suspend fun updateSessionItemNotes(wordId: String, notes: String?)

    @Delete
    suspend fun deleteSession(session: Session)

    @Delete
    suspend fun deleteSessionItem(item: SessionItem)

    @Query("DELETE FROM session_items WHERE wordId = :wordId")
    suspend fun deleteSessionItemById(wordId: String)

    @Query("DELETE FROM session_items WHERE sessionId = :sessionId")
    suspend fun deleteItemsForSession(sessionId: String)
}
