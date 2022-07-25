package kr.co.kadb.camera.presentation.ui.shoot

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Size
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kr.co.kadb.camera.R
import kr.co.kadb.camera.data.local.PreferenceManager
import kr.co.kadb.camera.databinding.FragmentShootBinding
import kr.co.kadb.camera.presentation.base.BaseBindingFragment
import kr.co.kadb.camera.presentation.widget.extension.save
import kr.co.kadb.cameralibrary.presentation.widget.extension.rotateAndCenterCrop
import kr.co.kadb.cameralibrary.presentation.widget.extension.rotateAndCrop
import kr.co.kadb.cameralibrary.presentation.widget.extension.toBase64
import kr.co.kadb.cameralibrary.presentation.widget.extension.toBitmap
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey
import timber.log.Timber
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

                    // Debug.
                    binding.imageview.setImageURI(imageUri)
                    binding.imageviewThumbnail.setImageBitmap(thumbnailBitmap)
//
//                    imageUri?.toBitmap(requireContext())?.save(requireContext(), true)
                    val bitmap = imageUri?.rotateAndCenterCrop(requireContext(), Size(1000, 1000))
                    bitmap.save(requireContext(), true)

                    // Debug.
                    Timber.i(">>>>> Base64 : %s", bitmap.toBase64())

//                    val bitmap = imageUri?.rotateAndCrop(
//                        requireContext(),
//                        Rect(100, 100, 2000, 2000)
//                    )
                    binding.imageviewThumbnail.setImageBitmap(bitmap)

                    // Debug.
                    Timber.i(">>>>> TAKE_PICTURE imageUri : $imageUri")
                    Timber.i(">>>>> TAKE_PICTURE imageWidth : $imageWidth")
                    Timber.i(">>>>> TAKE_PICTURE imageHeight : $imageHeight")
                } else if (intent?.action == IntentKey.ACTION_TAKE_MULTIPLE_PICTURE) {
                    // 여러장.
                    // 이미지 URI.
                    val imageUris = intent.getStringArrayListExtra(IntentKey.EXTRA_URIS)
                    // 이미지 사이즈.
                    val imageSizes = intent.getSerializableExtra(IntentKey.EXTRA_SIZES)

                    // Debug.
                    Timber.i(">>>>> TAKE_MULTIPLE_PICTURE imageUris : $imageUris")
                    Timber.i(">>>>> TAKE_MULTIPLE_PICTURE imageSizes : $imageSizes")
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
        // 촬영.
        binding.buttonShooting.setOnClickListener {
//            Intent(IntentKey.ACTION_TAKE_MULTIPLE_PICTURE).also { takePictureIntent ->
            Intent(IntentKey.ACTION_TAKE_PICTURE).also { takePictureIntent ->
                takePictureIntent.putExtra(IntentKey.EXTRA_HAS_MUTE, false)
                takePictureIntent.putExtra(IntentKey.EXTRA_CROP_PERCENT, arrayOf(0.3f, 0.4f))
                resultLauncher.launch(takePictureIntent)
            }
        }
    }

    // Init Callback.
    override fun initCallback() {
    }
}