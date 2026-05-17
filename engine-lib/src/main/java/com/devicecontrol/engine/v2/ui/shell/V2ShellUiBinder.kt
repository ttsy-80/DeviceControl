package com.devicecontrol.engine.v2.ui.shell

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.lifecycle.LifecycleOwner
import com.devicecontrol.engine.R
import com.devicecontrol.engine.v2.connection.V2ConnectionRepository
import com.devicecontrol.engine.v2.connection.V2ConnectionState
import com.devicecontrol.engine.v2.ui.widget.V2BatteryView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 绑定 [include_v2_top_bar]：时钟、居中标题、返回首页、电量、连接胶囊。
 */
class V2ShellUiBinder(
    private val topBarRoot: View,
    private val lifecycleOwner: LifecycleOwner,
) {
    private val context: Context = topBarRoot.context
    private val tvTime: TextView = topBarRoot.findViewById(R.id.tvV2Time)
    private val tvDate: TextView = topBarRoot.findViewById(R.id.tvV2Date)
    private val ivTitleIcon: ImageView = topBarRoot.findViewById(R.id.ivV2TitleIcon)
    private val tvTitleCn: TextView = topBarRoot.findViewById(R.id.tvV2TitleCn)
    private val tvTitleEn: TextView = topBarRoot.findViewById(R.id.tvV2TitleEn)
    private val btnBackHome: TextView = topBarRoot.findViewById(R.id.btnV2BackHome)
    private val tvConnection: TextView = topBarRoot.findViewById(R.id.tvV2Connection)
    private val batteryView: V2BatteryView = topBarRoot.findViewById(R.id.v2Battery)

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEEE yyyy/MM/dd", Locale.CHINA)
    private val handler = Handler(Looper.getMainLooper())
    private val tickRunnable = object : Runnable {
        override fun run() {
            val now = Date()
            tvTime.text = timeFormat.format(now)
            tvDate.text = dateFormat.format(now)
            handler.postDelayed(this, 1000L)
        }
    }

    private var batteryReceiverRegistered = false
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            applyBatteryIntent(intent)
        }
    }

    private fun applyBatteryIntent(intent: Intent?) {
        if (intent == null) return
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level >= 0 && scale > 0) {
            setBatteryLevel((level * 100f / scale).toInt())
        }
    }

    fun bindTitles(titleCn: String, titleEn: String) {
        tvTitleCn.text = titleCn
        tvTitleEn.text = titleEn
    }

    fun setTitleIcon(@DrawableRes iconRes: Int?) {
        if (iconRes == null) {
            ivTitleIcon.visibility = View.GONE
        } else {
            ivTitleIcon.setImageResource(iconRes)
            ivTitleIcon.visibility = View.VISIBLE
        }
    }

    fun setBackHomeVisible(visible: Boolean, onClick: (() -> Unit)? = null) {
        btnBackHome.visibility = if (visible) View.VISIBLE else View.GONE
        btnBackHome.setOnClickListener { onClick?.invoke() }
    }

    /** 供业务或调试设置电量 0～100 */
    fun setBatteryLevel(percent: Int) {
        batteryView.level = percent
    }

    fun observeConnection() {
        V2ConnectionRepository.connectionState.observe(lifecycleOwner) { state ->
            when (state) {
                V2ConnectionState.CONNECTED -> {
                    tvConnection.setText(R.string.v2_connected)
                    tvConnection.setBackgroundResource(R.drawable.bg_v2_status_connected)
                }
                V2ConnectionState.DISCONNECTED -> {
                    tvConnection.setText(R.string.v2_disconnected)
                    tvConnection.setBackgroundResource(R.drawable.bg_v2_status_disconnected)
                }
            }
        }
    }

    fun startClock() {
        handler.removeCallbacks(tickRunnable)
        tickRunnable.run()
        registerBatteryUpdates()
    }

    fun stopClock() {
        handler.removeCallbacks(tickRunnable)
        unregisterBatteryUpdates()
    }

    private fun registerBatteryUpdates() {
        if (batteryReceiverRegistered) return
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        applyBatteryIntent(context.registerReceiver(null, filter))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(batteryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(batteryReceiver, filter)
        }
        batteryReceiverRegistered = true
    }

    private fun unregisterBatteryUpdates() {
        if (!batteryReceiverRegistered) return
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (_: IllegalArgumentException) {
            // already unregistered
        }
        batteryReceiverRegistered = false
    }
}
