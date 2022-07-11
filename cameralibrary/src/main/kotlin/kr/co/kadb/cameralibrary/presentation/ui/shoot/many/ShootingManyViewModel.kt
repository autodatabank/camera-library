//package kr.co.kadb.cameralibrary.presentation.ui.shoot.many
//
//import android.app.Application
//import androidx.lifecycle.*
//import kr.co.kadb.cameralibrary.ui.common.PreferencesHelper
//import kr.co.kadb.cameralibrary.viewmodel.BaseAndroidViewModel
//import javax.inject.Inject
//
///**
// * Created by oooobang on 2020. 2. 3..
// * ViewModel.
// */
//internal class ShootingManyViewModel
//@Inject
//constructor(
//		application: Application,
//		preferences: PreferencesHelper
//) : BaseAndroidViewModel(application) {
//	// 기본 사진종류.
//	val defaultPhotoType = MutableLiveData<String>()
//
//	init {
//		// 초기 사진종류.
//		defaultPhotoType.value = preferences.defaultPhotoType
//	}
//}