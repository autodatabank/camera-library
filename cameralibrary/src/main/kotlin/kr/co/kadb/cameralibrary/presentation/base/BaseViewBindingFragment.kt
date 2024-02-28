package kr.co.kadb.cameralibrary.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * Base ViewBinding Fragment.
 */
internal abstract class BaseViewBindingFragment<T : ViewBinding> : BaseFragment() {

    // Binding.
    private var _binding: T? = null
    val binding get() = _binding!!
    val bindingSafely get() = _binding

    // ViewBinding.
    abstract fun fragmentBinding(inflater: LayoutInflater, container: ViewGroup?): T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = fragmentBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initScreen(view, savedInstanceState)
        initObserver()
        initListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    protected abstract fun initScreen(view: View, savedInstanceState: Bundle?)
    protected abstract fun initObserver()
    protected abstract fun initListener()
}
