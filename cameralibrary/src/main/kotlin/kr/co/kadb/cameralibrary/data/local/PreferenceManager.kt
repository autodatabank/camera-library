package kr.co.kadb.cameralibrary.data.local

import android.content.Context
import android.content.SharedPreferences

//import javax.inject.Inject

internal class PreferenceManager
//@Inject
constructor(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
//    var infoSkip: Boolean
//        get() = preferences.getBoolean(INFO_SKIP_KEY, false)
//        set(value) {
//            val editor = preferences.edit()
//            editor.putBoolean(INFO_SKIP_KEY, value)
//            editor.apply()
//        }
//
//    companion object {
//        val instance: PreferenceManager by lazy {
//            PreferenceManager(context)
//        }
//    }

    companion object {
        @Volatile
        private var INSTANCE: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }

        private fun build(context: Context) = PreferenceManager(context.applicationContext)
    }
}