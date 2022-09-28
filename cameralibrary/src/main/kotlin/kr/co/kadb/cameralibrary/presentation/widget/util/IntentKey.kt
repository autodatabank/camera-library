package kr.co.kadb.cameralibrary.presentation.widget.util

object IntentKey {
	// 한장 촬영.
	const val ACTION_TAKE_PICTURE = "kr.co.kadb.cameralibrary.ACTION_TAKE_PICTURE"

	// 여러장 촬영.
	const val ACTION_TAKE_MULTIPLE_PICTURES = "kr.co.kadb.cameralibrary.ACTION_TAKE_MULTIPLE_PICTURES"

	// 차량번호 촬영.
	const val ACTION_TAKE_VEHICLE_NUMBER_PICTURES = "kr.co.kadb.cameralibrary.ACTION_TAKE_VEHICLE_NUMBER_PICTURES"

	// 운행거리 촬영.
	const val ACTION_TAKE_MILEAGE_PICTURES = "kr.co.kadb.cameralibrary.ACTION_TAKE_MILEAGE_PICTURES"

	// 차대번호 촬영.
	const val ACTION_TAKE_VIN_NUMBER_PICTURES = "kr.co.kadb.cameralibrary.ACTION_TAKE_VIN_NUMBER_PICTURES"

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

	// 촬영 디버그 모드 요청용.
	const val EXTRA_IS_DEBUG_MODE = "EXTRA_IS_DEBUG_MODE"

	// 촬영음 Mute 가능 여부 요청용.
	const val EXTRA_CAN_MUTE = "EXTRA_CAN_MUTE"

	// 촬영 시 수평선 표시 요청용.
	const val EXTRA_HAS_HORIZON = "EXTRA_HAS_HORIZON"

	// 촬영 후 크롭 이미지 저장(원본 이미지는 저장 안함) 요청용.
	const val EXTRA_IS_SAVE_CROPPED_IMAGE = "EXTRA_IS_SAVE_CROPPED_IMAGE"

	// 촬영 시 미리보기 화면에 표시 할 마스크(백분율) 크기 요청용.
	@Deprecated(
		message = "직관성을 위하여 Deprecated." +
				"따라서 EXTRA_CROP_SIZE 사용하세요.",
		level = DeprecationLevel.WARNING
	)
	const val EXTRA_CROP_PERCENT = "EXTRA_CROP_PERCENT"

	// 촬영 시 미리보기 화면에 표시 할 마스크(백분율) 크기 요청용.
	const val EXTRA_CROP_SIZE = "EXTRA_CROP_SIZE"

	// 촬영 시 UI 로테이션 금지(Image Exif값에는 영향 없음) 요청용.
	const val EXTRA_CAN_UI_ROTATION = "EXTRA_CAN_UI_ROTATION"

	// 촬영 시 수평선 Color 요청용.
	const val EXTRA_HORIZON_COLOR = "EXTRA_HORIZON_COLOR"

	// 촬영 시 미리보기 화면에 표시 할 마스크(백분율) Border Color 요청용.
	const val EXTRA_CROP_BORDER_COLOR = "EXTRA_CROP_BORDER_COLOR"

	// 촬영 후 이미지 저장 시 JPEG 품질(1~100) 요청용.
	const val EXTRA_CROPPED_JPEG_QUALITY = "EXTRA_CROPPED_JPEG_QUALITY"

	// 한 장 촬영 시 width 반환용.
	const val EXTRA_WIDTH = "EXTRA_WIDTH"

	// 한 장 촬영 시 height 반환용.
	const val EXTRA_HEIGHT = "EXTRA_HEIGHT"

	// 한 장 촬영 시 rotation 반환용.
	const val EXTRA_ROTATION = "EXTRA_ROTATION"

	// 여러 장 촬영 시 uri 반환용.
	const val EXTRA_URIS = "EXTRA_URIS"

	// 여러 장 촬영 시 size 반환용.
	const val EXTRA_SIZES = "EXTRA_SIZES"

	// 여러 장 촬영 시 rotation 반환용.
	const val EXTRA_ROTATIONS = "EXTRA_ROTATIONS"
}