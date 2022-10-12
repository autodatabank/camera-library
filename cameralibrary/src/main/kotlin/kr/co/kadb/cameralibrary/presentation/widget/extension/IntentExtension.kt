package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.content.Intent
import android.os.Parcelable
import java.io.Serializable

/**
 * Created by oooobang on 2022. 9. 27..
 * String Extension.
 */
fun <T : Serializable?> Intent.getSerializable(name: String, clazz: Class<T>): T? {
    /*return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getSerializableExtra(name, clazz)
    } else {
        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        this.getSerializableExtra(name) as T
    }*/
    return this.getSerializableExtra(name) as T
}

fun <T : Parcelable?> Intent.getParcelable(name: String, clazz: Class<T>): T? {
    /*return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(name, clazz)
    } else {
        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        this.getParcelableExtra<T>(name)
    }*/
    return this.getParcelableExtra<T>(name)
}
