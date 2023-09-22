package kr.co.kadb.cameralibrary.presentation.widget.mlkit

import android.graphics.RectF
import java.io.Serializable

internal data class DetectedItem constructor(var text: String, var rect: RectF) : Serializable