package com.devicecontrol.engine.v2.connection

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * V2 蓝牙扫描/连接所需运行时权限（按 API 分级）。
 */
object V2BluetoothPermissions {

    fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

    fun hasAll(activity: ComponentActivity): Boolean =
        requiredPermissions().all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * 注册权限请求；[onResult] 在全部授予时为 true。
     * 须在 Activity [onCreate] 中、STARTED 之前调用。
     */
    fun registerLauncher(
        activity: ComponentActivity,
        onResult: (granted: Boolean) -> Unit,
    ): () -> Unit {
        val launcher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { result ->
            onResult(result.values.all { it })
        }
        return {
            if (hasAll(activity)) {
                onResult(true)
            } else {
                launcher.launch(requiredPermissions())
            }
        }
    }
}
