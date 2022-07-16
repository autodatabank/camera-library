package kr.co.kadb.camera.presentation.base

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import kr.co.kadb.camera.presentation.widget.extension.registerReceiver
import kr.co.kadb.camera.presentation.widget.listener.BackPressedListener
import kr.co.kadb.camera.presentation.widget.listener.FragmentCreateListener
import kr.co.kadb.camera.presentation.widget.listener.FragmentResumeListener
import kr.co.kadb.camera.presentation.widget.listener.FragmentStartListener
import kr.co.kadb.camera.presentation.widget.util.IntentKey.BROADCAST_EVENT
import kr.co.kadb.camera.presentation.widget.util.IntentKey.BROADCAST_EVENT_ACTIVATED
import kr.co.kadb.camera.presentation.widget.util.OBFragment
import timber.log.Timber
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
		Timber.d(">>>>> onFragmentSelected")
	}

	@Suppress("unused")
	open fun onFragmentDeselected() {
		// Debug.
		Timber.d(">>>>> onFragmentDeselected")
	}

	@Suppress("unused")
	open fun onLastFragmentActivation() {
		// Debug.
		Timber.d(">>>>> onLastFragmentActivation")
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
		Timber.i("EVENT_BROADCAST_RECEIVER")
	}

	// Event Receiver.
	protected open fun eventBroadcastReceiverForActivated(intent: Intent?) {
		Timber.i("EVENT_BROADCAST_RECEIVER_FOR_ACTIVATED")
	}

	// Cover OnClick Callback.
	/*inline fun coverOnClickCallback(crossinline callback: () -> Unit) = object : CoverOnClickCallback {
		override fun callback() {
			Timber.i("COVER")
		}
	}

	// Positive Callback.
	inline fun positiveCallback(crossinline callback: () -> Unit) = object : PositiveCallback {
		override fun callback() {
			Timber.i("POSITIVE")
		}
	}

	// Negative Callback.
	inline fun negativeCallback(crossinline callback: () -> Unit) = object : NegativeCallback {
		override fun callback() {
			Timber.i("NEGATIVE")
		}
	}

	// Retry Callback.
	inline fun <T> retryCallback(crossinline callback: (resource: Resource<T>) -> Unit) = object : RetryCallback {
		override fun <T> retry(resource: Resource<T>) {
			Timber.i("RETRY")
		}
	}*/
}
