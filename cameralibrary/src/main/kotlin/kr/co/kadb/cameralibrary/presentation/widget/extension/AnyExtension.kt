package kr.co.kadb.cameralibrary.presentation.widget.extension

import com.google.gson.GsonBuilder

/**
 * Any Extension.
 */
// Json.
internal fun Any?.toJsonString(): String = GsonBuilder().create().toJson(this)
// Json Pretty.
internal fun Any?.toJsonPretty(): String = GsonBuilder().setPrettyPrinting().create().toJson(this)