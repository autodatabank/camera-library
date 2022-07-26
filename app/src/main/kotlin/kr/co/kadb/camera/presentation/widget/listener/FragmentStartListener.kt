@file:Suppress("unused")

package kr.co.kadb.camera.presentation.widget.listener

import kr.co.kadb.camera.presentation.base.BaseFragment
import java.lang.ref.WeakReference

/**
 * Created by oooobang on 2018. 3. 6..
 * Generic interface for Start Fragment.
 */
internal interface FragmentStartListener {
	fun onFragmentStart(fragmentRef: WeakReference<BaseFragment>)
}