package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

/**
 * Created by oooobang on 2022. 9. 27..
 * String Extension.
 */
public fun <T : Serializable?> Intent.getSerializable(name: String, clazz: Class<T>): T? {
    /*return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getSerializableExtra(name, clazz)
    } else {
        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        this.getSerializableExtra(name) as T
    }*/
    return this.getSerializableExtra(name) as T
}

public fun <T : Parcelable?> Intent.getParcelable(name: String, clazz: Class<T>): T? {
    /*return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(name, clazz)
    } else {
        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        this.getParcelableExtra<T>(name)
    }*/
    return this.getParcelableExtra<T>(name)
}

public inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}

public inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

public inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

public inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}
