package com.devicecontrol.app

import android.app.Application
import com.devicecontrol.engine.lifecycle.SlcanEmergencyClose
import com.devicecontrol.engine.v2.connection.V2SlcanConnectionManager

class DeviceControlApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        V2SlcanConnectionManager.init(this)
        SlcanEmergencyClose.installUncaughtExceptionHandler()
    }
}
