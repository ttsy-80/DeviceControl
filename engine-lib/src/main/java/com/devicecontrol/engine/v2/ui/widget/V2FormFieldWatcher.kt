package com.devicecontrol.engine.v2.ui.widget

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.devicecontrol.engine.R

/** 每个 EditText 仅注册一次 TextWatcher，避免重复绑定导致无法删除/键盘闪退 */
fun EditText.setV2FormTextWatcher(onChanged: (String) -> Unit) {
    val tagKey = R.id.tag_v2_form_text_watcher
    (getTag(tagKey) as? TextWatcher)?.let { removeTextChangedListener(it) }
    val watcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable?) {
            onChanged(s?.toString().orEmpty())
        }
    }
    addTextChangedListener(watcher)
    setTag(tagKey, watcher)
}

fun EditText.setTextKeepSelection(text: String) {
    if (this.text.toString() == text) return
    val tagKey = R.id.tag_v2_form_text_watcher
    val watcher = getTag(tagKey) as? TextWatcher
    watcher?.let { removeTextChangedListener(it) }
    val start = selectionStart.coerceAtLeast(0)
    val end = selectionEnd.coerceAtLeast(0)
    setText(text)
    val newLen = this.text?.length ?: 0
    val safeStart = start.coerceIn(0, newLen)
    val safeEnd = end.coerceIn(0, newLen)
    setSelection(safeStart, safeEnd)
    watcher?.let { addTextChangedListener(it) }
}
