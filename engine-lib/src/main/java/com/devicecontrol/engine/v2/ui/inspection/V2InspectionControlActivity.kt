package com.devicecontrol.engine.v2.ui.inspection

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.connection.V2ConnectionRepository
import com.devicecontrol.engine.v2.ui.adapter.V2RecordRowAdapter
import com.devicecontrol.engine.v2.ui.base.V2BaseShellActivity
import com.devicecontrol.engine.v2.ui.dialog.V2ErrorDialog
import com.devicecontrol.engine.v2.ui.dialog.V2LpcDialog
import com.devicecontrol.engine.v2.ui.model.V2ModelEnginePanelBinder
import com.devicecontrol.engine.v2.ui.settings.V2ModeSettingsActivity
import com.devicecontrol.engine.v2.viewmodel.V2EngineViewModelFactory
import com.devicecontrol.engine.v2.viewmodel.V2InspectionControlViewModel
import com.devicecontrol.engine.v2.viewmodel.V2UiOperationMode

/** 检测主控（P7 自动 / P10 手动） */
class V2InspectionControlActivity : V2BaseShellActivity() {

    override val logTag: String = "InspectionControl"

    private val viewModel: V2InspectionControlViewModel by viewModels {
        V2EngineViewModelFactory(application)
    }
    private lateinit var recordAdapter: V2RecordRowAdapter
    private lateinit var contentRoot: View
    private var bladeCounts: List<Int> = emptyList()
    private var suppressLpcSelection = false
    private var suppressModeSelection = false

    override fun contentLayoutId(): Int = R.layout.content_v2_inspection_control

    override fun shellTitleCn(): String = getString(R.string.v2_inspection_title_cn)
    override fun shellTitleEn(): String = getString(R.string.v2_inspection_title_en)

    @DrawableRes
    override fun shellTitleIcon(): Int = R.drawable.ic_v2_inspection_title

    override fun showBackHome(): Boolean = true

    override fun showShellBottomBar(): Boolean = false

    override fun onContentCreated(root: View) {
        contentRoot = root
        val modelId = intent.getLongExtra(EXTRA_MODEL_ID, 0L)
        val modelName = intent.getStringExtra(EXTRA_MODEL_NAME).orEmpty()
        if (modelId <= 0L) {
            Toast.makeText(this, R.string.v2_model_load_failed, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        root.findViewById<TextView>(R.id.tvEngineModelName).text = modelName

        recordAdapter = V2RecordRowAdapter(
            onReturn = { row -> viewModel.playbackRecord(row.taskRecord) },
            onDelete = { row -> viewModel.deleteRecord(row.taskRecord) },
        )
        root.findViewById<RecyclerView>(R.id.rvRecords).apply {
            layoutManager = LinearLayoutManager(this@V2InspectionControlActivity)
            adapter = recordAdapter
        }

        viewModel.initInspection(modelId, modelName)

        viewModel.engineModelName.observe(this) { name ->
            root.findViewById<TextView>(R.id.tvEngineModelName).text = name
            shellBinder.bindTitles(name, getString(R.string.v2_inspection_title_en))
        }
        viewModel.imagePath.observe(this) { path ->
            val photoPanel = root.findViewById<View>(R.id.ivEnginePhoto).parent as View
            V2ModelEnginePanelBinder.bindEngineImage(photoPanel, path)
        }
        viewModel.engineParamsText.observe(this) {
            root.findViewById<TextView>(R.id.tvEngineParams).text = it
        }
        viewModel.statusBarText.observe(this) {
            root.findViewById<TextView>(R.id.tvStatusBar).text = it
        }
        viewModel.records.observe(this) { recordAdapter.submitList(it) }
        viewModel.lpcPositions.observe(this) { positions ->
            bladeCounts = viewModel.configBladeCounts()
            setupLpcSpinner(root, positions)
        }
        viewModel.operationMode.observe(this) { mode ->
            applyOperationModeUi(root, mode)
            val spinner = root.findViewById<Spinner>(R.id.spinnerMode)
            suppressModeSelection = true
            spinner.setSelection(if (mode == V2UiOperationMode.AUTO) 0 else 1, false)
            suppressModeSelection = false
        }
        viewModel.isRunning.observe(this) { running ->
            updateControlEnabled(root, running)
        }
        viewModel.canStart.observe(this) { can ->
            root.findViewById<View>(R.id.btnStart).isEnabled = can
        }
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        setupAutoControlPills(root)
        setupManualControlPills(root)
        setupStartPauseButtons(root)
        setupModeSpinner(root)
        bindControlClicks(root)
        bindEndClicks(root)

        root.findViewById<View>(R.id.btnStart).setOnClickListener { viewModel.onStart() }
        root.findViewById<View>(R.id.btnPause).setOnClickListener { viewModel.onPause() }
        root.findViewById<View>(R.id.btnRecord).setOnClickListener { viewModel.onControlAction("RECORD") }
        root.findViewById<View>(R.id.btnBacklashOnReturn).setOnClickListener {
            viewModel.onControlAction("BACKLASH_ON_RETURN")
        }
    }

    override fun onResume() {
        super.onResume()
        // 从 P8/P11 设置页返回后，重新加载当前模式的 Room 配置
        viewModel.reapplyModeSettings()
    }

    override fun onDestroy() {
        viewModel.destroySession()
        super.onDestroy()
    }

    private fun setupAutoControlPills(root: View) {
        val specs = listOf(
            PillSpec(R.id.btnForward, R.string.v2_forward, R.drawable.ic_v2_ctrl_forward, R.color.v2_primary),
            PillSpec(R.id.btnReverse, R.string.v2_reverse, R.drawable.ic_v2_ctrl_reverse, R.color.v2_action_green),
            PillSpec(R.id.btnAccel, R.string.v2_accel, R.drawable.ic_v2_ctrl_accel, R.color.v2_primary),
            PillSpec(R.id.btnDecel, R.string.v2_decel, R.drawable.ic_v2_ctrl_decel, R.color.v2_primary),
            PillSpec(R.id.btnContinuous, R.string.v2_continuous, R.drawable.ic_v2_ctrl_continuous, R.color.v2_action_green),
            PillSpec(R.id.btnJog, R.string.v2_jog, R.drawable.ic_v2_ctrl_jog, R.color.v2_primary),
            PillSpec(R.id.btnAutoPhoto, R.string.v2_auto_photo, R.drawable.ic_v2_ctrl_camera, R.color.v2_action_green),
            PillSpec(R.id.btnControlSettings, R.string.v2_settings_btn, R.drawable.ic_v2_ctrl_settings, R.color.v2_primary),
            PillSpec(R.id.btnBacklash, R.string.v2_backlash, R.drawable.ic_v2_ctrl_backlash, R.color.v2_primary),
            PillSpec(R.id.btnEnd, R.string.v2_end, R.drawable.ic_v2_ctrl_end, R.color.v2_primary, R.drawable.bg_v2_control_btn_end),
        )
        specs.forEach { bindPill(root, it) }
    }

    private fun setupManualControlPills(root: View) {
        val specs = listOf(
            PillSpec(R.id.btnForwardManual, R.string.v2_forward, R.drawable.ic_v2_ctrl_forward, R.color.v2_primary),
            PillSpec(R.id.btnReverseManual, R.string.v2_reverse, R.drawable.ic_v2_ctrl_reverse, R.color.v2_action_green),
            PillSpec(R.id.btnAccelManual, R.string.v2_accel, R.drawable.ic_v2_ctrl_accel, R.color.v2_primary),
            PillSpec(R.id.btnDecelManual, R.string.v2_decel, R.drawable.ic_v2_ctrl_decel, R.color.v2_primary),
            PillSpec(R.id.btnContinuousManual, R.string.v2_continuous, R.drawable.ic_v2_ctrl_continuous, R.color.v2_action_green),
            PillSpec(R.id.btnJogManual, R.string.v2_jog, R.drawable.ic_v2_ctrl_jog, R.color.v2_primary),
            PillSpec(
                R.id.btnManualModeSettings,
                R.string.v2_manual_mode_settings,
                R.drawable.ic_v2_ctrl_settings,
                R.color.v2_primary,
            ),
            PillSpec(R.id.btnBacklashManual, R.string.v2_backlash, R.drawable.ic_v2_ctrl_backlash, R.color.v2_primary),
            PillSpec(
                R.id.btnEndManual,
                R.string.v2_end,
                R.drawable.ic_v2_ctrl_end,
                R.color.v2_primary,
                R.drawable.bg_v2_control_btn_end,
            ),
        )
        specs.forEach { bindPill(root, it) }
    }

    private fun setupStartPauseButtons(root: View) {
        val pad = (resources.displayMetrics.density * 10).toInt()
        val start = root.findViewById<Button>(R.id.btnStart)
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
            start, whiteIconDrawable(R.drawable.ic_v2_start_badge), null, null, null,
        )
        start.compoundDrawablePadding = pad
        val pause = root.findViewById<Button>(R.id.btnPause)
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
            pause, whiteIconDrawable(R.drawable.ic_v2_pause_badge), null, null, null,
        )
        pause.compoundDrawablePadding = pad
    }

    private fun whiteIconDrawable(@DrawableRes resId: Int): Drawable? {
        val base = ContextCompat.getDrawable(this, resId) ?: return null
        val wrapped = DrawableCompat.wrap(base.mutate())
        DrawableCompat.setTint(wrapped, ContextCompat.getColor(this, R.color.v2_on_primary))
        return wrapped
    }

    private fun bindPill(root: View, spec: PillSpec) {
        val pill = root.findViewById<View>(spec.viewId)
        pill.findViewById<TextView>(R.id.tvControlLabel).setText(spec.labelRes)
        pill.findViewById<ImageView>(R.id.ivControlIcon).apply {
            setImageResource(spec.iconRes)
            ImageViewCompat.setImageTintList(
                this,
                ColorStateList.valueOf(ContextCompat.getColor(this@V2InspectionControlActivity, R.color.v2_on_primary)),
            )
        }
        val bg = spec.backgroundRes ?: if (spec.colorRes == R.color.v2_action_green) {
            R.drawable.bg_v2_control_btn_green
        } else {
            R.drawable.bg_v2_control_btn_orange
        }
        pill.background = ContextCompat.getDrawable(this, bg)
    }

    private fun setupModeSpinner(root: View) {
        val spinner = root.findViewById<Spinner>(R.id.spinnerMode)
        val labels = listOf(getString(R.string.v2_auto_mode), getString(R.string.v2_manual_mode))
        spinner.adapter = buildSpinnerAdapter(labels)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (suppressModeSelection) return
                val mode = if (position == 0) V2UiOperationMode.AUTO else V2UiOperationMode.MANUAL
                viewModel.setOperationMode(mode)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setupLpcSpinner(root: View, positions: List<String>) {
        val spinner = root.findViewById<Spinner>(R.id.spinnerLpc)
        if (positions.isEmpty()) {
            spinner.adapter = buildSpinnerAdapter(emptyList())
            return
        }
        suppressLpcSelection = true
        spinner.adapter = buildSpinnerAdapter(positions)
        val index = viewModel.currentLpcIndex.value ?: 0
        spinner.setSelection(index.coerceIn(0, positions.lastIndex), false)
        suppressLpcSelection = false
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (suppressLpcSelection) return
                viewModel.setLpcIndex(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        spinner.setOnLongClickListener {
            val selected = viewModel.currentLpcIndex.value ?: 0
            V2LpcDialog.show(this, positions, bladeCounts, selected) { index ->
                suppressLpcSelection = true
                viewModel.setLpcIndex(index)
                spinner.setSelection(index)
                suppressLpcSelection = false
            }
            true
        }
    }

    private fun applyOperationModeUi(root: View, mode: V2UiOperationMode) {
        val auto = mode == V2UiOperationMode.AUTO
        root.findViewById<View>(R.id.panelAutoOps).visibility = if (auto) View.VISIBLE else View.GONE
        root.findViewById<View>(R.id.panelManualOps).visibility = if (auto) View.GONE else View.VISIBLE
        root.findViewById<View>(R.id.scrollOperation).scrollTo(0, 0)
    }

    private fun updateControlEnabled(root: View, running: Boolean) {
        val autoMode = viewModel.operationMode.value == V2UiOperationMode.AUTO
        val disableMotion = running && autoMode
        val autoIds = listOf(
            R.id.btnForward, R.id.btnReverse, R.id.btnAccel, R.id.btnDecel,
            R.id.btnContinuous, R.id.btnJog, R.id.btnBacklash,
        )
        val manualIds = listOf(
            R.id.btnForwardManual, R.id.btnReverseManual, R.id.btnAccelManual, R.id.btnDecelManual,
            R.id.btnContinuousManual, R.id.btnJogManual, R.id.btnBacklashManual,
        )
        (autoIds + manualIds).forEach { id ->
            root.findViewById<View>(id).isEnabled = !disableMotion
        }
    }

    private fun buildSpinnerAdapter(items: List<String>): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(
            this,
            R.layout.spinner_item_v2,
            android.R.id.text1,
            items,
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                bindSpinnerTextView(view, items[position])
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                bindSpinnerTextView(view, items[position])
                return view
            }
        }.apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item_v2)
        }
    }

    private fun bindSpinnerTextView(view: View, text: String) {
        val tv = view.findViewById<TextView>(android.R.id.text1) ?: (view as? TextView) ?: return
        tv.text = text
    }

    private fun bindControlClicks(root: View) {
        mapOf(
            R.id.btnForward to "FORWARD",
            R.id.btnReverse to "REVERSE",
            R.id.btnAccel to "ACCEL",
            R.id.btnDecel to "DECEL",
            R.id.btnContinuous to "CONTINUOUS",
            R.id.btnJog to "JOG",
            R.id.btnAutoPhoto to "AUTO_PHOTO",
            R.id.btnBacklash to "BACKLASH",
            R.id.btnForwardManual to "FORWARD",
            R.id.btnReverseManual to "REVERSE",
            R.id.btnAccelManual to "ACCEL",
            R.id.btnDecelManual to "DECEL",
            R.id.btnContinuousManual to "CONTINUOUS",
            R.id.btnJogManual to "JOG",
            R.id.btnBacklashManual to "BACKLASH",
        ).forEach { (id, action) ->
            root.findViewById<View>(id).setOnClickListener {
                if (!ensureConnectedForControl()) return@setOnClickListener
                viewModel.onControlAction(action)
            }
        }
        root.findViewById<View>(R.id.btnControlSettings).setOnClickListener {
            openModeSettings(false)
        }
        root.findViewById<View>(R.id.btnManualModeSettings).setOnClickListener {
            openModeSettings(true)
        }
    }

    private fun bindEndClicks(root: View) {
        val endListener = View.OnClickListener {
            viewModel.onEnd()
            finish()
        }
        root.findViewById<View>(R.id.btnEnd).setOnClickListener(endListener)
        root.findViewById<View>(R.id.btnEndManual).setOnClickListener(endListener)
    }

    private fun openModeSettings(manual: Boolean) {
        val modelId = intent.getLongExtra(EXTRA_MODEL_ID, 0L)
        startActivity(
            android.content.Intent(this, V2ModeSettingsActivity::class.java).apply {
                putExtra(V2ModeSettingsActivity.EXTRA_MANUAL, manual)
                putExtra(V2ModeSettingsActivity.EXTRA_MODEL_ID, modelId)
            },
        )
    }

    private fun ensureConnectedForControl(): Boolean {
        if (V2ConnectionRepository.isConnected()) return true
        V2ErrorDialog.show(
            context = this,
            message = getString(R.string.v2_error_sample),
            onClose = null,
            onBackHome = { navigateToHome() },
        )
        return false
    }

    private data class PillSpec(
        val viewId: Int,
        @StringRes val labelRes: Int,
        @DrawableRes val iconRes: Int,
        @ColorRes val colorRes: Int,
        @DrawableRes val backgroundRes: Int? = null,
    )

    companion object {
        const val EXTRA_MODEL_ID = "extra_v2_inspection_model_id"
        const val EXTRA_MODEL_NAME = "extra_v2_model_name"
    }
}
