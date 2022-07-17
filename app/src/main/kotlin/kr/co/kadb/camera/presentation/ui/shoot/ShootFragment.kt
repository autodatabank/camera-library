package kr.co.kadb.camera.presentation.ui.shoot

import android.content.Intent
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kr.co.kadb.camera.R
import kr.co.kadb.camera.data.local.PreferenceManager
import kr.co.kadb.camera.databinding.FragmentShootBinding
import kr.co.kadb.camera.presentation.base.BaseBindingFragment
import kr.co.kadb.cameralibrary.presentation.widget.event.IntentAction
import javax.inject.Inject

/**
 * Created by oooobang on 2020. 2. 3..
 * 촬영.
 */
@AndroidEntryPoint
internal class ShootFragment : BaseBindingFragment<FragmentShootBinding, ShootViewModel>() {
    companion object {
        fun create() = ShootFragment()
    }

    @Inject
    lateinit var preferences: PreferenceManager

    @Inject
    lateinit var viewController: ShootController

    // ViewModel.
    override val viewModel: ShootViewModel by viewModels()

    // Fragment Layout.
    override val layoutResourceId: Int = R.layout.fragment_shoot

    override fun initVariable() {
    }

    // Init Layout.
    override fun initLayout() {
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
            Intent(IntentAction.ACTION_TAKE_PICTURE).also { imageCaptureIntent ->
                activity?.startActivity(imageCaptureIntent)
            }
        }
    }

    // Init Callback.
    override fun initCallback() {
    }
}