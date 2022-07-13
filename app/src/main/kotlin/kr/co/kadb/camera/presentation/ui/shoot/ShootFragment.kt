package kr.co.kadb.camera.presentation.ui.shoot

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kr.co.kadb.camera.R
import kr.co.kadb.camera.data.local.PreferenceManager
import kr.co.kadb.camera.databinding.FragmentShootBinding
import kr.co.kadb.camera.presentation.base.BaseBindingFragment
import kr.co.kadb.cameralibrary.presentation.widget.event.IntentAction
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by oooobang on 2020. 2. 3..
 * 촬영.
 */
@AndroidEntryPoint
internal class ShootFragment : BaseBindingFragment<FragmentShootBinding, ShootViewModel>() {
    companion object {
        fun create(extraTo: String?): ShootFragment {
            val fragment = ShootFragment()
            val bundle = Bundle()
            //bundle.putString(EXTRA_TO, extraTo)
            fragment.arguments = bundle
            return fragment
        }
    }

    @Inject
    lateinit var preferences: PreferenceManager

    @Inject
    lateinit var viewController: ShootController

    // ViewModel.
    override val viewModel: ShootViewModel by viewModels()

    //
    private var extraTo: String? = null

    // Fragment Layout.
    override val layoutResourceId: Int = R.layout.fragment_shoot
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
////
////        // Binding.
////        binding.viewModel = viewModel
////
////        // 이미지 패스.
////        extraTo = arguments?.getString(EXTRA_TO)
////
////        // CameraView Lifecycle Owner.
////        binding.cameraview.setLifecycleOwner(viewLifecycleOwner)
//    }

    override fun initVariable() {
    }

    // Init Layout.
    override fun initLayout() {
        // Debug.
        Timber.i(">>>>> initLayout!!!!!")
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
            Intent(IntentAction.ACTION_ADB_CAMERA).also { imageCaptureIntent ->
                activity?.startActivity(imageCaptureIntent)
            }
        }
//
//        // 선택.
//        binding.buttonSelect.setOnClickListener {
//            viewController.navigateToShootingMany(viewModel.paths)
//        }
    }

    // Init Callback.
    override fun initCallback() {
//        // 촬영 후 결과.
//        binding.cameraview.onPictureTaken { result ->
//            viewModel.saveWithByte(result.data) {
//                // 차량등록증 촬영 시 사진 결과 화면으로...
//                if (extraTo == TO_SHOOTING_REGISTRATION_CARD) {
//                    viewController.navigateToShootingOne(viewModel.paths)
//                }
//            }
//        }
    }
}