package com.devicecontrol.engine.v2.ui.widget

import android.content.Context
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/** 2.0 横屏：禁止全屏提取输入法，与 v1 [NewModelDialog] 一致 */
fun EditText.applyV2LandscapeIme(action: Int = EditorInfo.IME_ACTION_DONE) {
    imeOptions = action or EditorInfo.IME_FLAG_NO_EXTRACT_UI
}

/** 收起软键盘（anchor 无 windowToken 时静默忽略） */
fun View.hideSoftKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

/** 退出步进器编辑：清焦点并收起软键盘 */
fun EditText.finishStepperEditing() {
    if (isFocused) clearFocus()
    hideSoftKeyboard()
}
