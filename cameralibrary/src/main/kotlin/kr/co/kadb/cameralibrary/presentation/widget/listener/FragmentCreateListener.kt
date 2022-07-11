@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.listener

import kr.co.kadb.cameralibrary.presentation.base.BaseFragment
import java.lang.ref.WeakReference

/**
 * Created by oooobang on 2018. 3. 6..
 * Generic interface for Create Fragment.
 */
internal interface FragmentCreateListener {
	fun onFragmentCreate(fragmentRef: WeakReference<BaseFragment>)
}