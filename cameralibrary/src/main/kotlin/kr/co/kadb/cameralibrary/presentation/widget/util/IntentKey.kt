package kr.co.kadb.cameralibrary.presentation.widget.util

public object IntentKey {
	// 한장 촬영.
	public const val ACTION_TAKE_PICTURE: String = "kr.co.kadb.cameralibrary.ACTION_TAKE_PICTURE"

	// 여러장 촬영.
	public const val ACTION_TAKE_MULTIPLE_PICTURES: String = "kr.co.kadb.cameralibrary.ACTION_TAKE_MULTIPLE_PICTURES"

	// 차량번호 촬영.
	internal const val ACTION_DETECT_VEHICLE_NUMBER_IN_PICTURES: String = "kr.co.kadb.cameralibrary.ACTION_DETECT_VEHICLE_NUMBER_IN_PICTURES"

	// 주행거리 촬영.
	public const val ACTION_DETECT_MILEAGE_IN_PICTURES: String = "kr.co.kadb.cameralibrary.ACTION_DETECT_MILEAGE_IN_PICTURES"

	// 차대번호 촬영.
	public const val ACTION_DETECT_VIN_NUMBER_IN_PICTURES: String = "kr.co.kadb.cameralibrary.ACTION_DETECT_VIN_NUMBER_IN_PICTURES"

	// 정비명세서 촬영.
	public const val ACTION_DETECT_MAINTENANCE_STATEMENT_IN_PICTURES: String = "kr.co.kadb.cameralibrary.ACTION_DETECT_MAINTENANCE_STATEMENT_IN_PICTURES"

	// 종료.
	internal const val BROADCAST_FINISH: String = "kr.co.kadb.cameralibrary.finish"

	//
	internal const val BROADCAST_EVENT: String = "kr.co.kadb.cameralibrary.event"

	//
	internal const val BROADCAST_EVENT_ACTIVATED: String = "kr.co.kadb.cameralibrary.event_activated"

	// event.
	internal const val EXTRA_EVENT: String = "EXTRA_EVENT"

	// from.
	internal const val EXTRA_FROM: String = "EXTRA_FROM"

	// to.
	internal const val EXTRA_TO: String = "EXTRA_TO"

	// data.
	internal const val EXTRA_DATA: String = "data"

	// 촬영 디버그 모드 요청용.
	public const val EXTRA_IS_DEBUG_MODE: String = "EXTRA_IS_DEBUG_MODE"

	// 촬영음 Mute 가능 여부 요청용.
	public const val EXTRA_CAN_MUTE: String = "EXTRA_CAN_MUTE"

	// 촬영 시 수평선 표시 요청용.
	public const val EXTRA_HAS_HORIZON: String = "EXTRA_HAS_HORIZON"

	// 촬영 후 크롭 이미지 저장(원본 이미지는 저장 안함) 요청용.
	public const val EXTRA_IS_SAVE_CROPPED_IMAGE: String = "EXTRA_IS_SAVE_CROPPED_IMAGE"

	// 촬영 시 미리보기 화면에 표시 할 마스크(백분율) 크기 요청용.
	@Deprecated(
		message = "직관성을 위하여 Deprecated." +
				"따라서 EXTRA_CROP_SIZE 사용하세요.",
		level = DeprecationLevel.WARNING
	)
	public const val EXTRA_CROP_PERCENT: String = "EXTRA_CROP_PERCENT"

	// 촬영 시 미리보기 화면에 표시 할 마스크(백분율) 크기 요청용.
	public const val EXTRA_CROP_SIZE: String = "EXTRA_CROP_SIZE"

	// 촬영 시 UI 로테이션 금지(Image Exif값에는 영향 없음) 요청용.
	public const val EXTRA_CAN_UI_ROTATION: String = "EXTRA_CAN_UI_ROTATION"

	// 촬영 시 수평선 Color 요청용.
	public const val EXTRA_HORIZON_COLOR: String = "EXTRA_HORIZON_COLOR"

	// 촬영 시 미리보기 화면에 표시 할 마스크(백분율) Border Color 요청용.
	public const val EXTRA_CROP_BORDER_COLOR: String = "EXTRA_CROP_BORDER_COLOR"

	// 촬영 후 이미지 저장 시 JPEG 품질(1~100) 요청용.
	public const val EXTRA_CROPPED_JPEG_QUALITY: String = "EXTRA_CROPPED_JPEG_QUALITY"

	// 한 장 촬영 시 width 반환용.
	public const val EXTRA_WIDTH: String = "EXTRA_WIDTH"

	// 한 장 촬영 시 height 반환용.
	public const val EXTRA_HEIGHT: String = "EXTRA_HEIGHT"

	// 한 장 촬영 시 rotation 반환용.
	public const val EXTRA_ROTATION: String = "EXTRA_ROTATION"

	// 여러 장 촬영 시 uri 반환용.
	public const val EXTRA_URIS: String = "EXTRA_URIS"

	// 여러 장 촬영 시 size 반환용.
	public const val EXTRA_SIZES: String = "EXTRA_SIZES"

	// 여러 장 촬영 시 rotation 반환용.
	public const val EXTRA_ROTATIONS: String = "EXTRA_ROTATIONS"

	// 감지 텍스트 반환용.
	public const val EXTRA_DETECT_TEXT: String = "EXTRA_DETECT_TEXT"

	// 감지 Rect 반환용.
	public const val EXTRA_DETECT_RECT: String = "EXTRA_DETECT_RECT"
}