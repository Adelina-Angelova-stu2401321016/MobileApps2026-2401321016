package com.project.visprapp.ui.view

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.visprapp.data.local.SessionItem
import com.project.visprapp.databinding.ItemSessionWordBinding
import java.util.Date
import java.util.Locale

class SessionItemAdapter(
    private val onSaveWordNote: (item: SessionItem, newNote: String) -> Unit,
    private val onDeleteWord: (item: SessionItem) -> Unit
) : ListAdapter<SessionItem, SessionItemAdapter.SessionItemViewHolder>(SessionItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionItemViewHolder {
        val binding = ItemSessionWordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SessionItemViewHolder(binding, onSaveWordNote, onDeleteWord)
    }

    override fun onBindViewHolder(holder: SessionItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SessionItemViewHolder(
        private val binding: ItemSessionWordBinding,
        private val onSaveWordNote: (item: SessionItem, newNote: String) -> Unit,
        private val onDeleteWord: (item: SessionItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        fun bind(item: SessionItem) {
            binding.tvWordText.text = item.wordText
            binding.tvWordTime.text = timeFormat.format(Date(item.dtWord))

            if (!binding.etWordNote.hasFocus()) {
                binding.etWordNote.setText(item.notes ?: "")
            }

            binding.btnSaveWordNote.setOnClickListener {
                val noteText = binding.etWordNote.text.toString().trim()
                onSaveWordNote(item, noteText)
            }

            binding.btnClearWordNote.setOnClickListener {
                binding.etWordNote.setText("")
                onSaveWordNote(item, "")
            }

            binding.btnDeleteWord.setOnClickListener {
                onDeleteWord(item)
            }
        }
    }

    class SessionItemDiffCallback : DiffUtil.ItemCallback<SessionItem>() {
        override fun areItemsTheSame(oldItem: SessionItem, newItem: SessionItem): Boolean {
            return oldItem.wordId == newItem.wordId
        }

        override fun areContentsTheSame(oldItem: SessionItem, newItem: SessionItem): Boolean {
            return oldItem == newItem
        }
    }
}
