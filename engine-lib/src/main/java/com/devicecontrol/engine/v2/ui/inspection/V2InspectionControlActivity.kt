package com.devicecontrol.engine.v2.ui.inspection

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.ui.adapter.V2RecordRowAdapter
import com.devicecontrol.engine.v2.ui.base.V2BaseShellActivity
import com.devicecontrol.engine.v2.ui.dialog.V2LpcDialog
import com.devicecontrol.engine.v2.ui.settings.V2ModeSettingsActivity
import com.devicecontrol.engine.v2.viewmodel.V2InspectionControlViewModel
import com.devicecontrol.engine.v2.viewmodel.V2UiOperationMode

/** 检测主控（P7 自动 / P10 手动） */
class V2InspectionControlActivity : V2BaseShellActivity() {

    override val logTag: String = "InspectionControl"

    private val viewModel: V2InspectionControlViewModel by viewModels()
    private val recordAdapter = V2RecordRowAdapter()

    override fun contentLayoutId(): Int = R.layout.content_v2_inspection_control

    override fun shellTitleCn(): String = getString(R.string.v2_inspection_title_cn)
    override fun shellTitleEn(): String = getString(R.string.v2_inspection_title_en)

    @DrawableRes
    override fun shellTitleIcon(): Int = R.drawable.ic_v2_inspection_title

    override fun showBackHome(): Boolean = true

    /** 公司底栏在内容区展示（与 P6 一致），避免壳层白条与设计稿不符 */
    override fun showShellBottomBar(): Boolean = false

    override fun onContentCreated(contentRoot: View) {
        val modelName = intent.getStringExtra(EXTRA_MODEL_NAME) ?: "CFM56-3B"
        viewModel.initEngineModel(modelName)

        contentRoot.findViewById<TextView>(R.id.tvEngineModelName).text = modelName
        viewModel.engineParamsText.observe(this) {
            contentRoot.findViewById<TextView>(R.id.tvEngineParams).text = it
        }
        viewModel.statusBarText.observe(this) {
            contentRoot.findViewById<TextView>(R.id.tvStatusBar).text = it
        }
        viewModel.records.observe(this) { recordAdapter.submitList(it) }

        contentRoot.findViewById<RecyclerView>(R.id.rvRecords).apply {
            layoutManager = LinearLayoutManager(this@V2InspectionControlActivity)
            adapter = recordAdapter
        }

        setupAutoControlPills(contentRoot)
        setupManualControlPills(contentRoot)
        setupStartPauseButtons(contentRoot)
        setupModeSpinner(contentRoot)
        setupLpcSpinner(contentRoot)
        bindControlClicks(contentRoot)
        applyOperationModeUi(contentRoot, V2UiOperationMode.AUTO)

        contentRoot.findViewById<View>(R.id.btnStart).setOnClickListener { viewModel.onStart() }
        contentRoot.findViewById<View>(R.id.btnPause).setOnClickListener { viewModel.onPause() }
        bindEndClicks(contentRoot)
        contentRoot.findViewById<View>(R.id.btnRecord).setOnClickListener {
            viewModel.onControlAction("RECORD")
        }
        contentRoot.findViewById<View>(R.id.btnBacklashOnReturn).setOnClickListener {
            viewModel.onControlAction("BACKLASH_ON_RETURN")
        }
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
            start, start.context.getDrawable(R.drawable.ic_v2_start_badge), null, null, null,
        )
        start.compoundDrawablePadding = pad
        val pause = root.findViewById<Button>(R.id.btnPause)
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
            pause, pause.context.getDrawable(R.drawable.ic_v2_pause_badge), null, null, null,
        )
        pause.compoundDrawablePadding = pad
    }

    private fun bindPill(root: View, spec: PillSpec) {
        val pill = root.findViewById<View>(spec.viewId)
        pill.findViewById<TextView>(R.id.tvControlLabel).setText(spec.labelRes)
        pill.findViewById<ImageView>(R.id.ivControlIcon).setImageResource(spec.iconRes)
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
        spinner.setSelection(0, false)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val mode = if (position == 0) V2UiOperationMode.AUTO else V2UiOperationMode.MANUAL
                viewModel.setOperationMode(mode)
                applyOperationModeUi(root, mode)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun applyOperationModeUi(root: View, mode: V2UiOperationMode) {
        val auto = mode == V2UiOperationMode.AUTO
        root.findViewById<View>(R.id.panelAutoOps).visibility = if (auto) View.VISIBLE else View.GONE
        root.findViewById<View>(R.id.panelManualOps).visibility = if (auto) View.GONE else View.VISIBLE
        root.findViewById<View>(R.id.scrollOperation).scrollTo(0, 0)
    }

    private fun setupLpcSpinner(root: View) {
        val spinner = root.findViewById<Spinner>(R.id.spinnerLpc)
        val items = (1..4).map { getString(R.string.v2_lpc_format, it) }
        spinner.adapter = buildSpinnerAdapter(items)
        spinner.setSelection(0, false)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setLpcIndex(position + 1)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        spinner.setOnLongClickListener {
            V2LpcDialog.show(this, items, viewModel.lpcIndex.value ?: 1) { index ->
                viewModel.setLpcIndex(index)
                spinner.setSelection(index - 1)
            }
            true
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
        val tv = view.findViewById<TextView>(android.R.id.text1)
            ?: (view as? TextView)
            ?: return
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
        startActivity(
            android.content.Intent(this, V2ModeSettingsActivity::class.java).apply {
                putExtra(V2ModeSettingsActivity.EXTRA_MANUAL, manual)
            },
        )
    }

    private data class PillSpec(
        val viewId: Int,
        @StringRes val labelRes: Int,
        @DrawableRes val iconRes: Int,
        @ColorRes val colorRes: Int,
        @DrawableRes val backgroundRes: Int? = null,
    )

    companion object {
        const val EXTRA_MODEL_NAME = "extra_v2_model_name"
    }
}
