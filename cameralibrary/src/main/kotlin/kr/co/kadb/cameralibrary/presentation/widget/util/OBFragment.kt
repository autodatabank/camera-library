package kr.co.kadb.cameralibrary.presentation.widget.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

/**
 * Created by oooobang on 2018. 3. 7..
 * Fragment.
 */
internal abstract class OBFragment : Fragment() {
	override fun onAttach(context: Context) {
		super.onAttach(context)

		//==================================================
		DebugLog.v { "${javaClass.simpleName} onAttach(Context) <" }
		//==================================================
	}

	/*****
	 * Fragment의 onCreate() 메소드는 Activity의 onCreate() 메소드와 비슷하지만 Bundle을 받아오기 때문에 bundle에 대한 속성을 사용할 수 있다.
	 */
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		//==================================================
		DebugLog.v { "${javaClass.simpleName} onCreate <" }
		//==================================================
	}

	//
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		//==================================================
		DebugLog.v { "${javaClass.simpleName} onViewCreated <" }
		//==================================================
	}

	/*****
	 * 이 메소드가 호출되면 화면의 모든 UI가 만들어진 지고 호출이 된다.
	 */
	override fun onStart() {
		super.onStart()

		//==================================================
		DebugLog.v { "${javaClass.simpleName} onStart <" }
		//==================================================
	}

	/*****
	 * 이 메소드가 호출되고 난 다음에 사용자와 Fragment와 상호작용이 가능하다.
	 * 다시 말해서 이 곳에서 사용자가 버튼을 누르거나 하는 이벤트를 받을 수 있게 된다.
	 */
	override fun onResume() {
		super.onResume()

		//==================================================
		DebugLog.v { "${javaClass.simpleName} onResume <" }
		//==================================================
	}

	/*****
	 * 이 메소드는 Fragment가 다시 돌아갈 때 (Back) 처음으로 불려지는 콜백 메소드이다.
	 */
	override fun onPause() {
		super.onPause()

		//==================================================
		DebugLog.v { "${javaClass.simpleName} onPause <" }
		//==================================================
	}

	/*****
	 * 이 메소드에서는 Activity와 동일하게 Fragment가 사라질때 현재의 상태를 저장하고 나중에 Fragment가 돌아오면 다시 저장한 내용을 사용할 수 있게해주는 메소드이다.
	 */
	override fun onSaveInstanceState(savedInstanceState: Bundle) {
		super.onSaveInstanceState(savedInstanceState)

		//==================================================
		DebugLog.v { "${javaClass.simpleName} onSaveInstanceState <" }
		//==================================================
	}

	/*****
	 * Fragment의 onStop() 메소드는 Activity의 onStop()메소드와 비슷하다.
	 * 이 콜백 메소드가 호출되면 Fragment가 더이상 보이지 않는 상태이고 더이상 Activity에서 Fragment에게 오퍼레이션을 할 수 없게 된다.
	 */
	override fun onStop() {
		super.onStop()

		//==================================================
		DebugLog.v { "${javaClass.simpleName} onStop <" }
		//==================================================
	}

	/*****
	 * Fragment의 View가 모두 소멸될 때 호출되는 콜백 메소드이다.
	 * 이때 View에 관련된 모든 자원들이 사라지게 된다.
	 */
	override fun onDestroyView() {
		super.onDestroyView()

		//==================================================
		DebugLog.v { "${javaClass.simpleName} onDestroyView <" }
		//==================================================
	}

	/*****
	 * Fragment를 더이상 사용하지 않을 때 호출되는 콜백 메소드이다.
	 * 하지만 Activity와의 연결은 아직 끊어진 상태는 아니다.
	 */
	override fun onDestroy() {
		super.onDestroy()

		//==================================================
		DebugLog.v { "${javaClass.simpleName} onDestroy <" }
		//==================================================
	}

	/*****
	 * Fragment가 더이상 Activity와 관계가 없을 때 두 사이의 연결을 끊으며 Fragment에 관련된 모든 자원들이 사라지게 된다.
	 */
	override fun onDetach() {
		super.onDetach()

		//==================================================
		DebugLog.v { "${javaClass.simpleName} onDetach <" }
		//==================================================
	}

	override fun startActivity(intent: Intent) {
		super.startActivity(intent)

		//==================================================
		DebugLog.v { "${javaClass.simpleName} startActivity <" }
		//==================================================
	}

	override fun onLowMemory() {
		super.onLowMemory()

		//==================================================
		DebugLog.e { "${javaClass.simpleName} onLowMemory <" }
		//==================================================
	}
}
