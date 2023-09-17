package kr.co.kadb.cameralibrary.presentation.model

import android.net.Uri
import android.util.Size

internal data class Image(
    val path: String = "",
    val uri: Uri = Uri.EMPTY,
    val size: Size = Size(0, 0)
)
