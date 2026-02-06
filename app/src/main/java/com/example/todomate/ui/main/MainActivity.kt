package com.example.todomate.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todomate.R
import com.example.todomate.data.local.TodoEntity
import com.example.todomate.databinding.ActivityMainBinding
import com.example.todomate.ui.dashboard.DashboardActivity
import com.example.todomate.ui.detail.DetailActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var todoAdapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupRecyclerView()
        setupEncouragement()
        observeData()
        setupFab()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(
            onItemClick = { todo -> navigateToDetail(todo.id) },
            onCheckChanged = { todo -> viewModel.toggleComplete(todo) },
            onDeleteClick = { todo -> showDeleteConfirmation(todo) }
        )
        binding.recyclerView.apply {
            adapter = todoAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun observeData() {
        viewModel.todos.observe(this) { todos ->
            todoAdapter.submitList(todos)
            binding.textEmpty.isVisible = todos.isEmpty()
            binding.recyclerView.isVisible = todos.isNotEmpty()

            // 할 일 목록이 로드되면 격려 메시지 요청 (최초 1회)
            if (viewModel.encouragementMessage.value == null) {
                viewModel.requestEncouragement()
            }
        }

        viewModel.encouragementMessage.observe(this) { state ->
            when (state) {
                is MainViewModel.AiMessageState.Loading -> {
                    binding.textEncouragement.text = getString(R.string.loading_message)
                    binding.btnRefreshMessage.isEnabled = false
                }
                is MainViewModel.AiMessageState.Success -> {
                    binding.textEncouragement.text = state.message
                    binding.btnRefreshMessage.isEnabled = true
                }
                is MainViewModel.AiMessageState.Error -> {
                    binding.textEncouragement.text = state.message
                    binding.btnRefreshMessage.isEnabled = true
                }
            }
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            navigateToDetail(-1L)
        }
    }

    private fun setupEncouragement() {
        binding.btnRefreshMessage.setOnClickListener {
            viewModel.requestEncouragement()
        }
    }

    private fun navigateToDetail(todoId: Long) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_TODO_ID, todoId)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmation(todo: TodoEntity) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_title)
            .setMessage(R.string.delete_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteTodo(todo)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            R.id.action_dashboard -> {
                startActivity(Intent(this, DashboardActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSortDialog() {
        val options = arrayOf(
            getString(R.string.sort_date_desc),
            getString(R.string.sort_date_asc),
            getString(R.string.sort_priority)
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sort)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.setSortType(MainViewModel.SortType.DATE_DESC)
                    1 -> viewModel.setSortType(MainViewModel.SortType.DATE_ASC)
                    2 -> viewModel.setSortType(MainViewModel.SortType.PRIORITY)
                }
            }
            .show()
    }
}
