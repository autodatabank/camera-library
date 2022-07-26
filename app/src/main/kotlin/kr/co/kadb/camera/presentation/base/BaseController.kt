package kr.co.kadb.camera.presentation.base

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kr.co.kadb.camera.R
import kr.co.kadb.camera.presentation.widget.extension.negativeButton
import kr.co.kadb.camera.presentation.widget.extension.positiveButton
import kr.co.kadb.camera.presentation.widget.extension.showAlert
import kr.co.kadb.camera.presentation.widget.extension.toJsonPretty
import timber.log.Timber

/**
 * Created by oooobang on 2018. 2. 28..
 * Base Controller.
 */
internal open class BaseController constructor(activityContext: Context) {
    // Activity.
    internal val activity = activityContext as AppCompatActivity

    // FragmentManager.
    internal val fragmentManager = (activityContext as AppCompatActivity).supportFragmentManager

    private var resultDetailsSettings: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Activity.RESULT_OK
            // Debug.
            Timber.i(">>>>> RESULT : %s", result.resultCode)
            Timber.i(">>>>> RESULT : %s", result.data.toJsonPretty())
            Timber.i(">>>>> RESULT : %s", result.data?.data)
            Timber.i(">>>>> RESULT : %s", result.data?.extras?.get("data"))
        }

    // BackStack.
    fun navigateToBackStack() {
        if (fragmentManager.fragments.size > 1) {
            fragmentManager.popBackStack()
        } else {
            activity.finish()
        }
    }

    // BackStack.
    fun navigateToPopBackStack() {
        fragmentManager.popBackStack()
    }

    // finish.
    fun finish() {
        activity.finish()
    }

    // 외부저장장치 읽기/쓰기 권한.
    fun requestStoragePermission(
        errorAction: (() -> Unit)? = null,
        deniedAction: ((Boolean) -> Unit)? = null,
        grantedAction: (() -> Unit)? = null
    ) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        Dexter.withContext(activity)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    // 허용됨.
                    if (report?.areAllPermissionsGranted() == true) {
                        grantedAction?.invoke()
                    }

                    // 영구 거부.
                    if (report?.isAnyPermissionPermanentlyDenied == true) {
                        showSettingsDialog(
                            R.string.text_adb_camera_this_app_needs_permission,
                            deniedAction
                        )
                    }
                }

                // 필요 이유.
                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener {
                activity.showAlert {
                    setTitle(R.string.text_adb_camera_error)
                    setMessage(R.string.text_adb_camera_error_permission)
                    positiveButton {
                        errorAction?.invoke()
                    }
                }
            }
            .onSameThread()
            .check()
    }

    // 카메라 권한.
    fun requestCameraPermission(
        errorAction: (() -> Unit)? = null,
        deniedAction: ((Boolean) -> Unit)? = null,
        grantedAction: (() -> Unit)? = null
    ) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        } else {
            listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        Dexter.withContext(activity)
            .withPermissions(permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    // 허용됨.
                    if (report?.areAllPermissionsGranted() == true) {
                        grantedAction?.invoke()
                    }

                    // 영구 거부.
                    if (report?.isAnyPermissionPermanentlyDenied == true) {
                        showSettingsDialog(
                            R.string.text_adb_camera_this_app_needs_permission,
                            deniedAction
                        )
                    }
                }

                // 필요 이유.
                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener {
                activity.showAlert {
                    setTitle(R.string.text_adb_camera_error)
                    setMessage(R.string.text_adb_camera_error_permission)
                    positiveButton {
                        errorAction?.invoke()
                    }
                }
            }
            .onSameThread()
            .check()
    }

    // 설정 안내 팝업.
    private fun showSettingsDialog(
        @StringRes messageId: Int = R.string.text_adb_camera_this_app_needs_permission,
        action: ((Boolean) -> Unit)? = null
    ) {
        activity.showAlert {
            setTitle(R.string.text_adb_camera_permission_title)
            setMessage(messageId)
            positiveButton(R.string.text_adb_camera_goto_settings) {
                navigateToSystemSettings()
                action?.invoke(true)
            }
            negativeButton(R.string.text_adb_camera_cancel) {
                action?.invoke(false)
            }
        }
    }

    // 시스템 설정.
    private fun navigateToSystemSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts(
            "package",
            activity.applicationContext.packageName,
            null
        )
        resultDetailsSettings.launch(intent)
        //activity.startActivity(intent)
    }
}
