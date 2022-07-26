package kr.co.kadb.camera.presentation.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel

/**
 * Created by oooobang on 2020. 1. 17..
 * Base Binding Activity.
 */
internal abstract class BaseBindingActivity<T : ViewDataBinding, VM : ViewModel> : BaseActivity() {
    // Binding.
    lateinit var binding: T

    // ViewModel.
    abstract val viewModel: VM

    // Layout resource ID.
    @get:LayoutRes
    abstract val layoutResourceId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutResourceId)
        binding.lifecycleOwner = this
        initVariable()
        initLayout()
        initObserver()
        initListener()
        initCallback()
    }

    protected abstract fun initVariable()
    protected abstract fun initLayout()
    protected abstract fun initObserver()
    protected abstract fun initListener()
    protected abstract fun initCallback()
}