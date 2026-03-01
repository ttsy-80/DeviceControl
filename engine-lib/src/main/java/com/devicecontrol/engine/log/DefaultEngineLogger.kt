package com.devicecontrol.engine.log

import android.util.Log

/**
 * 默认日志实现：委托给系统 [Log]
 */
class DefaultEngineLogger(private val defaultTag: String = "EngineLib") : EngineLogger {
    override fun d(tag: String, message: String) {
        Log.d(defaultTag, "[$tag] $message")
    }
    override fun i(tag: String, message: String) {
        Log.i(defaultTag, "[$tag] $message")
    }
    override fun w(tag: String, message: String) {
        Log.w(defaultTag, "[$tag] $message")
    }
    override fun e(tag: String, message: String) {
        Log.e(defaultTag, "[$tag] $message")
    }
    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e(defaultTag, "[$tag] $message", throwable)
        } else {
            Log.e(defaultTag, "[$tag] $message")
        }
    }
}
