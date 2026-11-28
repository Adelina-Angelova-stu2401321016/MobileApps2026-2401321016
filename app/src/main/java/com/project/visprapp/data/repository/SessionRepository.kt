package com.project.visprapp.data.repository

import android.content.Context
import com.project.visprapp.data.local.AppDatabase
import com.project.visprapp.data.local.Session
import com.project.visprapp.data.local.SessionItem
import com.project.visprapp.data.local.SessionWithItems
import kotlinx.coroutines.flow.Flow

class SessionRepository(context: Context) {

    private val sessionDao = AppDatabase.getInstance(context).sessionDao()

    val allSessions: Flow<List<Session>> = sessionDao.getAllSessions()
    val allSessionsWithItems: Flow<List<SessionWithItems>> = sessionDao.getAllSessionsWithItems()

    fun getSessionsForUser(userId: String): Flow<List<Session>> = sessionDao.getSessionsForUser(userId)

    fun getSessionsWithItemsForUser(userId: String): Flow<List<SessionWithItems>> =
        sessionDao.getSessionsWithItemsForUser(userId)

    fun getSessionWithItems(sessionId: String): Flow<SessionWithItems?> =
        sessionDao.getSessionWithItems(sessionId)

    fun getItemsForSession(sessionId: String): Flow<List<SessionItem>> =
        sessionDao.getItemsForSession(sessionId)

    suspend fun saveSession(session: Session) {
        sessionDao.insertSession(session)
    }

    suspend fun saveSessionItem(item: SessionItem) {
        sessionDao.insertSessionItem(item)
    }

    suspend fun saveSessionItems(items: List<SessionItem>) {
        sessionDao.insertSessionItems(items)
    }

    suspend fun updateSessionNotes(sessionId: String, notes: String?) {
        sessionDao.updateSessionNotes(sessionId, notes)
    }

    suspend fun updateSessionItemNotes(wordId: String, notes: String?) {
        sessionDao.updateSessionItemNotes(wordId, notes)
    }

    suspend fun deleteSession(session: Session) {
        sessionDao.deleteItemsForSession(session.sessionId)
        sessionDao.deleteSession(session)
    }

    suspend fun deleteSessionItem(item: SessionItem) {
        sessionDao.deleteSessionItem(item)
    }
}
