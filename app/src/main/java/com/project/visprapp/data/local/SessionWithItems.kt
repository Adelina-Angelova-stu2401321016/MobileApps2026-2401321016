package com.project.visprapp.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class SessionWithItems(
    @Embedded val session: Session,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val items: List<SessionItem>
)
