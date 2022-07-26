@file:Suppress("unused")

package kr.co.kadb.camera.presentation.widget.extension

import androidx.annotation.StringRes
import com.google.android.material.textfield.TextInputLayout
import android.view.animation.AnimationUtils
import kr.co.kadb.camera.R

/**
 * Created by oooobang on 2018. 3. 12..
 * TextInputLayout Extension.
 */
internal fun TextInputLayout.showError(@StringRes errorMessage: Int? = null) {
	// Shake Animation.
	startAnimation(AnimationUtils.loadAnimation(context, R.anim.adb_camera_shake_horizontal))

	// Error.
	errorMessage?.let {
		isErrorEnabled = true
		error = context.getString(it)
	}
}

internal fun TextInputLayout.hideError() {
	// Error.
	isErrorEnabled = false
	error = null
}