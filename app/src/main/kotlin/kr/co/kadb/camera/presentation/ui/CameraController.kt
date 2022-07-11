package kr.co.kadb.camera.presentation.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kr.co.kadb.camera.presentation.ui.shoot.ShootActivity
import timber.log.Timber
import java.lang.ref.WeakReference

/**
 * Created by oooobang on 2020. 2. 3..
 * Controller.
 */
internal class CameraController {
//    private val resultLauncher: ActivityResultLauncher<Intent>
//
//    constructor(activity: Activity) {
//
//    }
//
//    constructor(activity: AppCompatActivity) {
//        resultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
////                // Debug.
////                Timber.i(">>>>> RESULT : %s", result.data.toJsonPretty())
////                Timber.i(">>>>> RESULT : %s", result.data?.data)
////                Timber.i(">>>>> RESULT : %s", result.data?.extras?.get("data"))
////                Timber.i(">>>>> RESULT : %s", contentUri)
//                //val bitmap = result.data?.extras?.get("data") as? Bitmap
//                //binding.photoviewStandbyScreen.setImageURI(contentUri)
//            }
//        }
//    }
//
//    // Activity Reference.
//    //val activityRef: WeakReference<AppCompatActivity>
//
//    // 촬영.
//    fun navigateToShoot() {
//        val intent = Intent(activity, ShootActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
//        //intent.putExtra(EXTRA_PHOTO_URI, path)
//        activity.startActivity(intent)
//    }

//    // ContainerId.
//    private val containerId: Int = R.id.layout_container

//    // 촬영.
//    fun navigateToShooting(extraTo: String?) {
//        val fragment = ShootFragment.create(extraTo)
//        fragmentManager.beginTransaction()
//            .replace(containerId, fragment)
//            .commitAllowingStateLoss()
//    }
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