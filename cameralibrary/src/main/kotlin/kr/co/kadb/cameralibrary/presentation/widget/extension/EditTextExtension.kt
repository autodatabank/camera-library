@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.content.Context
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * EditText Extension.
 */
// 키보드 보기.
internal fun EditText.showSoftInput() {
	requestFocus()
	postDelayed({
		val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
		imm?.showSoftInput(this, 0)
	}, 125)
}

// 입력 문자열과 다른 경우 변경.
internal fun EditText.submitText(newText: String, textWatcher: TextWatcher? = null) {
	/*if (text.toString() != newText) {
        text = Editable.Factory.getInstance().newEditable(newText)
    }*/
	if (text.toString() != newText) {
		textWatcher?.let {
			removeTextChangedListener(it)
		}
		setText(newText)
		textWatcher?.let {
			addTextChangedListener(it)
		}
	}
}

// Action.
internal inline fun EditText.onImeAction(crossinline action: (text: String) -> Unit) {
	setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
		if ((event?.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
			action(text.toString())
			return@OnKeyListener true
		}
		false
	})
	setOnEditorActionListener { _, _, _ ->
		action(text.toString())
		true
	}
}

// Done.
@Suppress("unused")
internal inline fun EditText.onDone(crossinline action: (text: String) -> Unit) {
	imeOptions = EditorInfo.IME_ACTION_DONE
	onImeAction {
		hideSoftInput()
		action(text.toString())
	}
}

// Send.
@Suppress("unused")
internal inline fun EditText.onSend(crossinline action: (text: String) -> Unit) {
	imeOptions = EditorInfo.IME_ACTION_SEND
	onImeAction {
		hideSoftInput()
		action(text?.toString() ?: "")
	}
}

// Search.
@Suppress("unused")
internal inline fun EditText.onSearch(crossinline action: (text: String) -> Unit) {
	imeOptions = EditorInfo.IME_ACTION_SEARCH
	onImeAction {
		hideSoftInput()
		action(text?.toString() ?: "")
	}
}