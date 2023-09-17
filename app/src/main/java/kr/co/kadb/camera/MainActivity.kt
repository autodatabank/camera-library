package kr.co.kadb.camera

import android.content.Intent
import android.graphics.Color
import android.graphics.RectF
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toRect
import androidx.exifinterface.media.ExifInterface
import kr.co.kadb.cameralibrary.presentation.CameraIntent
import kr.co.kadb.cameralibrary.presentation.model.CropSize
import kr.co.kadb.cameralibrary.presentation.widget.extension.exifInterface
import kr.co.kadb.cameralibrary.presentation.widget.extension.getParcelable
import kr.co.kadb.cameralibrary.presentation.widget.extension.getSerializable
import kr.co.kadb.cameralibrary.presentation.widget.util.BitmapHelper
import kr.co.kadb.cameralibrary.presentation.widget.util.IntentKey
import kr.co.kadb.cameralibrary.presentation.widget.util.UriHelper
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    // Crop Size.
    private val cropSize = CropSize(0.8f, 0.7f)

    // Activity for result.
    // Example 2, 3.
    private var resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val intent = result.data
        if (result.resultCode == RESULT_OK) {
            // 한 장, 여러 장.
            when (intent?.action) {
                IntentKey.ACTION_TAKE_PICTURE -> {
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

                    // 촬영 원본 이미지.
                    findViewById<ImageView>(R.id.imageview).setImageURI(imageUri)
                    // 촬영 원본을 크롭 또는 리사이즈한 이미지.
                    findViewById<ImageView>(R.id.imageview_thumbnail).setImageBitmap(
                        resizeBitmap
                    )
                }

                IntentKey.ACTION_TAKE_MULTIPLE_PICTURES -> {
                    // 여러장.
                    // 이미지 URI.
                    val imageUris =
                        intent.getSerializable(IntentKey.EXTRA_URIS, ArrayList::class.java)
                    // 이미지 사이즈.
                    val imageSizes =
                        intent.getSerializable(IntentKey.EXTRA_SIZES, ArrayList::class.java)

                    // Debug.
                    Timber.d(">>>>> imageUris : $imageUris")
                    Timber.d(">>>>> imageSizes : $imageSizes")
                }

                IntentKey.ACTION_DETECT_MILEAGE_IN_PICTURES,
                IntentKey.ACTION_DETECT_VIN_NUMBER_IN_PICTURES -> {
                    // 이미지 가로.
                    val imageWidth = intent.getIntExtra(IntentKey.EXTRA_WIDTH, 0)
                    // 이미지 세로.
                    val imageHeight = intent.getIntExtra(IntentKey.EXTRA_HEIGHT, 0)
                    // 감지한 텍스트.
                    val detectText = intent.getStringExtra(IntentKey.EXTRA_DETECT_TEXT)
                    // 감지한 Rect.
                    val detectRect =
                        intent.getParcelable(IntentKey.EXTRA_DETECT_RECT, RectF::class.java)

                    // Debug.
                    Timber.i(">>>>> ACTION_DETECT_IN_PICTURES : $detectText")
                    Timber.i(">>>>> ACTION_DETECT_IN_PICTURES > $detectRect")
                    Timber.i(">>>>> ACTION_DETECT_IN_PICTURES > $imageWidth x $imageHeight")

                    // 감지한 텍스트.
                    findViewById<TextView>(R.id.textview).text = detectText

                    // 이미지 URI.
                    val imageUri = intent.data ?: return@registerForActivityResult
                    val exifInterface = imageUri.exifInterface(baseContext)
                    val (width, height) = Pair(
                        exifInterface?.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0) ?: 0,
                        exifInterface?.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0) ?: 0
                    )

                    // Debug.
                    Timber.i(">>>>> ACTION_DETECT_IN_PICTURES > $width x $height")

                    // 촬영 원본 이미지.
                    findViewById<ImageView>(R.id.imageview).setImageURI(imageUri)

                    // 이미지 중앙을 기준으로 원본 사이즈에서 가로:70% 세로:50% 크롭.
                    val cropBitmap = detectRect?.let {
                        UriHelper.rotateAndCrop(baseContext, imageUri, it.toRect())
                    }

                    // Debug.
                    Timber.i(">>>>> ACTION_DETECT_IN_PICTURES > cropBitmap : ${cropBitmap?.width} x ${cropBitmap?.height}")

                    // 촬영 원본을 크롭 또는 리사이즈한 이미지.
                    findViewById<ImageView>(R.id.imageview_thumbnail).setImageBitmap(cropBitmap)
                }
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
                setCanMute(false)
                setHasHorizon(true)
                setCropSize(cropSize)
                setCanUiRotation(true)
                setHorizonColor(Color.RED)
                setUnusedAreaBorderColor(Color.GREEN)
                setCroppedJpegQuality(95)
                setSaveCropedImage(false)
            }.run {
                resultLauncher.launch(this.build())
            }
        }

        // 여러장 촬영.
        findViewById<Button>(R.id.button_multiple_shoot).setOnClickListener {
            CameraIntent.Build(this).apply {
                setAction(IntentKey.ACTION_TAKE_MULTIPLE_PICTURES)
                setCanMute(false)
                setHasHorizon(true)
                setCropSize(cropSize)
                setCanUiRotation(true)
                setHorizonColor(Color.RED)
                setUnusedAreaBorderColor(Color.GREEN)
                setCroppedJpegQuality(95)
                setSaveCropedImage(false)
            }.run {
                resultLauncher.launch(this.build())
            }
        }

        // 주행거리 촬영.
        findViewById<Button>(R.id.button_mileage_shoot).setOnClickListener {
            CameraIntent.Build(this).apply {
                setAction(IntentKey.ACTION_DETECT_MILEAGE_IN_PICTURES)
                setCanMute(false)
                setHasHorizon(true)
                setCanUiRotation(true)
                setHorizonColor(Color.RED)
            }.run {
                resultLauncher.launch(this.build())
            }
        }

        // 차대번호 촬영.
        findViewById<Button>(R.id.button_vin_number_shoot).setOnClickListener {
            CameraIntent.Build(this).apply {
                setAction(IntentKey.ACTION_DETECT_VIN_NUMBER_IN_PICTURES)
                setCanMute(false)
                setHasHorizon(true)
                setCanUiRotation(true)
                setHorizonColor(Color.RED)
            }.run {
                resultLauncher.launch(this.build())
            }
        }
    }
}