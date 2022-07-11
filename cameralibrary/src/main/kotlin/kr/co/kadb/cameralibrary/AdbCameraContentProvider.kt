package kr.co.kadb.cameralibrary

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import timber.log.Timber

class AdbCameraContentProvider: ContentProvider() {
    override fun onCreate(): Boolean {
        // Vector Resource.
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        // Timber.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        return false
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }
}