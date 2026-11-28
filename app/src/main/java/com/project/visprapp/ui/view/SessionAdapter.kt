package com.project.visprapp.ui.view

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.project.visprapp.R
import com.project.visprapp.data.local.Session
import java.util.Date
import java.util.Locale

class SessionAdapter(
    private val sessions: List<Session>,
    private val onViewClick: ((Session) -> Unit)? = null,
    private val onDeleteClick: (Session) -> Unit
) : RecyclerView.Adapter<SessionAdapter.SessionViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    class SessionViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val sessionTime: TextView =
            view.findViewById(R.id.sessionTime)

        val viewButton: ImageButton =
            view.findViewById(R.id.viewSessionButton)

        val deleteButton: ImageButton =
            view.findViewById(R.id.deleteSessionButton)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SessionViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_session, parent, false)

        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SessionViewHolder,
        position: Int
    ) {
        val session = sessions[position]
        val formattedDate = dateFormat.format(Date(session.dtSaved))
        holder.sessionTime.text = "Session from: $formattedDate"

        holder.viewButton.setOnClickListener {
            onViewClick?.invoke(session)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(session)
        }
    }

    override fun getItemCount(): Int {
        return sessions.size
    }
}
