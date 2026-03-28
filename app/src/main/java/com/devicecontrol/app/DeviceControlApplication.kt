package com.devicecontrol.app

import android.app.Application
import com.devicecontrol.engine.lifecycle.SlcanEmergencyClose

class DeviceControlApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SlcanEmergencyClose.installUncaughtExceptionHandler()
    }
}
