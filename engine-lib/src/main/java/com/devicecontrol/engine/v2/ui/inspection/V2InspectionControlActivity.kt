package com.devicecontrol.engine.v2.ui.inspection

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import com.devicecontrol.engine.v2.ui.settings.V2ModeSettingsActivity
import com.devicecontrol.engine.v2.viewmodel.V2ControlHighlightState
import com.devicecontrol.engine.v2.viewmodel.V2EngineViewModelFactory
import com.devicecontrol.engine.v2.viewmodel.V2InspectionControlViewModel
import com.devicecontrol.engine.v2.viewmodel.V2UiOperationMode
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

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
    private var lastHighlight = V2ControlHighlightState()
    private lateinit var modeSelector: V2InspectionModeSelector
    private lateinit var lpcSelector: V2InspectionLpcSelector

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
        val enginePanel = root.findViewById<View>(R.id.enginePanelLeft)

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
            com.devicecontrol.engine.v2.ui.model.V2ModelEnginePanelBinder.bindEngineImage(enginePanel, path)
        }
        viewModel.engineParamsText.observe(this) {
            root.findViewById<TextView>(R.id.tvEngineParams).text = it
        }
        viewModel.statusBarText.observe(this) {
            root.findViewById<TextView>(R.id.tvStatusBar).text = it
        }
        viewModel.records.observe(this) { recordAdapter.submitList(it) }
        viewModel.lpcPositions.observe(this) { positions ->
            if (positions.isNullOrEmpty()) return@observe
            bladeCounts = viewModel.configBladeCounts()
            val index = viewModel.currentLpcIndex.value ?: 0
            if (::lpcSelector.isInitialized) {
                lpcSelector.updateData(positions, bladeCounts, index)
            } else {
                setupLpcSelector(root, positions, index)
            }
        }
        viewModel.currentLpcIndex.observe(this) { index ->
            if (!::lpcSelector.isInitialized) return@observe
            suppressLpcSelection = true
            lpcSelector.setIndexSilently(index)
            suppressLpcSelection = false
        }
        viewModel.operationMode.observe(this) { mode ->
            applyOperationModeUi(root, mode)
            suppressModeSelection = true
            modeSelector.setModeSilently(mode)
            suppressModeSelection = false
            applyControlHighlights(root, lastHighlight, mode == V2UiOperationMode.MANUAL)
            updateControlEnabled(root, viewModel.isRunning.value == true)
        }
        viewModel.controlHighlight.observe(this) { state ->
            lastHighlight = state
            val manual = viewModel.operationMode.value == V2UiOperationMode.MANUAL
            applyControlHighlights(root, state, manual)
        }
        viewModel.isRunning.observe(this) { running ->
            updateControlEnabled(root, running)
        }
        viewModel.canStart.observe(this) { can ->
            root.findViewById<View>(R.id.btnStart).apply {
                isEnabled = can
                alpha = if (can) 1f else DISABLED_ALPHA
            }
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
        updateControlEnabled(root, viewModel.isRunning.value == true)

        root.findViewById<View>(R.id.btnStart).setOnClickListener { viewModel.onStart() }
        root.findViewById<View>(R.id.btnPause).setOnClickListener { viewModel.onPause() }
        root.findViewById<View>(R.id.btnRecord).setOnClickListener { viewModel.onControlAction("RECORD") }
        root.findViewById<View>(R.id.btnBacklashOnReturn).setOnClickListener {
            viewModel.onControlAction("BACKLASH_ON_RETURN")
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.reapplyModeSettings()
    }

    override fun onDestroy() {
        if (::modeSelector.isInitialized) modeSelector.dismiss()
        if (::lpcSelector.isInitialized) lpcSelector.dismiss()
        viewModel.destroySession()
        super.onDestroy()
    }

    private fun setupAutoControlPills(root: View) {
        val specs = listOf(
            PillSpec(R.id.btnForward, R.string.v2_forward, R.drawable.ic_v2_ctrl_forward),
            PillSpec(R.id.btnReverse, R.string.v2_reverse, R.drawable.ic_v2_ctrl_forward, rotateIcon180 = true),
            PillSpec(R.id.btnAccel, R.string.v2_accel, R.drawable.ic_v2_ctrl_accel),
            PillSpec(R.id.btnDecel, R.string.v2_decel, R.drawable.ic_v2_ctrl_decel),
            PillSpec(R.id.btnContinuous, R.string.v2_continuous, R.drawable.ic_v2_ctrl_continuous),
            PillSpec(R.id.btnJog, R.string.v2_jog, R.drawable.ic_v2_ctrl_jog),
            PillSpec(R.id.btnAutoPhoto, R.string.v2_auto_photo, R.drawable.ic_v2_ctrl_camera),
            PillSpec(R.id.btnControlSettings, R.string.v2_settings_btn, R.drawable.ic_v2_ctrl_settings),
            PillSpec(R.id.btnBacklash, R.string.v2_backlash, R.drawable.ic_v2_ctrl_backlash, showBacklashStatus = true),
            PillSpec(R.id.btnEnd, R.string.v2_end, R.drawable.ic_v2_ctrl_end, isEnd = true),
        )
        specs.forEach { bindPill(root, it) }
    }

    private fun setupManualControlPills(root: View) {
        val specs = listOf(
            PillSpec(R.id.btnForwardManual, R.string.v2_forward, R.drawable.ic_v2_ctrl_forward),
            PillSpec(R.id.btnReverseManual, R.string.v2_reverse, R.drawable.ic_v2_ctrl_forward, rotateIcon180 = true),
            PillSpec(R.id.btnAccelManual, R.string.v2_accel, R.drawable.ic_v2_ctrl_accel),
            PillSpec(R.id.btnDecelManual, R.string.v2_decel, R.drawable.ic_v2_ctrl_decel),
            PillSpec(R.id.btnContinuousManual, R.string.v2_continuous, R.drawable.ic_v2_ctrl_continuous),
            PillSpec(R.id.btnJogManual, R.string.v2_jog, R.drawable.ic_v2_ctrl_jog),
            PillSpec(
                R.id.btnManualModeSettings,
                R.string.v2_manual_mode_settings,
                R.drawable.ic_v2_ctrl_settings,
            ),
            PillSpec(
                R.id.btnBacklashManual,
                R.string.v2_backlash,
                R.drawable.ic_v2_ctrl_backlash,
                showBacklashStatus = true,
            ),
            PillSpec(R.id.btnEndManual, R.string.v2_end, R.drawable.ic_v2_ctrl_end, isEnd = true),
        )
        specs.forEach { bindPill(root, it) }
    }

    private fun setupStartPauseButtons(root: View) {
        val pad = (resources.displayMetrics.density * 12).toInt()
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
        val card = pill.findViewById<MaterialCardView>(R.id.cardControlRoot)
            ?: (pill as? MaterialCardView)
            ?: return
        pill.findViewById<TextView>(R.id.tvControlLabel).setText(spec.labelRes)
        pill.findViewById<ImageView>(R.id.ivControlIcon).apply {
            setImageResource(spec.iconRes)
            rotation = if (spec.rotateIcon180) 180f else 0f
            ImageViewCompat.setImageTintList(
                this,
                ColorStateList.valueOf(ContextCompat.getColor(this@V2InspectionControlActivity, R.color.v2_on_primary)),
            )
        }
        pill.findViewById<TextView>(R.id.tvControlSubLabel)?.let { sub ->
            if (spec.showBacklashStatus) {
                sub.visibility = View.VISIBLE
                sub.text = getString(R.string.v2_backlash_status_ok)
            } else {
                sub.visibility = View.GONE
            }
        }
        applyPillBackground(card, green = false, end = spec.isEnd)
        pill.tag = spec
    }

    private fun applyPillBackground(card: MaterialCardView, green: Boolean, end: Boolean) {
        val bgRes = when {
            end -> R.drawable.bg_v2_control_btn_end
            green -> R.drawable.bg_v2_control_btn_green
            else -> R.drawable.bg_v2_control_btn_orange
        }
        card.background = ContextCompat.getDrawable(this, bgRes)
    }

    private fun applyControlHighlights(root: View, state: V2ControlHighlightState, manual: Boolean) {
        if (manual) {
            applyHighlightFor(root, R.id.btnForwardManual, state.forwardGreen)
            applyHighlightFor(root, R.id.btnReverseManual, state.reverseGreen)
            applyHighlightFor(root, R.id.btnContinuousManual, state.continuousGreen)
            applyHighlightFor(root, R.id.btnJogManual, state.jogGreen)
            applyHighlightFor(root, R.id.btnAccelManual, false)
            applyHighlightFor(root, R.id.btnDecelManual, false)
            applyHighlightFor(root, R.id.btnManualModeSettings, false)
            applyHighlightFor(root, R.id.btnBacklashManual, false)
            applyHighlightFor(root, R.id.btnEndManual, false, end = true)
        } else {
            applyHighlightFor(root, R.id.btnForward, state.forwardGreen)
            applyHighlightFor(root, R.id.btnReverse, state.reverseGreen)
            applyHighlightFor(root, R.id.btnContinuous, state.continuousGreen)
            applyHighlightFor(root, R.id.btnJog, state.jogGreen)
            applyHighlightFor(root, R.id.btnAutoPhoto, state.autoPhotoGreen)
            applyHighlightFor(root, R.id.btnAccel, false)
            applyHighlightFor(root, R.id.btnDecel, false)
            applyHighlightFor(root, R.id.btnControlSettings, false)
            applyHighlightFor(root, R.id.btnBacklash, false)
            applyHighlightFor(root, R.id.btnEnd, false, end = true)
        }
    }

    private fun applyHighlightFor(root: View, viewId: Int, green: Boolean, end: Boolean = false) {
        val pill = root.findViewById<View>(viewId) ?: return
        val card = pill.findViewById<MaterialCardView>(R.id.cardControlRoot)
            ?: (pill as? MaterialCardView)
            ?: return
        applyPillBackground(card, green = green && !end, end = end)
    }

    private fun setupModeSpinner(root: View) {
        val trigger = root.findViewById<TextView>(R.id.tvModeSpinner)
        val initial = viewModel.operationMode.value ?: V2UiOperationMode.AUTO
        modeSelector = V2InspectionModeSelector(this, trigger).apply {
            setup(initial)
            onModeSelected = { mode ->
                if (suppressModeSelection.not()) {
                    viewModel.setOperationMode(mode)
                }
            }
        }
    }

    private fun setupLpcSelector(root: View, positions: List<String>, selectedIndex: Int) {
        val trigger = root.findViewById<TextView>(R.id.tvLpcSpinner)
        lpcSelector = V2InspectionLpcSelector(this, trigger).apply {
            setup(positions, bladeCounts, selectedIndex)
            onIndexSelected = { index ->
                if (!suppressLpcSelection) {
                    viewModel.setLpcIndex(index)
                }
            }
        }
    }

    private fun applyOperationModeUi(root: View, mode: V2UiOperationMode) {
        val auto = mode == V2UiOperationMode.AUTO
        root.findViewById<View>(R.id.panelAutoOps).visibility = if (auto) View.VISIBLE else View.GONE
        root.findViewById<View>(R.id.panelManualOps).visibility = if (auto) View.GONE else View.VISIBLE
        root.findViewById<androidx.core.widget.NestedScrollView>(R.id.scrollOperation).scrollTo(0, 0)
    }

    /**
     * 发送机未启动：操作区仅「自动/手动模式设置」可点；记录区暂停/记录/回程间隙禁用。
     * 已启动：当前可见操作区全部可点（自动/手动运行中一致）。
     */
    private fun updateControlEnabled(root: View, running: Boolean) {
        val autoPillIds = listOf(
            R.id.btnForward,
            R.id.btnReverse,
            R.id.btnAccel,
            R.id.btnDecel,
            R.id.btnContinuous,
            R.id.btnJog,
            R.id.btnAutoPhoto,
            R.id.btnControlSettings,
            R.id.btnBacklash,
            R.id.btnEnd,
        )
        val manualPillIds = listOf(
            R.id.btnForwardManual,
            R.id.btnReverseManual,
            R.id.btnAccelManual,
            R.id.btnDecelManual,
            R.id.btnContinuousManual,
            R.id.btnJogManual,
            R.id.btnManualModeSettings,
            R.id.btnBacklashManual,
            R.id.btnEndManual,
        )

        setRecordBarEnabled(root, running)

        if (!running) {
            autoPillIds.forEach { id ->
                setControlPillEnabled(root, id, id == R.id.btnControlSettings)
            }
            manualPillIds.forEach { id ->
                setControlPillEnabled(root, id, id == R.id.btnManualModeSettings)
            }
            return
        }

        autoPillIds.forEach { id -> setControlPillEnabled(root, id, true) }
        manualPillIds.forEach { id -> setControlPillEnabled(root, id, true) }
    }

    private fun setRecordBarEnabled(root: View, running: Boolean) {
        root.findViewById<View>(R.id.btnPause).apply {
            isEnabled = running
            alpha = if (running) 1f else DISABLED_ALPHA
        }
        root.findViewById<View>(R.id.btnRecord).apply {
            isEnabled = running
            alpha = if (running) 1f else DISABLED_ALPHA
        }
        root.findViewById<View>(R.id.btnBacklashOnReturn).apply {
            isEnabled = running
            alpha = if (running) 1f else DISABLED_ALPHA
        }
    }

    private fun setControlPillEnabled(root: View, viewId: Int, enabled: Boolean) {
        root.findViewById<View>(viewId)?.apply {
            isEnabled = enabled
            isClickable = enabled
            alpha = if (enabled) 1f else DISABLED_ALPHA
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
        tv.gravity = android.view.Gravity.CENTER
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
//            finish()
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
        if (!viewModel.requiresDeviceConnection()) return true
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
        val rotateIcon180: Boolean = false,
        val showBacklashStatus: Boolean = false,
        val isEnd: Boolean = false,
    )

    companion object {
        private const val DISABLED_ALPHA = 0.45f

        const val EXTRA_MODEL_ID = "extra_v2_inspection_model_id"
        const val EXTRA_MODEL_NAME = "extra_v2_model_name"
    }
}
