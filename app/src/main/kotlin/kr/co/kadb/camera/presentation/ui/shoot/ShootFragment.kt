package kr.co.kadb.camera.presentation.ui.shoot

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kr.co.kadb.camera.R
import kr.co.kadb.camera.data.local.PreferenceManager
import kr.co.kadb.camera.databinding.FragmentShootBinding
import kr.co.kadb.camera.presentation.base.BaseBindingFragment
import kr.co.kadb.cameralibrary.presentation.CameraIntent
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey
import kr.co.kadb.cameralibrary.presentation.widget.util.UriHelper
import javax.inject.Inject

/**
 * Created by oooobang on 2022. 7. 17..
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

    private var resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val intent = result.data
            if (result.resultCode == Activity.RESULT_OK) {
                // 한 장, 여러 장.
                if (intent?.action == IntentKey.ACTION_TAKE_PICTURE) {
                    // 한장.
                    // 이미지 URI.
                    val imageUri = intent.data
                    // 이미지 가로.
                    val imageWidth = intent.getIntExtra(IntentKey.EXTRA_WIDTH, 0)
                    // 이미지 세로.
                    val imageHeight = intent.getIntExtra(IntentKey.EXTRA_HEIGHT, 0)
                    // 썸네임 이미지.
                    val thumbnailBitmap = intent.extras?.get("data") as? Bitmap

                    // 촬영 원본 이미지.
                    binding.imageview.setImageURI(imageUri)
                    // 썸네일 이미지.
                    binding.imageviewThumbnail.setImageBitmap(thumbnailBitmap)

                    // 이미지 중앙 기준 Crop(%).
                    val bitmap = UriHelper.rotateAndCenterCrop(
                        requireContext(),
                        imageUri,
                        arrayOf(0.7f, 0.5f)
                    )

                    // 이미지 저장.
                    //bitmap.save(requireContext(), true)
                    // 크롭 이미지.
                    binding.imageviewThumbnail.setImageBitmap(bitmap)
                } else if (intent?.action == IntentKey.ACTION_TAKE_MULTIPLE_PICTURES) {
                    // 여러장.
                    // 이미지 URI.
                    val imageUris = intent.getStringArrayListExtra(IntentKey.EXTRA_URIS)
                    // 이미지 사이즈.
                    val imageSizes = intent.getSerializableExtra(IntentKey.EXTRA_SIZES)
                }
            }
        }

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
        // 한장 촬영.
        binding.buttonOneShoot.setOnClickListener {
//            Intent(IntentKey.ACTION_TAKE_PICTURE).also { takePictureIntent ->
//                takePictureIntent.putExtra(IntentKey.EXTRA_CAN_MUTE, false)
//                takePictureIntent.putExtra(IntentKey.EXTRA_HAS_HORIZON, true)
//                takePictureIntent.putExtra(IntentKey.EXTRA_CROP_PERCENT, arrayOf(0.7f, 0.5f))
//                takePictureIntent.putExtra(IntentKey.EXTRA_CAN_UI_ROTATION, true)
//                takePictureIntent.putExtra(IntentKey.EXTRA_HORIZON_COLOR, true)
//                takePictureIntent.putExtra(IntentKey.EXTRA_CROP_BORDER_COLOR, true)
//                resultLauncher.launch(takePictureIntent)
//            }
            CameraIntent.Build(requireActivity()).apply {
                setAction(IntentKey.ACTION_TAKE_PICTURE)
                setCanMute(false)
                setHasHorizon(true)
                setCropPercent(arrayOf(0.7f, 0.5f))
                setCanUiRotation(true)
                setHorizonColor(Color.RED)
                setUnusedAreaBorderColor(Color.GREEN)
            }.run {
                resultLauncher.launch(this.build())
            }
        }

        // 여러장 촬영.
        binding.buttonMultipleShoot.setOnClickListener {
//            Intent(IntentKey.ACTION_TAKE_MULTIPLE_PICTURES).also { takePictureIntent ->
//                takePictureIntent.putExtra(IntentKey.EXTRA_CAN_MUTE, false)
//                takePictureIntent.putExtra(IntentKey.EXTRA_HAS_HORIZON, true)
//                takePictureIntent.putExtra(IntentKey.EXTRA_CROP_PERCENT, arrayOf(0.7f, 0.5f))
//                takePictureIntent.putExtra(IntentKey.EXTRA_CAN_UI_ROTATION, true)
//                takePictureIntent.putExtra(IntentKey.EXTRA_HORIZON_COLOR, true)
//                takePictureIntent.putExtra(IntentKey.EXTRA_CROP_BORDER_COLOR, true)
//                resultLauncher.launch(takePictureIntent)
//            }
            CameraIntent.Build(requireActivity()).apply {
                setAction(IntentKey.ACTION_TAKE_MULTIPLE_PICTURES)
                setCanMute(false)
                setHasHorizon(true)
                setCropPercent(arrayOf(0.7f, 0.5f))
                setCanUiRotation(true)
                setHorizonColor(Color.RED)
                setUnusedAreaBorderColor(Color.GREEN)
            }.run {
                resultLauncher.launch(this.build())
            }
        }
    }

    // Init Callback.
    override fun initCallback() {
    }
}