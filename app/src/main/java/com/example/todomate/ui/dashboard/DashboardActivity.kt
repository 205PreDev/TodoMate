package com.example.todomate.ui.dashboard

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.todomate.R
import com.example.todomate.data.local.LifeAreaEntity
import com.example.todomate.databinding.ActivityDashboardBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONArray
import org.json.JSONObject

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    private var isWebViewReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setup3DChart()
        setupWeekNavigation()
        setupButtons()
        observeData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.dashboard_title)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setup3DChart() {
        binding.webViewChart.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true

            // 하드웨어 가속
            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

            // 투명 배경
            setBackgroundColor(0x00000000)

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    isWebViewReady = true
                    // 테마 적용
                    val isDark = (resources.configuration.uiMode and
                            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                    view?.evaluateJavascript("setTheme($isDark)", null)
                    // 데이터 업데이트
                    viewModel.radarChartData.value?.let { updateChart(it) }
                }
            }

            addJavascriptInterface(ChartInterface(), "Android")
            loadUrl("file:///android_asset/radar3d.html")
        }
    }

    private fun setupWeekNavigation() {
        binding.btnPreviousWeek.setOnClickListener {
            viewModel.navigateToPreviousWeek()
        }

        binding.btnNextWeek.setOnClickListener {
            viewModel.navigateToNextWeek()
        }
    }

    private fun setupButtons() {
        binding.btnManageAreas.setOnClickListener {
            showManageAreasDialog()
        }
    }

    private fun observeData() {
        viewModel.currentWeekStartDate.observe(this) {
            binding.textWeekRange.text = viewModel.getWeekRangeText()
            binding.btnNextWeek.isEnabled = !viewModel.isCurrentWeek()
        }

        viewModel.radarChartData.observe(this) { data ->
            updateChart(data)
        }

        viewModel.lifeAreas.observe(this) { areas ->
            updateAreasList(areas)
        }
    }

    private fun updateChart(data: DashboardViewModel.RadarChartData) {
        if (!isWebViewReady) return

        if (data.labels.isEmpty()) {
            binding.webViewChart.isVisible = false
            binding.textNoData.isVisible = true
            return
        }

        binding.webViewChart.isVisible = true
        binding.textNoData.isVisible = false

        // JSON 데이터 생성
        val jsonObject = JSONObject().apply {
            put("labels", JSONArray(data.labels))
            put("goalValues", JSONArray(data.goalValues.map { it.toDouble() }))
            put("actualValues", JSONArray(data.actualValues.map { it.toDouble() }))
        }

        val jsonString = jsonObject.toString().replace("'", "\\'")
        binding.webViewChart.evaluateJavascript("updateChart('$jsonString')", null)
    }

    private fun updateAreasList(areas: List<LifeAreaEntity>) {
        val areaNames = areas.joinToString(" | ") { it.name }
        binding.textAreasList.text = areaNames
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_set_goals -> {
                showGoalSettingDialog()
                true
            }
            R.id.action_reset_areas -> {
                showResetAreasConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showGoalSettingDialog() {
        val areas = viewModel.lifeAreas.value ?: return
        if (areas.isEmpty()) {
            MaterialAlertDialogBuilder(this)
                .setMessage("생활 영역이 없습니다. 먼저 영역을 추가해주세요.")
                .setPositiveButton(R.string.cancel, null)
                .show()
            return
        }

        val currentGoals = viewModel.weeklyGoals.value ?: emptyList()

        val dialogView = layoutInflater.inflate(R.layout.dialog_goal_setting, null)
        val container = dialogView.findViewById<android.widget.LinearLayout>(R.id.goalContainer)

        val seekBars = mutableMapOf<Long, SeekBar>()

        areas.forEach { area ->
            val itemView = layoutInflater.inflate(R.layout.item_goal_slider, container, false)
            val textName = itemView.findViewById<TextView>(R.id.textAreaName)
            val textValue = itemView.findViewById<TextView>(R.id.textGoalValue)
            val seekBar = itemView.findViewById<SeekBar>(R.id.seekBarGoal)

            textName.text = area.name
            val currentValue = currentGoals.find { it.lifeAreaId == area.id }?.targetPercentage ?: 20
            seekBar.progress = currentValue
            textValue.text = "$currentValue%"

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    textValue.text = "$progress%"
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })

            seekBars[area.id] = seekBar
            container.addView(itemView)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.set_weekly_goals)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val goals = seekBars.mapValues { it.value.progress }
                viewModel.saveWeeklyGoals(goals)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showManageAreasDialog() {
        val areas = viewModel.lifeAreas.value ?: return

        val items = areas.map { it.name }.toMutableList()
        items.add("+ ${getString(R.string.add_life_area)}")

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.manage_areas)
            .setItems(items.toTypedArray()) { _, which ->
                if (which == items.size - 1) {
                    // 추가
                    showAddAreaDialog()
                } else {
                    // 삭제 확인
                    showDeleteAreaConfirmation(areas[which])
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showAddAreaDialog() {
        val editText = EditText(this).apply {
            hint = getString(R.string.area_name_hint)
            setPadding(48, 32, 48, 32)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_life_area)
            .setView(editText)
            .setPositiveButton(R.string.add) { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.addCustomLifeArea(name)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteAreaConfirmation(area: LifeAreaEntity) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_area_title)
            .setMessage(getString(R.string.delete_area_message, area.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteLifeArea(area)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showResetAreasConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.reset_areas_title)
            .setMessage(R.string.reset_areas_message)
            .setPositiveButton(R.string.reset) { _, _ ->
                viewModel.resetLifeAreas()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // JavaScript Interface for WebView communication
    inner class ChartInterface {
        @JavascriptInterface
        fun onChartReady() {
            runOnUiThread {
                isWebViewReady = true
                viewModel.radarChartData.value?.let { updateChart(it) }
            }
        }
    }
}
