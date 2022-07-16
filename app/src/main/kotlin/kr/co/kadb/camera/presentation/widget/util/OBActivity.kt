package kr.co.kadb.camera.presentation.widget.util

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import timber.log.Timber

/**
 * Created by oooobang on 2018. 3. 7..
 * Activity.
 */
internal abstract class OBActivity : AppCompatActivity() {
	/**
	 * Activity가 생성 될 때 마다(초기 생성 및 재 생성) 호출.
	 */
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		/*
		 * 액티비티를 초기화 한다.
		 */

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onCreate <")
		//==================================================
	}

	/**
	 * onCreate가 종료된 후 호출된다. UI 상태 복구에 사용된다.
	 */
	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		/*
		 * savedInstanceState를 이용해 UI 상태를 복구한다.
		 * 이 번들은 onCreate에도 전달된다.
		 */

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onRestoreInstanceState <")
		//==================================================
	}

	override fun onNewIntent(intent: Intent) {
		super.onNewIntent(intent)

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onNewIntent <")
		//==================================================
	}

	/**
	 * Activity를 화면에 표시할 수 있는 상태로 만들기 위한 callback.
	 */
	override fun onStart() {
		super.onStart()
		/*
		 * Activity가 화면에 보이므로, 필요한 모든 UI변경 사항을 적용한다.
		 */

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onStart <")
		//==================================================
	}

	/**
	 * Activity가 Stop 상태에서 다시 Running 상태로 복귀할 때 처음 호출.
	 * 이 메소드 완료 후 Activity가 Foreground에 표시되면 onResume(), 이 외 onStop() 호출.
	 */
	override fun onRestart() {
		super.onRestart()
		/*
		 * Activity가 이미 화면에 보이고 있다고 생각하고 변경된 내용을 읽어 들인다.
		 */

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onRestart <")
		//==================================================
	}

	/**
	 * Activity가 Foreground에 표시되고 User와 Interract 할 수 있는 상태 전까지 호출.
	 */
	override fun onResume() {
		super.onResume()
		/*
		 * 일시 중지된 UI 업데이트나 스레드,
		 * 혹은 Activity가 비활성화 되면서 잠시 중단됐던 처리를모두 재개한다.
		 */

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onResume <")
		//==================================================
	}

	override fun onWindowFocusChanged(hasFocus: Boolean) {
		super.onWindowFocusChanged(hasFocus)
		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onWindowFocusChanged($hasFocus) <")
		//==================================================
	}

	/**
	 *
	 */
	override fun startActivity(intent: Intent) {
		super.startActivity(intent)
		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} startActivity <")
		//==================================================
	}

	/**
	 *
	 */
	override fun onChildTitleChanged(childActivity: Activity, title: CharSequence) {
		super.onChildTitleChanged(childActivity, title)
		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onChildTitleChanged <")
		//==================================================
	}

	/**
	 *
	 */
	override fun onPostCreate(savedInstanceState: Bundle?) {
		super.onPostCreate(savedInstanceState)
		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onPostCreate <")
		//==================================================
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onCreateOptionsMenu <")
		//==================================================

		return super.onCreateOptionsMenu(menu)
	}

	override fun onPrepareOptionsMenu(menu: Menu): Boolean {
		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onPrepareOptionsMenu <")
		//==================================================

		return super.onCreateOptionsMenu(menu)
	}

	/**
	 * 활성 수명이 끝날 때, UI 상태 변화를 저장하기 위해 호출된다.
	 */
	override fun onSaveInstanceState(savedInstanceState: Bundle) {
		super.onSaveInstanceState(savedInstanceState)
		/*
		 * UI 상태 변화를 savedInstanceState에 저장한다.
		 * 프로세스가 종료되고 재시작 될 경우 이 번들이 onCreate에 전달될 것이다.
		 */

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onSaveInstanceState <")
		//==================================================
	}

	/**
	 * A Activity가 새로운 B Activity에 의해 포커스를 잃었을 떄 호출.
	 */
	override fun onPause() {
		super.onPause()
		/*
		 * Activity가 활성 상태의 포그라운드 Activity가 아닐 경우
		 * 업데이트할 필요가 없는 UI 업데이트나 스레드, 혹은 CPU 사용량이 많은 처리를 일시 중단한다.
		 */

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onPause <")
		//==================================================
	}

	/**
	 * Activity 사용자에게 전혀 보이지 않게 되면 호출.
	 */
	override fun onStop() {
		super.onStop()
		/*
		 * 남아 있는 UI 업데이트나 스레드 혹은 Activity가 화면에 보이지 않을 때 필요치 않은 처리를 일시 중단한다.
		 * 이 메서드가 호출되고 난 뒤에는 프로세스가 종료될 가능성이 있으므로 바뀐 모든 내용과 상태 변화를 지속시킨다.
		 */

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onStop <")
		//==================================================
	}

	override fun onUserInteraction() {
		super.onUserInteraction()

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onUserInteraction <")
		//==================================================
	}

	override fun onUserLeaveHint() {
		super.onUserLeaveHint()

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onUserLeaveHint <")
		//==================================================
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onConfigurationChanged <")
		//==================================================
	}
//
//	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
//		super.onActivityResult(requestCode, resultCode, data)
//
//		//==================================================
//		Timber.v("=========================> " + javaClass.simpleName + " onActivityResult <")
//		//==================================================
//	}

	override fun onLowMemory() {
		super.onLowMemory()

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onLowMemory <")
		//==================================================
	}

	override fun onBackPressed() {
		super.onBackPressed()

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onBackPressed <")
		//==================================================
	}

	/**
	 * finish() 호출 등으로 Activity가 종료될때 호출.
	 */
	override fun onDestroy() {
		super.onDestroy()
		/*
		 * 스레드를 종료하고, 데이터베이스 연결을 닫는 등 모든 리소스를 해제한다.
		 */

		//==================================================
		Timber.v("=========================> ${javaClass.simpleName} onDestroy <")
		//==================================================
	}
}