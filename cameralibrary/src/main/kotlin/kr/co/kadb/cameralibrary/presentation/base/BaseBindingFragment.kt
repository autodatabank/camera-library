package kr.co.kadb.cameralibrary.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel

/**
 * Created by oooobang on 2022. 7. 2..
 * Base BindingFragment.
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
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initVariable()
        initLayout(view)
        initObserver()
        initListener()
        initCallback()
    }

    protected abstract fun initVariable()
    protected abstract fun initLayout(view: View)
    protected abstract fun initObserver()
    protected abstract fun initListener()
    protected abstract fun initCallback()
}
