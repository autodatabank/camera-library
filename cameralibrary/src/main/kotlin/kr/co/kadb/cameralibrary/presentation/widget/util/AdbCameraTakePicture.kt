package kr.co.kadb.cameralibrary.presentation.widget.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import kr.co.kadb.cameralibrary.presentation.widget.event.IntentAction

class AdbCameraTakePicture : ActivityResultContract<Uri, Bitmap?>() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return Intent(IntentAction.ACTION_TAKE_PICTURE)
            .putExtra("input", input)
    }

    override fun getSynchronousResult(context: Context, input: Uri): SynchronousResult<Bitmap?>? {
        return null
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Bitmap? {
        return if (intent == null || resultCode != Activity.RESULT_OK)
            null
        else
            intent.getParcelableExtra("data")
    }
}