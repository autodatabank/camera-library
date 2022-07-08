package kr.co.kadb.camera.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel

/**
 * Created by oooobang on 2018. 3. 2..
 * Base Fragment.
 */
internal abstract class BaseBindingFragment<T : ViewDataBinding, VM : ViewModel> : BaseFragment() {
    // Binding.
    lateinit var binding: T

    // ViewModel.
    abstract val viewModel: VM

    // Layout resource ID.
    @get:LayoutRes
    abstract val layoutResourceId: Int

    val baseActivity: BaseBindingActivity<*, *>
        get() = activity as BaseBindingActivity<*, *>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, layoutResourceId, container, false)
        binding.lifecycleOwner = this@BaseBindingFragment
        return binding.root

        initLayout()
        initObserver()
        initListener()
        initCallback()
    }

    abstract fun initLayout()
    abstract fun initObserver()
    abstract fun initListener()
    abstract fun initCallback()
}
