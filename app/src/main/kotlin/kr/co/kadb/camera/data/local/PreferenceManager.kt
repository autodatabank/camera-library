package kr.co.kadb.camera.data.local

import android.content.SharedPreferences
import javax.inject.Inject

class PreferenceManager
@Inject
constructor(private val sharedPreferences: SharedPreferences) {
//    var infoSkip: Boolean
//        get() = preferences.getBoolean(INFO_SKIP_KEY, false)
//        set(value) {
//            val editor = preferences.edit()
//            editor.putBoolean(INFO_SKIP_KEY, value)
//            editor.apply()
//        }

    companion object {
        private const val LILY_APP = "LILY_APP"
        const val INFO_SKIP_KEY = "AUTO_LOGIN_KEY"
    }
}