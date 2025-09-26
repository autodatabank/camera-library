package kr.co.kadb.cameralibrary.presentation.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

/**
 * Base ViewBinding Fragment.
 * 
 * ViewBinding을 사용하는 Fragment들의 베이스 클래스입니다.
 * 뷰바인딩의 생성, 초기화, 해제를 자동으로 처리하며,
 * 화면 초기화를 위한 추상 메서드들을 제공합니다.
 *
 * @param T ViewBinding 타입
 */
internal abstract class BaseViewBindingFragment<T : ViewBinding> : BaseFragment() {

    /**
     * ViewBinding 인스턴스를 저장하는 private 변수
     */
    private var _binding: T? = null
    
    /**
     * ViewBinding 인스턴스에 안전하지 않은 접근을 제공하는 프로퍼티
     * 
     * @return ViewBinding 인스턴스 (null이면 예외 발생)
     * @throws KotlinNullPointerException _binding이 null인 경우
     */
    val binding get() = _binding!!
    
    /**
     * ViewBinding 인스턴스에 안전한 접근을 제공하는 프로퍼티
     * 
     * @return ViewBinding 인스턴스 또는 null
     */
    val bindingSafely get() = _binding

    /**
     * Fragment의 ViewBinding을 생성하는 추상 메서드
     * 
     * @param inflater LayoutInflater 인스턴스
     * @param container 부모 ViewGroup (nullable)
     * @return 생성된 ViewBinding 인스턴스
     */
    abstract fun fragmentBinding(inflater: LayoutInflater, container: ViewGroup?): T

    /**
     * Fragment의 뷰를 생성합니다.
     * ViewBinding을 초기화하고 루트 뷰를 반환합니다.
     * 
     * @param inflater LayoutInflater 인스턴스
     * @param container 부모 ViewGroup (nullable)
     * @param savedInstanceState 이전 상태 정보 (nullable)
     * @return Fragment의 루트 뷰
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = fragmentBinding(inflater, container)
        return binding.root
    }

    /**
     * 뷰가 생성된 후 호출되는 메서드입니다.
     * 화면 초기화, 옵저버 설정, 리스너 설정을 순차적으로 실행합니다.
     * 
     * @param view 생성된 뷰
     * @param savedInstanceState 이전 상태 정보 (nullable)
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initScreen(view, savedInstanceState)
        initObserver()
        initListener()
    }

    /**
     * Fragment의 뷰가 파괴될 때 호출되는 메서드입니다.
     * ViewBinding 참조를 해제하여 메모리 누수를 방지합니다.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 화면 초기화를 담당하는 추상 메서드
     * 뷰 컴포넌트들의 초기 설정을 수행합니다.
     * 
     * @param view 생성된 뷰
     * @param savedInstanceState 이전 상태 정보 (nullable)
     */
    protected abstract fun initScreen(view: View, savedInstanceState: Bundle?)
    
    /**
     * 데이터 옵저버 설정을 담당하는 추상 메서드
     * ViewModel의 LiveData나 StateFlow 등을 관찰하는 로직을 구현합니다.
     */
    protected abstract fun initObserver()
    
    /**
     * 이벤트 리스너 설정을 담당하는 추상 메서드
     * 버튼 클릭, 텍스트 변경 등의 이벤트 리스너를 설정합니다.
     */
    protected abstract fun initListener()
}
