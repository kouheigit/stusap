package com.example.vocabapp.ui.screen.common

import com.example.vocabapp.*

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
internal fun AddWordField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: Int,
    inputType: Int = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES,
    autoFocus: Boolean = false,
    onImeAction: () -> Unit = {},
    onReady: (EditText) -> Unit = {},
) {
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnImeAction by rememberUpdatedState(onImeAction)
    val currentOnReady by rememberUpdatedState(onReady)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontWeight = FontWeight.Bold, color = TextMuted)
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            factory = { context ->
                EditText(context).apply {
                    setSingleLine(true)
                    hint = placeholder
                    textSize = 16f
                    setTextColor(TextDark.toArgb())
                    setHintTextColor(TextMuted.toArgb())
                    setPadding(32, 0, 32, 0)
                    this.inputType = inputType
                    imeOptions = imeAction
                    background = android.graphics.drawable.GradientDrawable().apply {
                        cornerRadius = 8.dp.value * resources.displayMetrics.density
                        setColor(android.graphics.Color.WHITE)
                        setStroke((1.dp.value * resources.displayMetrics.density).toInt(), Color(0xFFB0BEC5).toArgb())
                    }
                    addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            currentOnValueChange(s?.toString().orEmpty())
                        }
                        override fun afterTextChanged(s: Editable?) = Unit
                    })
                    setOnEditorActionListener { _, actionId, _ ->
                        if (actionId == imeAction) {
                            currentOnImeAction()
                            true
                        } else {
                            false
                        }
                    }
                    setOnFocusChangeListener { view, hasFocus ->
                        if (hasFocus) view.showKeyboard()
                    }
                    if (autoFocus) postDelayed({ focusAndShowKeyboard() }, 300)
                    currentOnReady(this)
                }
            },
            update = { editText ->
                if (editText.text.toString() != value) {
                    editText.setText(value)
                    editText.setSelection(value.length)
                }
                if (editText.imeOptions != imeAction) editText.imeOptions = imeAction
                if (editText.inputType != inputType) editText.inputType = inputType
                currentOnReady(editText)
            }
        )
    }
}

internal fun android.view.View.showKeyboard() {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

internal fun EditText.focusAndShowKeyboard() {
    requestFocus()
    setSelection(text?.length ?: 0)
    showKeyboard()
}
