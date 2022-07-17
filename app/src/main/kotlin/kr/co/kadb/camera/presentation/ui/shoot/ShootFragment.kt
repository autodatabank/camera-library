package kr.co.kadb.camera.presentation.ui.shoot

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kr.co.kadb.camera.R
import kr.co.kadb.camera.data.local.PreferenceManager
import kr.co.kadb.camera.databinding.FragmentShootBinding
import kr.co.kadb.camera.presentation.base.BaseBindingFragment
import kr.co.kadb.camera.presentation.widget.extension.toJsonPretty
import kr.co.kadb.cameralibrary.presentation.widget.event.IntentAction
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by oooobang on 2022. 7. 17..
 * 촬영.
 */
@AndroidEntryPoint
internal class ShootFragment : BaseBindingFragment<FragmentShootBinding, ShootViewModel>() {
    companion object {
        fun create() = ShootFragment()
    }

    @Inject
    lateinit var preferences: PreferenceManager

    @Inject
    lateinit var viewController: ShootController

    // ViewModel.
    override val viewModel: ShootViewModel by viewModels()

    // Fragment Layout.
    override val layoutResourceId: Int = R.layout.fragment_shoot

    private var resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Debug.
                Timber.i(">>>>> RESULT : %s", result.data.toJsonPretty())
                Timber.i(">>>>> RESULT : %s", result.data?.data)
                Timber.i(">>>>> RESULT : %s", result.data?.extras?.toJsonPretty())
                Timber.i(">>>>> RESULT : %s", result.data?.extras?.get("data"))
                Timber.i(">>>>> RESULT : %s", result.data?.extras?.get(MediaStore.EXTRA_OUTPUT))
            }
        }

    override fun initVariable() {
    }

    // Init Layout.
    override fun initLayout() {
    }

    // Init Observer.
    override fun initObserver() {
    }

    // Init Listener.
    override fun initListener() {
        // 촬영.
        binding.buttonShooting.setOnClickListener {
        }

        binding.buttonFlash.setOnClickListener {
//            Intent(IntentAction.ACTION_TAKE_PICTURE).also { takePictureIntent ->
//                activity?.startActivity(takePictureIntent)
//            }

            Intent(IntentAction.ACTION_TAKE_PICTURE).also { takePictureIntent ->
//                takePictureIntent.putextra
                //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
                resultLauncher.launch(takePictureIntent)
//                // TODO: dependencies
//                // implementation 'androidx.activity:activity-ktx:version'
//                // implementation 'androidx.fragment:fragment-ktx:version'
////                requireActivity().activityResultRegistry
//
//
////                registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { result ->
////                        // Debug.
////                        Timber.i(">>>>> RESULT : %s", result)
//////                    if (result.resultCode == Activity.RESULT_OK) {
//////                        // Debug.
//////                        Timber.i(">>>>> RESULT : %s", result.data.toJsonPretty())
//////                        Timber.i(">>>>> RESULT : %s", result.data?.data)
//////                        Timber.i(">>>>> RESULT : %s", result.data?.extras?.get("data"))
//////                    }
////                }.launch(takePictureIntent)
//
//
//                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                    if (result.resultCode == Activity.RESULT_OK) {
//                        // Debug.
//                        Timber.i(">>>>> RESULT : %s", result.data.toJsonPretty())
//                        Timber.i(">>>>> RESULT : %s", result.data?.data)
//                        Timber.i(">>>>> RESULT : %s", result.data?.extras?.get("data"))
//                    }
//                }.launch(takePictureIntent)
////                registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback { result ->
////                    if (result.resultCode == Activity.RESULT_OK) {
////                        // Debug.
////                        Timber.i(">>>>> RESULT : %s", result.data.toJsonPretty())
////                        Timber.i(">>>>> RESULT : %s", result.data?.data)
////                        Timber.i(">>>>> RESULT : %s", result.data?.extras?.get("data"))
////                    }
////                }).launch(takePictureIntent)
            }
        }
    }

    // Init Callback.
    override fun initCallback() {
    }
}