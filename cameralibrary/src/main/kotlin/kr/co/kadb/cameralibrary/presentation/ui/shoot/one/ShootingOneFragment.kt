//package kr.co.kadb.cameralibrary.presentation.ui.shoot.one
//
//import androidx.lifecycle.ViewModelProvider
//import android.os.Bundle
//import android.view.View
//import kr.co.kadb.cameralibrary.R
//import kr.co.kadb.cameralibrary.presentation.base.BaseBindingFragment
//import kr.co.kadb.cameralibrary.ui.shooting.ShootController
//import javax.inject.Inject
//
///**
// * Created by oooobang on 2020. 2. 13..
// * 촬영 결과.
// */
//internal class ShootingOneFragment : BaseBindingFragment<FragmentShootingOneBinding>(), Injectable {
//	companion object {
//		private const val PATHS = "paths"
//		fun create(paths: Array<String>?): ShootingOneFragment {
//			val fragment = ShootingOneFragment()
//			val bundle = Bundle()
//			bundle.putStringArray(PATHS, paths)
//			fragment.arguments = bundle
//			return fragment
//		}
//	}
//
//	@Inject
//	lateinit var viewController: ShootController
//
//	@Inject
//	lateinit var viewModelFactory: ViewModelProvider.Factory
//
//	// ViewModel.
//	private val viewModel: ShootingOneViewModel by viewModels {
//		viewModelFactory
//	}
//
//	// Fragment Layout.
//	override val layoutResourceId: Int = R.layout.fragment_shooting_one
//
//	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//		super.onViewCreated(view, savedInstanceState)
//
//		// Binding.
//		binding.viewModel = viewModel
//		binding.lifecycleOwner = viewLifecycleOwner
//
//		// Setup.
//		setupLayout()
//		setupObserver()
//		setupListener()
//		setupCallback()
//
//		// 이미지 패스.
//		viewModel.paths.value = arguments?.getStringArray(PATHS)
//		viewModel.paths.value?.firstOrNull()?.let {
//			binding.path = it
//		}
//	}
//
//	// Setup Layout.
//	private fun setupLayout() {
//	}
//
//	// Setup Observer.
//	private fun setupObserver() {
//	}
//
//	// Setup Listener.
//	private fun setupListener() {
//		// 이전.
//		binding.imagebuttonBack.setOnClickListener {
//			viewController.navigateToPopBackStack()
//		}
//
//		// 촬영종료.
//		binding.buttonShootingFinish.setOnClickListener {
//			viewController.finish()
//		}
//
//		// 저장.
//		binding.buttonSave.setOnClickListener {
//			// 선택사진, 사진설명, 사진구분.
//			val paths = viewModel.paths.value
//			val photoKind = PhotoType.KIND_REGISTRATION.value
//			if (paths?.isNotEmpty() == true) {
//				viewController.navigateToUploadDialog(paths, null, photoKind) { isSuccess, exception ->
//					if (isSuccess) {
//						// 사진 전송 요청이 정상 처리 되었습니다.
//						showSuccessToast(R.string.adb_cameralibrary_text_send_photo_was_successfully)
//						// 촬영종료.
//						viewController.finish()
//					} else {
//						// 사진 전송 요청에 실패하였습니다.\n[%s]
//						showErrorToast(getString(R.string.adb_cameralibrary_text_send_photo_was_failed_string, exception))
//					}
//				}
//			} else {
//				showAlert(R.string.adb_cameralibrary_text_please_select_an_item)
//			}
//		}
//	}
//
//	// Setup Callback
//	private fun setupCallback() {
//	}
//}