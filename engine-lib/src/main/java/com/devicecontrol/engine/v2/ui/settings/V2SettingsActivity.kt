package com.devicecontrol.engine.v2.ui.settings

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.ui.base.V2BaseShellActivity
import com.devicecontrol.engine.v2.ui.home.V2HomeActivity
import com.devicecontrol.engine.v2.viewmodel.V2SettingsPage
import com.devicecontrol.engine.v2.viewmodel.V2SettingsViewModel

/** 2.0 设置（P2～P5），布局对齐 PDF 稿面 */
class V2SettingsActivity : V2BaseShellActivity() {

    override val logTag: String = "Settings"

    private val viewModel: V2SettingsViewModel by viewModels()

    private lateinit var menuViews: Map<V2SettingsPage, TextView>
    private var bluetoothPanel: View? = null
    private var languagePanel: View? = null
    private var updatePanel: View? = null
    private var aboutPanel: View? = null

    private val deviceTextViews = mutableListOf<TextView>()
    private val languageTextViews = mutableListOf<TextView>()
    private var selectedLanguageIndex = 0

    override fun contentLayoutId(): Int = R.layout.content_v2_settings

    override fun shellTitleCn(): String = getString(R.string.v2_app_title_cn)
    override fun shellTitleEn(): String = getString(R.string.v2_app_title_en)

    /** 设置页底栏品牌放在右侧内容区底部，隐藏全局壳底栏 */
    override fun showShellBottomBar(): Boolean = false

    override fun onContentCreated(contentRoot: View) {
        val inflater = LayoutInflater.from(this)
        val container = contentRoot.findViewById<View>(R.id.settingsContent) as android.view.ViewGroup

        bluetoothPanel = inflater.inflate(R.layout.panel_v2_settings_bluetooth, container, false)
        languagePanel = inflater.inflate(R.layout.panel_v2_settings_language, container, false)
        updatePanel = inflater.inflate(R.layout.panel_v2_settings_update, container, false)
        aboutPanel = inflater.inflate(R.layout.panel_v2_settings_about, container, false)

        menuViews = mapOf(
            V2SettingsPage.BLUETOOTH to contentRoot.findViewById(R.id.menuBluetooth),
            V2SettingsPage.LANGUAGE to contentRoot.findViewById(R.id.menuLanguage),
            V2SettingsPage.UPDATE to contentRoot.findViewById(R.id.menuUpdate),
            V2SettingsPage.ABOUT to contentRoot.findViewById(R.id.menuAbout),
            V2SettingsPage.MANUAL to contentRoot.findViewById(R.id.menuManual),
        )

        menuViews.forEach { (page, tv) ->
            tv.setOnClickListener {
                if (page == V2SettingsPage.MANUAL) {
                    Toast.makeText(this, R.string.v2_settings_manual, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.selectPage(page)
            }
        }

        contentRoot.findViewById<View>(R.id.btnBackMain).setOnClickListener {
            startActivity(Intent(this, V2HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
            finish()
        }

        setupBluetoothPanel()
        setupLanguagePanel()
        setupUpdatePanel()

        viewModel.currentPage.observe(this) { page ->
            highlightMenu(page)
            showPanel(page, container)
        }
        viewModel.selectedDeviceIndex.observe(this) {
            V2SettingsSelectionUi.applyListSelection(this, deviceTextViews, it)
        }
        viewModel.selectPage(V2SettingsPage.BLUETOOTH)
    }

    private fun highlightMenu(page: V2SettingsPage) {
        menuViews.forEach { (p, tv) ->
            val selected = p == page
            tv.setBackgroundResource(
                if (selected) R.drawable.bg_v2_menu_selected else R.drawable.bg_v2_menu_normal,
            )
            tv.setTextColor(getColor(if (selected) R.color.v2_on_primary else R.color.v2_text_primary))
        }
    }

    private fun showPanel(page: V2SettingsPage, container: android.view.ViewGroup) {
        container.removeAllViews()
        val panel = when (page) {
            V2SettingsPage.BLUETOOTH -> bluetoothPanel
            V2SettingsPage.LANGUAGE -> languagePanel
            V2SettingsPage.UPDATE -> updatePanel
            V2SettingsPage.ABOUT -> aboutPanel
            V2SettingsPage.MANUAL -> aboutPanel
        } ?: return
        container.addView(panel)
    }

    private fun setupBluetoothPanel() {
        val panel = bluetoothPanel ?: return
        val ids = listOf(R.id.tvDevice1, R.id.tvDevice2, R.id.tvDevice3, R.id.tvDevice4, R.id.tvDevice5)
        deviceTextViews.clear()
        val devices = viewModel.bluetoothDevices.value.orEmpty()
        ids.forEachIndexed { index, id ->
            val tv = panel.findViewById<TextView>(id)
            tv.text = devices.getOrElse(index) { getString(R.string.v2_device_sample, index + 1) }
            tv.setOnClickListener { viewModel.selectDevice(index) }
            deviceTextViews.add(tv)
        }
        V2SettingsSelectionUi.applyListSelection(
            this,
            deviceTextViews,
            viewModel.selectedDeviceIndex.value ?: 0,
        )

        panel.findViewById<View>(R.id.btnConfirmConnect).setOnClickListener {
            viewModel.confirmBluetoothConnect()
            Toast.makeText(this, R.string.v2_connected, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLanguagePanel() {
        val panel = languagePanel ?: return
        val ids = listOf(R.id.langZh, R.id.langZhTw, R.id.langEn, R.id.langDe, R.id.langFr)
        languageTextViews.clear()
        ids.forEachIndexed { index, id ->
            val tv = panel.findViewById<TextView>(id)
            languageTextViews.add(tv)
            tv.setOnClickListener {
                selectedLanguageIndex = index
                V2SettingsSelectionUi.applyListSelection(this, languageTextViews, index)
            }
        }
        V2SettingsSelectionUi.applyListSelection(this, languageTextViews, 0)

        panel.findViewById<View>(R.id.btnChangeLanguage).setOnClickListener {
            val code = when (selectedLanguageIndex) {
                1 -> "zh-TW"
                2 -> "en"
                3 -> "de"
                4 -> "fr"
                else -> "zh"
            }
            viewModel.applyLanguage(code)
            Toast.makeText(this, R.string.v2_change_language, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupUpdatePanel() {
        updatePanel?.findViewById<TextView>(R.id.tvCurrentVersion)?.text =
            getString(R.string.v2_version_format, "v.1.0.0")
        updatePanel?.findViewById<TextView>(R.id.tvLastInspection)?.text =
            getString(R.string.v2_last_check_format, "2026.04.14")
        updatePanel?.findViewById<View>(R.id.btnCheckUpdate)?.setOnClickListener {
            viewModel.checkUpdate()
            Toast.makeText(this, R.string.v2_check_update, Toast.LENGTH_SHORT).show()
        }
    }
}
