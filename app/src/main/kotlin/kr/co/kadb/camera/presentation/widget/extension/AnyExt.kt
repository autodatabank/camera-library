package kr.co.kadb.camera.presentation.widget.extension

import com.google.gson.GsonBuilder
import timber.log.Timber

/**
 * Created by oooobang on 2018. 1. 31..
 */

internal fun Any?.fieldReflection(obj: Any?) {
    try {
        obj?.javaClass?.declaredFields?.forEach { outerField ->
            this?.javaClass?.declaredFields?.forEach { innerField ->
                if (outerField.name == innerField.name) {
                    innerField.isAccessible = true
                    innerField.set(this.javaClass.newInstance(), outerField.get(outerField.name))

                    // Debug.
                    Timber.d("outerField.name => ${outerField.name}, innerField.name => ${outerField.name}")
                }
            }
        }
    } catch (ex: Exception) {}
}

// Json Pretty.
internal fun Any?.toJsonPretty(): String {
    return GsonBuilder().setPrettyPrinting().create().toJson(this)
}