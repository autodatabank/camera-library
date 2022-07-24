package kr.co.kadb.cameralibrary.presentation.widget.util

object IntentKey {
	// 한장 촬영.
	const val ACTION_TAKE_PICTURE = "kr.co.kadb.cameralibrary.ACTION_TAKE_PICTURE"

	// 여러장 촬영.
	const val ACTION_TAKE_MULTIPLE_PICTURE = "kr.co.kadb.cameralibrary.ACTION_TAKE_MULTIPLE_PICTURE"

	// 종료.
	internal const val BROADCAST_FINISH = "kr.co.kadb.cameralibrary.finish"

	//
	internal const val BROADCAST_EVENT = "kr.co.kadb.cameralibrary.event"

	//
	internal const val BROADCAST_EVENT_ACTIVATED = "kr.co.kadb.cameralibrary.event_activated"

	// event.
	internal const val EXTRA_EVENT = "EXTRA_EVENT"

	// from.
	internal const val EXTRA_FROM = "EXTRA_FROM"

	// to.
	internal const val EXTRA_TO = "EXTRA_TO"

	// data.
	internal const val EXTRA_DATA = "data"

	// hsa mute.
	const val EXTRA_HAS_MUTE = "EXTRA_HAS_MUTE"

	// rotation.
	const val EXTRA_ROTATION = "EXTRA_ROTATION"

	// uris.
	const val EXTRA_URIS = "EXTRA_URIS"

	// width.
	const val EXTRA_WIDTH = "EXTRA_WIDTH"

	// height.
	const val EXTRA_HEIGHT = "EXTRA_HEIGHT"

	// sizes.
	const val EXTRA_SIZES = "EXTRA_SIZES"
}