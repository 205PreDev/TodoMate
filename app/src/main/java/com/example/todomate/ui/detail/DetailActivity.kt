package com.example.todomate.ui.detail

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.todomate.R
import com.example.todomate.data.local.TodoEntity
import com.example.todomate.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels()
    private var todoId: Long = -1L

    companion object {
        const val EXTRA_TODO_ID = "extra_todo_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        todoId = intent.getLongExtra(EXTRA_TODO_ID, -1L)

        setupToolbar()
        setupSaveButton()

        if (todoId != -1L) {
            loadTodo()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title =
            if (todoId == -1L) getString(R.string.add_todo)
            else getString(R.string.edit_todo)
    }

    private fun setupSaveButton() {
        binding.buttonSave.setOnClickListener {
            saveTodo()
        }
    }

    private fun loadTodo() {
        viewModel.getTodoById(todoId).observe(this) { todo ->
            todo?.let {
                binding.editTitle.setText(it.title)
                binding.editDescription.setText(it.description)

                when (it.priority) {
                    0 -> binding.chipPriorityLow.isChecked = true
                    1 -> binding.chipPriorityMedium.isChecked = true
                    2 -> binding.chipPriorityHigh.isChecked = true
                }

                when (it.category) {
                    "업무" -> binding.chipCategoryWork.isChecked = true
                    "개인" -> binding.chipCategoryPersonal.isChecked = true
                    "쇼핑" -> binding.chipCategoryShopping.isChecked = true
                    else -> binding.chipCategoryEtc.isChecked = true
                }
            }
        }
    }

    private fun saveTodo() {
        val title = binding.editTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.layoutTitle.error = getString(R.string.error_empty_title)
            return
        }

        val description = binding.editDescription.text.toString().trim()

        val priority = when (binding.chipGroupPriority.checkedChipId) {
            R.id.chipPriorityHigh -> 2
            R.id.chipPriorityMedium -> 1
            else -> 0
        }

        val category = when (binding.chipGroupCategory.checkedChipId) {
            R.id.chipCategoryWork -> "업무"
            R.id.chipCategoryPersonal -> "개인"
            R.id.chipCategoryShopping -> "쇼핑"
            else -> "기타"
        }

        if (todoId == -1L) {
            viewModel.insert(
                TodoEntity(
                    title = title,
                    description = description,
                    priority = priority,
                    category = category
                )
            )
        } else {
            viewModel.update(todoId, title, description, priority, category)
        }

        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
