package kr.co.kadb.camera

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kr.co.kadb.cameralibrary.presentation.CameraIntent
import kr.co.kadb.cameralibrary.presentation.widget.util.BitmapHelper
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey
import kr.co.kadb.cameralibrary.presentation.widget.util.UriHelper
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    // Activity for result.
    // Example 2, 3.
    private var resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val intent = result.data
            if (result.resultCode == RESULT_OK) {
                // 한 장, 여러 장.
                if (intent?.action == IntentKey.ACTION_TAKE_PICTURE) {
                    // 한장.
                    // 이미지 URI.
                    val imageUri = intent.data ?: return@registerForActivityResult
                    // 이미지 가로.
                    //val imageWidth = intent.getIntExtra(IntentKey.EXTRA_WIDTH, 0)
                    // 이미지 세로.
                    //val imageHeight = intent.getIntExtra(IntentKey.EXTRA_HEIGHT, 0)
                    // 이미지 방향.
                    //val imageRotation = intent.getIntExtra(IntentKey.EXTRA_ROTATION, 0)
                    // 썸네임 이미지.
                    //val thumbnailBitmap = intent.extras?.get("data") as? Bitmap

                    // 이미지 중앙을 기준으로 원본 사이즈에서 가로:70% 세로:50% 크롭.
                    /*val cropBitmap = UriHelper.rotateAndCenterCrop(
                        baseContext, imageUri, arrayOf(0.7f, 0.5f)
                    )*/

                    // Uri를 이미지로 변환.
                    val cropBitmap = UriHelper.toBitmap(baseContext, imageUri)

                    // Bitmap 저장.
                    //cropBitmap.save(baseContext, true)

                    // 가로, 세로 중 큰 길이를 640(pixel)에 맞춰 비율 축소.
                    val resizeBitmap = BitmapHelper.resize(cropBitmap, 640)
                    cropBitmap?.recycle()

                    // 가로, 세로 중 큰 길이를 640(pixel)에 가깝게(640이상 ~ 1280미만) 맞춰 비율 축소.
                    // 예) resizePixcel이 640인 경우 결과는 640이상 ~ 1280미만.
                    // 성능 및 좋은 샘플링으로 이미지를 추출.
                    //val optimumResizeBitmap = BitmapHelper.optimumResize(cropBitmap, 640)

                    // Bitmap 저장.
                    //resizeBitmap.save(baseContext, true)

                    // Base64로 인코딩 된 문자열 반환.
                    //val base64 = BitmapHelper.toBase64(resizeBitmap)

                    // 촬영 원본 이미지.
                    findViewById<ImageView>(R.id.imageview).setImageURI(imageUri)
                    // 촬영 원본을 크롭 및 리사이즈한 이미지.
                    findViewById<ImageView>(R.id.imageview_thumbnail).setImageBitmap(resizeBitmap)
                } else if (intent?.action == IntentKey.ACTION_TAKE_MULTIPLE_PICTURES) {
                    // 여러장.
                    // 이미지 URI.
                    val imageUris = intent.getSerializableExtra(IntentKey.EXTRA_URIS)
                    // 이미지 사이즈.
                    val imageSizes = intent.getSerializableExtra(IntentKey.EXTRA_SIZES)

                    // Debug.
                    Timber.d(">>>>> imageUris : $imageUris")
                    Timber.d(">>>>> imageSizes : $imageSizes")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Debug.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // 한장 촬영.
        findViewById<Button>(R.id.button_one_shoot).setOnClickListener {
            // Example 1.
            /*Intent(IntentKey.ACTION_TAKE_PICTURE).also { takePictureIntent ->
                takePictureIntent.putExtra(IntentKey.EXTRA_CAN_MUTE, false)
                takePictureIntent.putExtra(IntentKey.EXTRA_HAS_HORIZON, true)
                takePictureIntent.putExtra(IntentKey.EXTRA_CROP_PERCENT, arrayOf(0.7f, 0.5f))
                takePictureIntent.putExtra(IntentKey.EXTRA_CAN_UI_ROTATION, true)
                takePictureIntent.putExtra(IntentKey.EXTRA_IS_SAVE_CROPPED_IMAGE, true)
                takePictureIntent.putExtra(IntentKey.EXTRA_HORIZON_COLOR, Color.RED)
                takePictureIntent.putExtra(IntentKey.EXTRA_CROP_BORDER_COLOR, Color.GREEN)
                takePictureIntent.putExtra(IntentKey.EXTRA_CROPPED_JPEG_QUALITY, 95)
            }.run {
                startActivityForResult(this)
            }*/

            // Example 2.
            /*Intent(IntentKey.ACTION_TAKE_PICTURE).also { takePictureIntent ->
                takePictureIntent.putExtra(IntentKey.EXTRA_CAN_MUTE, false)
                takePictureIntent.putExtra(IntentKey.EXTRA_HAS_HORIZON, true)
                takePictureIntent.putExtra(IntentKey.EXTRA_CROP_PERCENT, arrayOf(0.7f, 0.5f))
                takePictureIntent.putExtra(IntentKey.EXTRA_CAN_UI_ROTATION, true)
                takePictureIntent.putExtra(IntentKey.EXTRA_IS_SAVE_CROPPED_IMAGE, true)
                takePictureIntent.putExtra(IntentKey.EXTRA_HORIZON_COLOR, Color.RED)
                takePictureIntent.putExtra(IntentKey.EXTRA_CROP_BORDER_COLOR, Color.GREEN)
                takePictureIntent.putExtra(IntentKey.EXTRA_CROPPED_JPEG_QUALITY, 95)
            }.run {
                resultLauncher.launch(this)
            }*/

            // Example 3.
            CameraIntent.Build(this).apply {
                setAction(IntentKey.ACTION_TAKE_PICTURE)
                //setCanMute(false)
                setHasHorizon(true)
                setCropPercent(arrayOf(0.7f, 0.5f))
                setCanUiRotation(true)
                setSaveCropedImage(true)
                //setHorizonColor(Color.RED)
                //setUnusedAreaBorderColor(Color.GREEN)
                //setCroppedJpegQuality(95)
            }.run {
                resultLauncher.launch(this.build())
            }
        }

        // 여러장 촬영.
        findViewById<Button>(R.id.button_multiple_shoot).setOnClickListener {
            // Example 1.
            /*Intent(IntentKey.ACTION_TAKE_MULTIPLE_PICTURES).also { takePictureIntent ->
                takePictureIntent.putExtra(IntentKey.EXTRA_CAN_MUTE, false)
                takePictureIntent.putExtra(IntentKey.EXTRA_HAS_HORIZON, true)
                takePictureIntent.putExtra(IntentKey.EXTRA_CROP_PERCENT, arrayOf(0.7f, 0.5f))
                takePictureIntent.putExtra(IntentKey.EXTRA_CAN_UI_ROTATION, true)
                takePictureIntent.putExtra(IntentKey.EXTRA_IS_SAVE_CROPPED_IMAGE, true)
                takePictureIntent.putExtra(IntentKey.EXTRA_HORIZON_COLOR, Color.RED)
                takePictureIntent.putExtra(IntentKey.EXTRA_CROP_BORDER_COLOR, Color.GREEN)
                takePictureIntent.putExtra(IntentKey.EXTRA_CROPPED_JPEG_QUALITY, 95)
            }.run {
                startActivityForResult(this)
            }*/

            // Example 2.
            /*Intent(IntentKey.ACTION_TAKE_MULTIPLE_PICTURES).also { takePictureIntent ->
                takePictureIntent.putExtra(IntentKey.EXTRA_CAN_MUTE, false)
                takePictureIntent.putExtra(IntentKey.EXTRA_HAS_HORIZON, true)
                takePictureIntent.putExtra(IntentKey.EXTRA_CROP_PERCENT, arrayOf(0.7f, 0.5f))
                takePictureIntent.putExtra(IntentKey.EXTRA_CAN_UI_ROTATION, true)
                takePictureIntent.putExtra(IntentKey.EXTRA_IS_SAVE_CROPPED_IMAGE, true)
                takePictureIntent.putExtra(IntentKey.EXTRA_HORIZON_COLOR, Color.RED)
                takePictureIntent.putExtra(IntentKey.EXTRA_CROP_BORDER_COLOR, Color.GREEN)
                takePictureIntent.putExtra(IntentKey.EXTRA_CROPPED_JPEG_QUALITY, 95)
            }.run {
                resultLauncher.launch(this)
            }*/

            // Example 3.
            CameraIntent.Build(this).apply {
                setAction(IntentKey.ACTION_TAKE_MULTIPLE_PICTURES)
                //setCanMute(false)
                setHasHorizon(true)
                setCropPercent(arrayOf(0.7f, 0.5f))
                setCanUiRotation(true)
                //setSaveCropedImage(true)
                //setHorizonColor(Color.RED)
                //setUnusedAreaBorderColor(Color.GREEN)
                //setCroppedJpegQuality(95)
            }.run {
                resultLauncher.launch(this.build())
            }
        }
    }
}