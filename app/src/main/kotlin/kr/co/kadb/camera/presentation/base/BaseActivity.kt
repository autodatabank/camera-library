package kr.co.kadb.camera.presentation.base

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Bundle
import kr.co.kadb.camera.presentation.widget.extension.registerReceiver
import kr.co.kadb.camera.presentation.widget.listener.FragmentCreateListener
import kr.co.kadb.camera.presentation.widget.listener.FragmentResumeListener
import kr.co.kadb.camera.presentation.widget.listener.FragmentStartListener
import kr.co.kadb.camera.presentation.widget.util.IntentKey.BROADCAST_FINISH
import kr.co.kadb.camera.presentation.widget.util.OBActivity
import java.lang.ref.WeakReference

/**
 * Created by oooobang on 2018. 2. 28..
 * Base Activity.
 */
internal abstract class BaseActivity : OBActivity(), FragmentCreateListener, FragmentStartListener,
    FragmentResumeListener {
    // 종료 BroadcastReceiver.
    private lateinit var finishReceiver: BroadcastReceiver

    // 현재 활성 된 Fragment.
    private var activeFragmentRef: WeakReference<BaseFragment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 종료 BroadcastReceiver 등록.
        registerFinishReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()

        // 종료 BroadcastReceiver 해지.
        unregisterFinishReceiver()
    }

    // Backkey Pressed.
    override fun onBackPressed() {
        if (activeFragmentRef?.get()?.onBackPressed() != true) {
            super.onBackPressed()
        }
    }

    // Fragment onCreate.
    override fun onFragmentCreate(fragmentRef: WeakReference<BaseFragment>) {
    }

    // Fragment onStart.
    override fun onFragmentStart(fragmentRef: WeakReference<BaseFragment>) {
    }

    // Fragment onResume.
    override fun onFragmentResume(fragmentRef: WeakReference<BaseFragment>) {
        activeFragmentRef = fragmentRef
    }

    // 종료 BroadcastReceiver 등록.
    private fun registerFinishReceiver() {
        val intentFilter = IntentFilter(BROADCAST_FINISH)
        finishReceiver = registerReceiver(intentFilter) {
            this@BaseActivity.finish()
        }
    }

    // 종료 BroadcastReceiver 해지.
    private fun unregisterFinishReceiver() {
        unregisterReceiver(finishReceiver)
    }
}