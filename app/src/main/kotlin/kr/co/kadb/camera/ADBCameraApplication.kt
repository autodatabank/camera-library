package kr.co.kadb.camera

import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application.
 * Created by oooobang on 2022. 07. 06.
 */
@HiltAndroidApp
internal class ADBCameraApplication: MultiDexApplication() {
	override fun onCreate() {
		super.onCreate()

		// Vector Resource.
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

		if (BuildConfig.DEBUG) {
			Timber.plant(Timber.DebugTree())
		}
	}
}
