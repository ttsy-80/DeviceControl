package com.devicecontrol.engine.v2.ui.widget

import android.view.inputmethod.EditorInfo
import android.widget.EditText

/** 2.0 横屏：禁止全屏提取输入法，与 v1 [NewModelDialog] 一致 */
fun EditText.applyV2LandscapeIme(action: Int = EditorInfo.IME_ACTION_DONE) {
    imeOptions = action or EditorInfo.IME_FLAG_NO_EXTRACT_UI
}
