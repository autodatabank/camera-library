package kr.co.kadb.cameralibrary.presentation.ui.shoot

import android.content.Context
import kr.co.kadb.cameralibrary.R
import kr.co.kadb.cameralibrary.presentation.base.BaseController

/**
 * Created by oooobang on 2020. 2. 3..
 * Controller.
 */
internal class ShootController
constructor(activityContext: Context) : BaseController(activityContext) {
	// ContainerId.
	private val containerId: Int = R.id.layout_container

	// 촬영.
	fun navigateToShooting() {
		val fragment = ShootFragment.create()
		fragmentManager.beginTransaction()
				.replace(containerId, fragment)
				.commitAllowingStateLoss()
	}
//
//	// 한장 촬영 결과.
//	fun navigateToShootingOne(paths: List<String>?) {
//		val fragment = ShootingOneFragment.create(paths?.toTypedArray())
//		val tag = "ShootingOneFragment"
//		fragmentManager.beginTransaction()
//				.setCustomAnimations(android.R.anim.slide_in_left,
//						android.R.anim.slide_out_right,
//						android.R.anim.slide_in_left,
//						android.R.anim.slide_out_right)
//				.replace(containerId, fragment, tag)
//				.addToBackStack(null)
//				.commitAllowingStateLoss()
//	}
//
//	// 여러장 촬영 결과.
//	fun navigateToShootingMany(paths: List<String>?) {
//		val fragment = ShootingManyFragment.create(paths?.toTypedArray())
//		val tag = "ShootingManyFragment"
//		fragmentManager.beginTransaction()
//				.setCustomAnimations(android.R.anim.slide_in_left,
//						android.R.anim.slide_out_right,
//						android.R.anim.slide_in_left,
//						android.R.anim.slide_out_right)
//				.replace(containerId, fragment, tag)
//				.addToBackStack(null)
//				.commitAllowingStateLoss()
//	}
//
//	// 뷰어.
//	fun navigateToViewer(path: String) {
//		val intent = Intent(activity, ViewerActivity::class.java)
//		intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
//		intent.putExtra(EXTRA_PHOTO_URI, path)
//		activity.startActivity(intent)
//	}
//
//	// 업로드 Dialog.
//	fun navigateToUploadDialog(paths: Array<String>,
//							   names: Array<String>? = null,
//							   photoKind: String?,// = PhotoType.KIND_VEHICLE.value,
//							   photoType: String? = null,// = PhotoType.BEFORE_REPAIR.value,
//							   action: ((Boolean, String?) -> Unit)? = null) {
//		// 사진 저장.
//		val baseUrl = "${preferences.host}/mob/auto10_photo_c.aspx"
//		val request = RQPhotoC(preferences.companyCode,
//				preferences.mobileNumber,
//				EstimateLiveData.instance.value?.serial,
//				photoKind = photoKind,
//				photoType = photoType,
//				vehicleNumber = EstimateLiveData.instance.value?.vehicleNumber)
//
//		// Debug.
//		Timber.i("UPLOAD PHOTOS ---------- %s", request.toJson())
//
//		// 업로드.
//		val dialogFragment = UploadDialogFragment.create(
//				baseUrl,
//				request.toJson(),
//				paths,
//				names,
//				true)
//		dialogFragment.show(fragmentManager, "uploadDialogFragment")
//		fragmentManager.executePendingTransactions()
//		dialogFragment.completedListener = object : DialogOnCompletedListener {
//			override fun onCompleted(isCompleted: Boolean, other: Any?) {
//				action?.invoke(isCompleted, other as? String)
//			}
//		}
//	}
}