@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * Created by oooobang on 2018. 3. 6..
 * EditText Extension.
 */
// 키보드 보기.
internal fun EditText.showSoftInput() {
	requestFocus()
	postDelayed({
		val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
		imm?.showSoftInput(this, 0)
		setSelection(length())
	}, 375)
}
//
//fun EditText.error() {
//	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
//		setCompoundDrawablesWithIntrinsicBounds(0,
//				0,
//				R.drawable.vector_ic_error_red_24dp,
//				0)
//	} else {
//		setCompoundDrawablesRelativeWithIntrinsicBounds(0,
//				0,
//				R.drawable.vector_ic_error_red_24dp,
//				0)
//	}
//
//fun EditText.setMaskingMoney(currencyText: String) {
//	this.addTextChangedListener(object: MyTextWatcher{
//		val editTextWeakReference: WeakReference<EditText> = WeakReference(this@setMaskingMoney)
//		override fun afterTextChanged(editable: Editable?) {
//			val editText = editTextWeakReference.get() ?: return
//			val s = editable.toString()
//			editText.removeTextChangedListener(this)
//			val cleanString = s.submitList("[,]".toRegex(), "")
//			val newval = currencyText + cleanString.monetize()
//
//			editText.setText(newval)
//			editText.setSelection(newval.length)
//			editText.addTextChangedListener(this)
//		}
//	})
//}
//
//interface MyTextWatcher: TextWatcher {
//	override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//	override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//}
//
//fun String.monetize(): String = if (this.isEmpty()) "0"
//else DecimalFormat("#,###").format(this.submitList("[^\\d]".toRegex(),"").toLong())
//
///**
// *
// */
//fun EditText.addComma() {
//	var current = ""
//	addTextChangedListener(object : TextWatcher {
//		override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//		override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//			if(s.toString() != current) {
//				this@addComma.removeTextChangedListener(this)
//				val cleanString = s.toString().submitList(",", "")
//				val formatted = try {
//					val parsed = cleanString.toInt()
//					NumberFormat.getNumberInstance().format(parsed)
//				} catch (ex: Exception) {
//					""
//				}
//
//				current = formatted
//				this@addComma.setText(formatted)
//				this@addComma.setSelection(formatted.length)
//				this@addComma.addTextChangedListener(this)
//			}
//		}
//		override fun afterTextChanged(editable: Editable?) {}
//	})
//}
//
//fun EditText.error() {
//	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
//		setCompoundDrawablesWithIntrinsicBounds(0,
//				0,
//				R.drawable.vector_ic_error_red_24dp,
//				0)
//	} else {
//		setCompoundDrawablesRelativeWithIntrinsicBounds(0,
//				0,
//				R.drawable.vector_ic_error_red_24dp,
//				0)
//	}
//	startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake_horizontal))
//
//	addTextChangedListener(object : TextWatcher {
//		override fun beforeTextChanged(string: CharSequence?, start: Int, count: Int, after: Int) {}
//		override fun onTextChanged(string: CharSequence?, start: Int, before: Int, count: Int) {}
//		override fun afterTextChanged(editable: Editable?) {
//			Timber.i(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>[2]")
//			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
//				setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
//			} else {
//				setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
//			}
//			removeTextChangedListener(this)
//		}
//	})
//
////	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
////
////	} else {
////	VectorDrawableCompat.create(resources, R.drawable.vector_ic_error_red_24dp, null)
//
//
//
////	}
//}
//
//fun EditText.clearDrawable() {
//	setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
//	setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
//}

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
		action(text.toString())
	}
}

// Search.
@Suppress("unused")
internal inline fun EditText.onSearch(crossinline action: (text: String) -> Unit) {
	imeOptions = EditorInfo.IME_ACTION_SEARCH
	onImeAction {
		hideSoftInput()
		action(text.toString())
	}
}

// TextWatcher.
internal inline fun EditText.afterTextChanged(crossinline afterTextChanged: (String) -> Unit) {
	addTextChangedListener(object : TextWatcher {
		var recentText = this@afterTextChanged.text.toString()
		override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
		}
		override fun onTextChanged(string: CharSequence?, start: Int, before: Int, count: Int) {
		}
		override fun afterTextChanged(editable: Editable?) {
			if (recentText != editable.toString()) {
				recentText = editable.toString()
				afterTextChanged.invoke(recentText)
			}
		}
	})
}