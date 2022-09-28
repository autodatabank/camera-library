package kr.co.kadb.camera

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kr.co.kadb.cameralibrary.presentation.CameraIntent
import kr.co.kadb.cameralibrary.presentation.model.CropSize
import kr.co.kadb.cameralibrary.presentation.widget.mlkit.GraphicOverlay
import kr.co.kadb.cameralibrary.presentation.widget.mlkit.TextRecognitionProcessor
import kr.co.kadb.cameralibrary.presentation.widget.mlkit.VisionImageProcessor
import kr.co.kadb.cameralibrary.presentation.widget.util.BitmapHelper
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey
import kr.co.kadb.cameralibrary.presentation.widget.util.UriHelper
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    //
    private var imageProcessor: VisionImageProcessor? = null

    // Crop Size.
    private val cropSize = CropSize(0.7f, 0.5f)

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
                    /*val bitmap = UriHelper.rotateAndCenterCrop(
                        baseContext, imageUri, cropSize.width, cropSize.height
                    )*/

                    // Uri를 이미지로 변환.
                    val bitmap = UriHelper.toBitmap(baseContext, imageUri)

                    // Bitmap 저장.
                    //cropBitmap.save(baseContext, true)

                    // 가로, 세로 중 큰 길이를 640(pixel)에 맞춰 비율 축소.
                    val resizeBitmap = BitmapHelper.resize(bitmap, 640)
                    // 가로, 세로 중 큰 길이를 640(pixel)에 가깝게(640이상 ~ 1280미만) 맞춰 비율 축소.
                    // 예) resizePixcel이 640인 경우 결과는 640이상 ~ 1280미만.
                    // 성능 및 좋은 샘플링으로 이미지를 추출.
                    //val resizeBitmap = BitmapHelper.optimumResize(bitmap, 640)
                    bitmap?.recycle()

                    // Bitmap 저장.
                    //resizeBitmap.save(baseContext, true)

                    // Base64로 인코딩 된 문자열 반환.
                    //val base64 = BitmapHelper.toBase64(resizeBitmap)


                    //
                    findViewById<GraphicOverlay>(R.id.adb_cameralibrary_graphic_overlay)?.apply {
                        setImageSourceInfo(
                            resizeBitmap?.width ?: 0,
                            resizeBitmap?.height ?: 0,
                            false
                        )
                    }?.let { graphicOverlay ->
                        //
                        findViewById<ImageView>(R.id.imageview_thumbnail).isVisible = false
                        //
                        imageProcessor = TextRecognitionProcessor(
                            context = baseContext,
                            KoreanTextRecognizerOptions.Builder().build()
                        )
                        imageProcessor?.processBitmap(resizeBitmap, graphicOverlay)
                    }


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
            // Example 3.
            CameraIntent.Build(this).apply {
                setAction(IntentKey.ACTION_TAKE_PICTURE)
                //setCanMute(false)
                setHasHorizon(true)
                setCropSize(cropSize)
                setCanUiRotation(true)
                //setHorizonColor(Color.RED)
                //setUnusedAreaBorderColor(Color.GREEN)
                //setCroppedJpegQuality(95)
            }.run {
                resultLauncher.launch(this.build())
            }
        }

        // 여러장 촬영.
        findViewById<Button>(R.id.button_multiple_shoot).setOnClickListener {
            CameraIntent.Build(this).apply {
                setAction(IntentKey.ACTION_TAKE_MULTIPLE_PICTURES)
                //setCanMute(false)
                setHasHorizon(true)
                setCropSize(cropSize)
                setCanUiRotation(true)
                //setHorizonColor(Color.RED)
                //setUnusedAreaBorderColor(Color.GREEN)
                //setCroppedJpegQuality(95)
            }.run {
                resultLauncher.launch(this.build())
            }
        }

        // 운행거리 촬영.
        findViewById<Button>(R.id.button_vehicle_number_shoot).setOnClickListener {
            CameraIntent.Build(this).apply {
                setAction(IntentKey.ACTION_TAKE_VEHICLE_NUMBER_PICTURES)
                //setCanMute(false)
                setHasHorizon(true)
                //setCropSize(cropSize)
                setCanUiRotation(true)
                //setHorizonColor(Color.RED)
                //setUnusedAreaBorderColor(Color.GREEN)
                //setCroppedJpegQuality(95)
            }.run {
                resultLauncher.launch(this.build())
            }
        }

        // 운행거리 촬영.
        findViewById<Button>(R.id.button_mileage_shoot).setOnClickListener {
            CameraIntent.Build(this).apply {
                setAction(IntentKey.ACTION_TAKE_MILEAGE_PICTURES)
                //setCanMute(false)
                setHasHorizon(true)
                //setCropSize(cropSize)
                setCanUiRotation(true)
                //setHorizonColor(Color.RED)
                //setUnusedAreaBorderColor(Color.GREEN)
                //setCroppedJpegQuality(95)
            }.run {
                resultLauncher.launch(this.build())
            }
        }

        // 차대번호 촬영.
        findViewById<Button>(R.id.button_vin_number_shoot).setOnClickListener {
            CameraIntent.Build(this).apply {
                setAction(IntentKey.ACTION_TAKE_VIN_NUMBER_PICTURES)
                //setCanMute(false)
                setHasHorizon(true)
                //setCropSize(cropSize)
                setCanUiRotation(true)
                //setHorizonColor(Color.RED)
                //setUnusedAreaBorderColor(Color.GREEN)
                //setCroppedJpegQuality(95)
            }.run {
                resultLauncher.launch(this.build())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        imageProcessor?.run { this.stop() }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageProcessor?.run { this.stop() }
    }
}