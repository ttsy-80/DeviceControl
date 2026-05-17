package com.devicecontrol.engine.v2.viewmodel

import androidx.lifecycle.ViewModel
import com.devicecontrol.engine.v2.log.V2Log

/**
 * 2.0 首页：仅负责导航事件日志，无持久化。
 */
class V2HomeViewModel : ViewModel() {

    fun onStartInspectionClicked() {
        V2Log.i(TAG, "user tap startInspection")
    }

    fun onModelManageClicked() {
        V2Log.i(TAG, "user tap modelManage")
    }

    fun onSettingsClicked() {
        V2Log.i(TAG, "user tap settings")
    }

    companion object {
        private const val TAG = "HomeVM"
    }
}
