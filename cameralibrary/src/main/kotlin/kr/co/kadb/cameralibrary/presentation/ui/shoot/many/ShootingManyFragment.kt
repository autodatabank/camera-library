//package kr.co.kadb.cameralibrary.presentation.ui.shoot.many
//
//import androidx.lifecycle.ViewModelProvider
//import android.os.Bundle
//import android.view.View
//import androidx.fragment.app.viewModels
//import kr.co.kadb.cameralibrary.R
//import kr.co.kadb.cameralibrary.databinding.FragmentShootingManyBinding
//import kr.co.kadb.cameralibrary.extension.getResourceString
//import kr.co.kadb.cameralibrary.extension.showAlert
//import kr.co.kadb.cameralibrary.extension.showErrorToast
//import kr.co.kadb.cameralibrary.extension.showSuccessToast
//import kr.co.kadb.cameralibrary.ui.common.base.BaseBindingFragment
//import kr.co.kadb.cameralibrary.ui.common.type.PhotoType
//import kr.co.kadb.cameralibrary.ui.shooting.ShootController
//import kr.co.kadb.cameralibrary.vendor.aosp.AppExecutors
//import kr.co.kadb.cameralibrary.vendor.aosp.Injectable
//import kr.co.kadb.cameralibrary.vendor.aosp.autoCleared
//import javax.inject.Inject
//
///**
// * Created by oooobang on 2020. 2. 3..
// * 촬영 결과.
// */
//internal class ShootingManyFragment : BaseBindingFragment<FragmentShootingManyBinding>(), Injectable {
//	companion object {
//		private const val PATHS = "paths"
//		fun create(paths: Array<String>?): ShootingManyFragment {
//			val fragment = ShootingManyFragment()
//			val bundle = Bundle()
//			bundle.putStringArray(PATHS, paths)
//			fragment.arguments = bundle
//			return fragment
//		}
//	}
//
//	@Inject
//	lateinit var appExecutors: AppExecutors
//
//	@Inject
//	lateinit var viewController: ShootController
//
//	@Inject
//	lateinit var viewModelFactory: ViewModelProvider.Factory
//
//	// ViewModel.
//	private val viewModel: ShootingManyViewModel by viewModels {
//		viewModelFactory
//	}
//
//	// DataBinding.
//	var adapter by autoCleared<ShootingManyAdapter>()
//
//	// Fragment Layout.
//	override val layoutResourceId: Int = R.layout.fragment_shooting_many
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
//		//
//		val paths = arguments?.getStringArray(PATHS)
//		paths?.let {
//			adapter.submitList(it.toList())
//			binding.resultCount = it.size
//		}
//	}
//
//	// Setup Layout.
//	private fun setupLayout() {
//		// Adapter.
//		val recyclerViewAadapter = ShootingManyAdapter(appExecutors, dataBindingComponent)
//		binding.recyclerview.adapter = recyclerViewAadapter
//		adapter = recyclerViewAadapter
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
//		// 확대.
//		adapter.setZoomClickAction {
//			viewController.navigateToViewer(it)
//		}
//
//		// 저장.
//		binding.buttonSave.setOnClickListener {
//			// 선택사진, 사진설명, 사진구분.
//			val paths = adapter.selectedItems.toTypedArray()
//			val position = binding.spinnerPhotoType.selectedItemPosition
//			val photoType = context?.getResourceString(R.array.photoTypeCodes,
//					position) ?: PhotoType.BEFORE_REPAIR.value
//			val photoKind = PhotoType.KIND_VEHICLE.value
//			if (paths.isNotEmpty()) {
//				viewController.navigateToUploadDialog(paths, null, photoKind, photoType) { isSuccess, exception ->
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