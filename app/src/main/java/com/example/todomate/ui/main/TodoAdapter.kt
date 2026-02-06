package com.example.todomate.ui.main

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todomate.R
import com.example.todomate.data.local.TodoEntity
import com.example.todomate.databinding.ItemTodoBinding

class TodoAdapter(
    private val onItemClick: (TodoEntity) -> Unit,
    private val onCheckChanged: (TodoEntity) -> Unit,
    private val onDeleteClick: (TodoEntity) -> Unit
) : ListAdapter<TodoEntity, TodoAdapter.TodoViewHolder>(TodoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemTodoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TodoViewHolder(
        private val binding: ItemTodoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(todo: TodoEntity) {
            binding.apply {
                textTitle.text = todo.title
                textDescription.text = todo.description
                checkboxComplete.isChecked = todo.isCompleted

                // Priority indicator color
                val priorityColor = when (todo.priority) {
                    2 -> R.color.priority_high
                    1 -> R.color.priority_medium
                    else -> R.color.priority_low
                }
                viewPriority.setBackgroundColor(
                    ContextCompat.getColor(root.context, priorityColor)
                )

                // Category chip
                chipCategory.text = todo.category

                // Strike-through for completed items
                if (todo.isCompleted) {
                    textTitle.paintFlags = textTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    textTitle.alpha = 0.5f
                    textDescription.alpha = 0.5f
                } else {
                    textTitle.paintFlags = textTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    textTitle.alpha = 1.0f
                    textDescription.alpha = 1.0f
                }

                // Click listeners
                root.setOnClickListener { onItemClick(todo) }
                checkboxComplete.setOnClickListener { onCheckChanged(todo) }
                buttonDelete.setOnClickListener { onDeleteClick(todo) }
            }
        }
    }

    class TodoDiffCallback : DiffUtil.ItemCallback<TodoEntity>() {
        override fun areItemsTheSame(oldItem: TodoEntity, newItem: TodoEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TodoEntity, newItem: TodoEntity): Boolean {
            return oldItem == newItem
        }
    }
}
