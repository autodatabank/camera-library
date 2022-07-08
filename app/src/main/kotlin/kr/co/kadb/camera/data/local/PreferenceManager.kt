package kr.co.kadb.camera.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(LILY_APP, Context.MODE_PRIVATE)

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