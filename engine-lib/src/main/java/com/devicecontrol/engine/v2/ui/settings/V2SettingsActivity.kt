package com.devicecontrol.engine.v2.ui.settings

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.connection.V2BluetoothPermissions
import com.devicecontrol.engine.v2.ui.dialog.V2LoadingDialog
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

    private val deviceTextViews = mutableListOf<TextView>()

    private lateinit var requestBluetoothPermissions: () -> Unit

    override fun contentLayoutId(): Int = R.layout.content_v2_settings

    override fun shellTitleCn(): String = getString(R.string.v2_app_title_cn)
    override fun shellTitleEn(): String = getString(R.string.v2_app_title_en)

    override fun showShellBottomBar(): Boolean = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        requestBluetoothPermissions = V2BluetoothPermissions.registerLauncher(this) { granted ->
            if (granted) {
                viewModel.refreshBluetoothScan()
            } else {
                Toast.makeText(this, R.string.v2_bluetooth_permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun onContentCreated(contentRoot: View) {
        val inflater = LayoutInflater.from(this)
        val container = contentRoot.findViewById<View>(R.id.settingsContent) as android.view.ViewGroup

        bluetoothPanel = inflater.inflate(R.layout.panel_v2_settings_bluetooth, container, false)

        menuViews = mapOf(
            V2SettingsPage.BLUETOOTH to contentRoot.findViewById(R.id.menuBluetooth),
            V2SettingsPage.LANGUAGE to contentRoot.findViewById(R.id.menuLanguage),
            V2SettingsPage.UPDATE to contentRoot.findViewById(R.id.menuUpdate),
            V2SettingsPage.ABOUT to contentRoot.findViewById(R.id.menuAbout),
            V2SettingsPage.MANUAL to contentRoot.findViewById(R.id.menuManual),
        )

        menuViews.forEach { (page, tv) ->
            tv.setOnClickListener {
                if (page == V2SettingsPage.BLUETOOTH) {
                    viewModel.selectPage(page)
                } else {
                    toastTabPlaceholder(page)
                }
            }
        }

        contentRoot.findViewById<View>(R.id.btnBackMain).setOnClickListener {
            startActivity(Intent(this, V2HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
            finish()
        }

        setupBluetoothPanel()
        bindBluetoothObservers()

        viewModel.currentPage.observe(this) { page ->
            highlightMenu(page)
            showPanel(page, container)
            if (page == V2SettingsPage.BLUETOOTH) {
                requestBluetoothPermissions()
            }
        }
        viewModel.selectPage(V2SettingsPage.BLUETOOTH)
    }

    override fun onPause() {
//        viewModel.stopBluetoothScan()
        super.onPause()
    }

    override fun onDestroy() {
        V2LoadingDialog.dismiss()
        super.onDestroy()
    }

    private fun bindBluetoothObservers() {
        viewModel.bluetoothDevices.observe(this) { devices ->
            deviceTextViews.forEachIndexed { index, tv ->
                if (index < devices.size) {
                    tv.visibility = View.VISIBLE
                    tv.text = devices[index].displayName
                } else {
                    tv.visibility = View.GONE
                }
            }
            bluetoothPanel?.findViewById<View>(R.id.btnConfirmConnect)?.visibility =
                if (devices.isNotEmpty()) View.VISIBLE else View.GONE
            bluetoothPanel?.findViewById<View>(R.id.scrollPanelContent)?.visibility =
                if (devices.isNotEmpty()) View.VISIBLE else View.GONE
            updateScanCenterVisibility()
            updateConfirmConnectEnabled()
        }
        viewModel.selectedDeviceIndex.observe(this) {
            V2SettingsSelectionUi.applyListSelection(this, deviceTextViews, it)
        }
        viewModel.bluetoothScanning.observe(this) { scanning ->
            bluetoothPanel?.findViewById<View>(R.id.tvScanStatus)?.visibility =
                if (scanning) View.VISIBLE else View.GONE
            updateScanCenterVisibility()
            updateConfirmConnectEnabled()
        }
        viewModel.bluetoothScanEmpty.observe(this) { empty ->
            bluetoothPanel?.findViewById<View>(R.id.llScanEmpty)?.visibility =
                if (empty) View.VISIBLE else View.GONE
            updateScanCenterVisibility()
        }
        viewModel.connecting.observe(this) { connecting ->
            updateConnectingUi(connecting)
        }
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
        viewModel.connectSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, R.string.v2_connected, Toast.LENGTH_SHORT).show()
                viewModel.consumeConnectSuccess()
            }
        }
    }

    private fun toastTabPlaceholder(page: V2SettingsPage) {
        val msgRes = when (page) {
            V2SettingsPage.LANGUAGE -> R.string.v2_settings_language
            V2SettingsPage.UPDATE -> R.string.v2_settings_update
            V2SettingsPage.ABOUT -> R.string.v2_settings_about
            V2SettingsPage.MANUAL -> R.string.v2_settings_manual
            V2SettingsPage.BLUETOOTH -> return
        }
        Toast.makeText(this, msgRes, Toast.LENGTH_SHORT).show()
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
            else -> return
        } ?: return
        container.addView(panel)
    }

    private fun setupBluetoothPanel() {
        val panel = bluetoothPanel ?: return
        val ids = listOf(R.id.tvDevice1, R.id.tvDevice2, R.id.tvDevice3, R.id.tvDevice4, R.id.tvDevice5)
        deviceTextViews.clear()
        ids.forEachIndexed { index, id ->
            val tv = panel.findViewById<TextView>(id)
            tv.setOnClickListener { viewModel.selectDevice(index) }
            deviceTextViews.add(tv)
        }

        panel.findViewById<View>(R.id.btnConfirmConnect).setOnClickListener {
            viewModel.confirmBluetoothConnect()
        }
        panel.findViewById<View>(R.id.btnRetryScan).setOnClickListener {
            requestBluetoothPermissions()
        }
    }

    private fun updateScanCenterVisibility() {
        val scanning = viewModel.bluetoothScanning.value == true
        val empty = viewModel.bluetoothScanEmpty.value == true
        bluetoothPanel?.findViewById<View>(R.id.flScanCenter)?.visibility =
            if (scanning || empty) View.VISIBLE else View.GONE
    }

    private fun updateConnectingUi(connecting: Boolean) {
        val btnConfirm = bluetoothPanel?.findViewById<TextView>(R.id.btnConfirmConnect)
        if (connecting) {
            val idx = viewModel.selectedDeviceIndex.value ?: 0
            val deviceName = viewModel.bluetoothDevices.value?.getOrNull(idx)?.displayName.orEmpty()
            V2LoadingDialog.show(
                this,
                getString(R.string.v2_bluetooth_connecting_format, deviceName),
            )
            btnConfirm?.text = getString(R.string.v2_bluetooth_connecting)
        } else {
            V2LoadingDialog.dismiss()
            btnConfirm?.text = getString(R.string.v2_confirm_connect)
        }
        updateConfirmConnectEnabled()
    }

    private fun updateConfirmConnectEnabled() {
        val hasDevices = !viewModel.bluetoothDevices.value.isNullOrEmpty()
        val connecting = viewModel.connecting.value == true
        bluetoothPanel?.findViewById<View>(R.id.btnConfirmConnect)?.isEnabled = hasDevices && !connecting
    }
}
