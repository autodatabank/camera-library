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

	// 촬영음 Mute 가능 여부 요청용.
	const val EXTRA_CAN_MUTE = "EXTRA_HAS_MUTE"

	// 촬영 시 미리보기에 화면에 표시 할 마스크(백분융) 크기 요청용.
	const val EXTRA_CROP_PERCENT = "EXTRA_MASK_PERCENT"

	// 촬영 시 UI 로테이션 금지(Image Exif값에는 영향 없음) 요청용.
	const val EXTRA_CAN_UI_ROTATION = "EXTRA_CAN_UI_ROTATION"

	// 한 장 촬영 시 width 반환용.
	const val EXTRA_WIDTH = "EXTRA_WIDTH"

	// 한 장 촬영 시 height 반환용.
	const val EXTRA_HEIGHT = "EXTRA_HEIGHT"

	// 여러 장 촬영 시 uris 반환용.
	const val EXTRA_URIS = "EXTRA_URIS"

	// 여러 장 촬영 시 sizes 반환용.
	const val EXTRA_SIZES = "EXTRA_SIZES"
}