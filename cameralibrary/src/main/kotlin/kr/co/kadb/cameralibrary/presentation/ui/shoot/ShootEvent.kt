package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.util.Size

// Event.
sealed class ShootEvent {
    data class PlayShutterSound(val canMute: Boolean) : ShootEvent()
    data class TakePicture(
        val uri: Uri, val size: Size, val rotation: Int, val thumbnailBitmap: Bitmap?
    ) : ShootEvent()

    data class TakeMultiplePictures(
        val uris: ArrayList<Uri>, val sizes: ArrayList<Size>, val rotations: ArrayList<Int>
    ) : ShootEvent()

    data class DetectInImage(
        val text: String? = null,
        val rect: RectF? = null,
        val uri: Uri? = null,
        val size: Size? = null,
        val rotation: Int? = null,
        val thumbnailBitmap: Bitmap? = null
    ) : ShootEvent()
}