package kr.co.kadb.cameralibrary.presentation.base

import android.content.*
import android.os.Bundle
import kr.co.kadb.cameralibrary.presentation.widget.extension.registerReceiver
import kr.co.kadb.cameralibrary.presentation.widget.listener.*
import kr.co.kadb.cameralibrary.presentation.widget.util.DebugLog
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.BROADCAST_EVENT
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey.BROADCAST_EVENT_ACTIVATED
import kr.co.kadb.cameralibrary.presentation.widget.util.OBFragment
import java.lang.ref.WeakReference


/**
 * Created by oooobang on 2018. 3. 2..
 * Base Fragment.
 */
internal abstract class BaseFragment : OBFragment(), BackPressedListener {
    /*// Action Event Listener.
    var actionEventListenerRef: WeakReference<ActionEventListener?>? = null*/

    // Event BroadcastReceiver.
    private lateinit var eventBroadcastReceiver: BroadcastReceiver

    // Event BroadcastReceiver.
    private lateinit var eventBroadcastReceiverForActivated: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Event BroadcastReceiver 등록.
        registerEventReceiver()

        // Create Delegate.
        (activity as? FragmentCreateListener)?.onFragmentCreate(WeakReference(this))
    }

    override fun onStart() {
        super.onStart()

        // Event BroadcastReceiver 등록.
        registerEventReceiverForActivated()

        // Start Delegate.
        (activity as? FragmentStartListener)?.onFragmentStart(WeakReference(this))
    }

    override fun onResume() {
        super.onResume()

        // Resume Delegate.
        (activity as? FragmentResumeListener)?.onFragmentResume(WeakReference(this))
    }

    override fun onStop() {
        super.onStop()

        // Event BroadcastReceiver 해지.
        unregisterEventReceiverForActivated()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Action Event BroadcastReceiver 해지.
        unregisterEventReceiver()
    }

    //
    override fun onBackPressed(): Boolean {
        return false
    }

    @Suppress("unused")
    open fun onFragmentSelected() {
        // Debug.
        DebugLog.d { ">>>>> onFragmentSelected" }
    }

    @Suppress("unused")
    open fun onFragmentDeselected() {
        // Debug.
        DebugLog.d { ">>>>> onFragmentDeselected" }
    }

    @Suppress("unused")
    open fun onLastFragmentActivation() {
        // Debug.
        DebugLog.d { "onLastFragmentActivation" }
    }

    // Action Event BroadcastReceiver 등록.
    private fun registerEventReceiver() {
        val intentFilter = IntentFilter(BROADCAST_EVENT)
        context?.applicationContext?.apply {
            eventBroadcastReceiver = registerReceiver(intentFilter) {
                eventBroadcastReceiver(it)
            }
        }
    }

    // Data BroadcastReceiver 등록.
    private fun registerEventReceiverForActivated() {
        val intentFilter = IntentFilter(BROADCAST_EVENT_ACTIVATED)
        context?.applicationContext?.apply {
            eventBroadcastReceiverForActivated = registerReceiver(intentFilter) {
                eventBroadcastReceiverForActivated(it)
            }
        }
    }

    // Action Event BroadcastReceiver 해지.
    private fun unregisterEventReceiver() {
        context?.applicationContext?.unregisterReceiver(eventBroadcastReceiver)
    }

    // Data BroadcastReceiver 해지.
    private fun unregisterEventReceiverForActivated() {
        context?.applicationContext?.unregisterReceiver(eventBroadcastReceiverForActivated)
    }

    // Event Receiver.
    protected open fun eventBroadcastReceiver(intent: Intent?) {
        DebugLog.i { "EVENT_BROADCAST_RECEIVER" }
    }

    // Event Receiver.
    protected open fun eventBroadcastReceiverForActivated(intent: Intent?) {
        DebugLog.i { "EVENT_BROADCAST_RECEIVER_FOR_ACTIVATED" }
    }
}
