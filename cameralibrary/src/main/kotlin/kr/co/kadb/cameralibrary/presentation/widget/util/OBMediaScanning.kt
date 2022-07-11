@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.util

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import timber.log.Timber
import java.io.File

/**
 * Created by oooobang on 2018. 4. 10..
 */
internal class OBMediaScanning(context: Context,
					  private val targetFile: File) : MediaScannerConnection.MediaScannerConnectionClient {
	private val mConnection: MediaScannerConnection = MediaScannerConnection(context, this)

	init {
		mConnection.connect()
	}

	override fun onMediaScannerConnected() {
		mConnection.scanFile(targetFile.absolutePath, null)
	}

	override fun onScanCompleted(path: String?, uri: Uri?) {
		mConnection.disconnect()

		// Debug.
		Timber.i("OBMediaScanning URI => %s", uri)
		Timber.i("OBMediaScanning PATH => %s", path)
	}
}