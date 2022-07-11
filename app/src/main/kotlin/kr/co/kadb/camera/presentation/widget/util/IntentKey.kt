package kr.co.kadb.camera.presentation.widget.util

import kr.co.kadb.camera.BuildConfig

internal object IntentKey {
	// 종료.
	const val BROADCAST_FINISH = BuildConfig.APPLICATION_ID + ".finish"

	//
	const val BROADCAST_EVENT = BuildConfig.APPLICATION_ID + ".event"

	//
	const val BROADCAST_EVENT_ACTIVATED = BuildConfig.APPLICATION_ID + ".event_activated"

	// event.
	const val EXTRA_EVENT = "EXTRA_EVENT"

	// from.
	const val EXTRA_FROM = "EXTRA_FROM"

	// to.
	const val EXTRA_TO = "EXTRA_TO"

	// data.
	const val EXTRA_DATA = "EXTRA_DATA"
}