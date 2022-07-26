package kr.co.kadb.cameralibrary.data.local

import android.content.Context
import android.content.SharedPreferences

//import javax.inject.Inject

internal class PreferenceManager
constructor(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    var flashMode: Int
        get() = preferences.getInt("flashMode", 0)
        set(value) {
            val editor = preferences.edit()
            editor.putInt("flashMode", value)
            editor.apply()
        }
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