package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Base64
import android.util.Size
import androidx.annotation.IntRange
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Created by oooobang on 2018. 5. 11..
 * Bitmap Extension.
 */
private val ALL_EXIF_TAGS = listOf(
    ExifInterface.TAG_IMAGE_WIDTH,
    ExifInterface.TAG_IMAGE_LENGTH,
    ExifInterface.TAG_BITS_PER_SAMPLE,
    ExifInterface.TAG_COMPRESSION,
    ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION,
    ExifInterface.TAG_ORIENTATION,
    ExifInterface.TAG_SAMPLES_PER_PIXEL,
    ExifInterface.TAG_PLANAR_CONFIGURATION,
    ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING,
    ExifInterface.TAG_Y_CB_CR_POSITIONING,
    ExifInterface.TAG_X_RESOLUTION,
    ExifInterface.TAG_Y_RESOLUTION,
    ExifInterface.TAG_RESOLUTION_UNIT,
    ExifInterface.TAG_STRIP_OFFSETS,
    ExifInterface.TAG_ROWS_PER_STRIP,
    ExifInterface.TAG_STRIP_BYTE_COUNTS,
    ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT,
    ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH,
    ExifInterface.TAG_TRANSFER_FUNCTION,
    ExifInterface.TAG_WHITE_POINT,
    ExifInterface.TAG_PRIMARY_CHROMATICITIES,
    ExifInterface.TAG_Y_CB_CR_COEFFICIENTS,
    ExifInterface.TAG_REFERENCE_BLACK_WHITE,
    ExifInterface.TAG_DATETIME,
    ExifInterface.TAG_IMAGE_DESCRIPTION,
    ExifInterface.TAG_MAKE,
    ExifInterface.TAG_MODEL,
    ExifInterface.TAG_SOFTWARE,
    ExifInterface.TAG_ARTIST,
    ExifInterface.TAG_COPYRIGHT,
    ExifInterface.TAG_EXIF_VERSION,
    ExifInterface.TAG_FLASHPIX_VERSION,
    ExifInterface.TAG_COLOR_SPACE,
    ExifInterface.TAG_GAMMA,
    ExifInterface.TAG_PIXEL_X_DIMENSION,
    ExifInterface.TAG_PIXEL_Y_DIMENSION,
    ExifInterface.TAG_COMPONENTS_CONFIGURATION,
    ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL,
    ExifInterface.TAG_MAKER_NOTE,
    ExifInterface.TAG_USER_COMMENT,
    ExifInterface.TAG_RELATED_SOUND_FILE,
    ExifInterface.TAG_DATETIME_ORIGINAL,
    ExifInterface.TAG_DATETIME_DIGITIZED,
    ExifInterface.TAG_OFFSET_TIME,
    ExifInterface.TAG_OFFSET_TIME_ORIGINAL,
    ExifInterface.TAG_OFFSET_TIME_DIGITIZED,
    ExifInterface.TAG_SUBSEC_TIME,
    ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
    ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
    ExifInterface.TAG_EXPOSURE_TIME,
    ExifInterface.TAG_F_NUMBER,
    ExifInterface.TAG_EXPOSURE_PROGRAM,
    ExifInterface.TAG_SPECTRAL_SENSITIVITY,
    ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
    ExifInterface.TAG_OECF,
    ExifInterface.TAG_SENSITIVITY_TYPE,
    ExifInterface.TAG_STANDARD_OUTPUT_SENSITIVITY,
    ExifInterface.TAG_RECOMMENDED_EXPOSURE_INDEX,
    ExifInterface.TAG_ISO_SPEED,
    ExifInterface.TAG_ISO_SPEED_LATITUDE_YYY,
    ExifInterface.TAG_ISO_SPEED_LATITUDE_ZZZ,
    ExifInterface.TAG_SHUTTER_SPEED_VALUE,
    ExifInterface.TAG_APERTURE_VALUE,
    ExifInterface.TAG_BRIGHTNESS_VALUE,
    ExifInterface.TAG_EXPOSURE_BIAS_VALUE,
    ExifInterface.TAG_MAX_APERTURE_VALUE,
    ExifInterface.TAG_SUBJECT_DISTANCE,
    ExifInterface.TAG_METERING_MODE,
    ExifInterface.TAG_LIGHT_SOURCE,
    ExifInterface.TAG_FLASH,
    ExifInterface.TAG_SUBJECT_AREA,
    ExifInterface.TAG_FOCAL_LENGTH,
    ExifInterface.TAG_FLASH_ENERGY,
    ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE,
    ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION,
    ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION,
    ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT,
    ExifInterface.TAG_SUBJECT_LOCATION,
    ExifInterface.TAG_EXPOSURE_INDEX,
    ExifInterface.TAG_SENSING_METHOD,
    ExifInterface.TAG_FILE_SOURCE,
    ExifInterface.TAG_SCENE_TYPE,
    ExifInterface.TAG_CFA_PATTERN,
    ExifInterface.TAG_CUSTOM_RENDERED,
    ExifInterface.TAG_EXPOSURE_MODE,
    ExifInterface.TAG_WHITE_BALANCE,
    ExifInterface.TAG_DIGITAL_ZOOM_RATIO,
    ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
    ExifInterface.TAG_SCENE_CAPTURE_TYPE,
    ExifInterface.TAG_GAIN_CONTROL,
    ExifInterface.TAG_CONTRAST,
    ExifInterface.TAG_SATURATION,
    ExifInterface.TAG_SHARPNESS,
    ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION,
    ExifInterface.TAG_SUBJECT_DISTANCE_RANGE,
    ExifInterface.TAG_IMAGE_UNIQUE_ID,
    ExifInterface.TAG_CAMERA_OWNER_NAME,
    ExifInterface.TAG_BODY_SERIAL_NUMBER,
    ExifInterface.TAG_LENS_SPECIFICATION,
    ExifInterface.TAG_LENS_MAKE,
    ExifInterface.TAG_LENS_MODEL,
    ExifInterface.TAG_LENS_SERIAL_NUMBER,
    ExifInterface.TAG_GPS_VERSION_ID,
    ExifInterface.TAG_GPS_LATITUDE_REF,
    ExifInterface.TAG_GPS_LATITUDE,
    ExifInterface.TAG_GPS_LONGITUDE_REF,
    ExifInterface.TAG_GPS_LONGITUDE,
    ExifInterface.TAG_GPS_ALTITUDE_REF,
    ExifInterface.TAG_GPS_ALTITUDE,
    ExifInterface.TAG_GPS_TIMESTAMP,
    ExifInterface.TAG_GPS_SATELLITES,
    ExifInterface.TAG_GPS_STATUS,
    ExifInterface.TAG_GPS_MEASURE_MODE,
    ExifInterface.TAG_GPS_DOP,
    ExifInterface.TAG_GPS_SPEED_REF,
    ExifInterface.TAG_GPS_SPEED,
    ExifInterface.TAG_GPS_TRACK_REF,
    ExifInterface.TAG_GPS_TRACK,
    ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
    ExifInterface.TAG_GPS_IMG_DIRECTION,
    ExifInterface.TAG_GPS_MAP_DATUM,
    ExifInterface.TAG_GPS_DEST_LATITUDE_REF,
    ExifInterface.TAG_GPS_DEST_LATITUDE,
    ExifInterface.TAG_GPS_DEST_LONGITUDE_REF,
    ExifInterface.TAG_GPS_DEST_LONGITUDE,
    ExifInterface.TAG_GPS_DEST_BEARING_REF,
    ExifInterface.TAG_GPS_DEST_BEARING,
    ExifInterface.TAG_GPS_DEST_DISTANCE_REF,
    ExifInterface.TAG_GPS_DEST_DISTANCE,
    ExifInterface.TAG_GPS_PROCESSING_METHOD,
    ExifInterface.TAG_GPS_AREA_INFORMATION,
    ExifInterface.TAG_GPS_DATESTAMP,
    ExifInterface.TAG_GPS_DIFFERENTIAL,
    ExifInterface.TAG_GPS_H_POSITIONING_ERROR,
    ExifInterface.TAG_INTEROPERABILITY_INDEX,
    ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH,
    ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH,
    //ExifInterface.TAG_THUMBNAIL_ORIENTATION,
    ExifInterface.TAG_DNG_VERSION,
    ExifInterface.TAG_DEFAULT_CROP_SIZE,
    ExifInterface.TAG_ORF_THUMBNAIL_IMAGE,
    ExifInterface.TAG_ORF_PREVIEW_IMAGE_START,
    ExifInterface.TAG_ORF_PREVIEW_IMAGE_LENGTH,
    ExifInterface.TAG_ORF_ASPECT_FRAME,
    ExifInterface.TAG_RW2_SENSOR_BOTTOM_BORDER,
    ExifInterface.TAG_RW2_SENSOR_LEFT_BORDER,
    ExifInterface.TAG_RW2_SENSOR_RIGHT_BORDER,
    ExifInterface.TAG_RW2_SENSOR_TOP_BORDER,
    ExifInterface.TAG_RW2_ISO,
    ExifInterface.TAG_RW2_JPG_FROM_RAW,
    ExifInterface.TAG_XMP,
    ExifInterface.TAG_NEW_SUBFILE_TYPE,
    ExifInterface.TAG_SUBFILE_TYPE
)

private val DO_NOT_COPY_EXIF_TAGS = listOf(
    // Dimension-related tags, which might change after cropping.
    ExifInterface.TAG_IMAGE_WIDTH,
    ExifInterface.TAG_IMAGE_LENGTH,
    ExifInterface.TAG_PIXEL_X_DIMENSION,
    // Thumbnail-related tags. Currently we do not create thumbnail for cropped images.
    ExifInterface.TAG_PIXEL_Y_DIMENSION,
    // Our primary image is always Jpeg.
    ExifInterface.TAG_COMPRESSION,
    ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT,
    ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH,
    ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH,
    ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH/*,
        ExifInterface.TAG_THUMBNAIL_ORIENTATION*/
)

// 저장.
fun Bitmap?.save(
    context: Context? = null,
    isPublicDirectory: Boolean = false,
    filename: String = System.currentTimeMillis().toString(),
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
    exifInterface: ExifInterface? = null,
    @IntRange(from = 1, to = 100)
    jpegQuality: Int = 95,
    action: ((path: String?, uri: Uri?) -> Unit)? = null
): String? {
    var path: String? = null
    val extension = when (format) {
        Bitmap.CompressFormat.PNG -> "png"
        Bitmap.CompressFormat.JPEG -> "jpg"
        else -> "webp"
    }

    if (isPublicDirectory) {
        /* 공용 저장소 사용. */
        // 하위 디렉토리명.
        val childDirectory = context?.packageName?.split('.')?.last() ?: "adbcamerax"
        // 공유 저장소 사용 시 Android Q 대응.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 1)
                put(MediaStore.Images.Media.MIME_TYPE, "image/*")
                put(MediaStore.Images.Media.DISPLAY_NAME, "$filename.$extension")
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/$childDirectory"
                )
            }

            // 저장.
            context?.contentResolver?.let { contentResolver ->
                // ContentResolver을 통해 insert를 해주고 해당 insert가 되는 위치의 Uri를 리턴받는다.
                // 이후로는 해당 Uri를 통해 파일 관리를 해줄 수 있다.
                val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                contentResolver.insert(collection, contentValues)?.let { uri ->
                    // Debug.
                    Timber.i(">>>>> Q URI : %s", uri)

                    // 반환용.
                    path = uri.toString()

                    // 파일 쓰기.
                    var fileOutputStream: FileOutputStream? = null
                    var parcelFileDescriptor: ParcelFileDescriptor? = null
                    try {
                        // Uri(item)의 위치에 파일을 생성해준다.
                        parcelFileDescriptor = contentResolver.openFileDescriptor(
                            uri, "w", null
                        )
                        parcelFileDescriptor?.fileDescriptor?.also { fileDescriptor ->
                            fileOutputStream = FileOutputStream(fileDescriptor)
                            this?.compress(format, jpegQuality, fileOutputStream)
                            contentResolver.update(uri, contentValues, null, null)
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    } finally {
                        fileOutputStream?.close()
                        parcelFileDescriptor?.close()
                    }
                    contentValues.clear()

                    // 파일을 모두 write하고 다른곳에서 사용할 수 있도록 0으로 업데이트를 해줍니다.
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)

                    // Exif 태그 데이터를 이미지 파일에 저장.
                    try {
                        parcelFileDescriptor = contentResolver.openFileDescriptor(
                            uri, "rw", null
                        )
                        parcelFileDescriptor?.fileDescriptor?.also { fileDescriptor ->
                            exifInterface?.let { exif ->
                                val saveExifInterface = ExifInterface(fileDescriptor)
                                val exifInterfaceTags = ALL_EXIF_TAGS.toMutableList()
                                exifInterfaceTags.removeAll(DO_NOT_COPY_EXIF_TAGS)
                                exifInterfaceTags.forEach { tag ->
                                    exif.getAttribute(tag)?.let { originValue ->
                                        saveExifInterface.setAttribute(tag, originValue)
                                    }
                                }
                                saveExifInterface.saveAttributes()
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    } finally {
                        parcelFileDescriptor?.close()
                    }
                }
            }
            action?.invoke(path, path?.toUri())
        } else {
            var fileOutputStream: FileOutputStream? = null
            try {
                @Suppress("DEPRECATION")
                val directory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    childDirectory
                )
                if (!directory.mkdirs()) {
                    Timber.i(">>>>> Directory not created : %s", directory)
                }

                path = "${directory.absolutePath}/$filename.$extension"
                fileOutputStream = FileOutputStream(path)
                this?.compress(format, jpegQuality, fileOutputStream)

                // Exif 태그 데이터를 이미지 파일에 저장.
                exifInterface?.let { exif ->
                    val saveExifInterface = ExifInterface(File(path!!))
                    val exifInterfaceTags = ALL_EXIF_TAGS.toMutableList()
                    exifInterfaceTags.removeAll(DO_NOT_COPY_EXIF_TAGS)
                    exifInterfaceTags.forEach { tag ->
                        exif.getAttribute(tag)?.let { originValue ->
                            saveExifInterface.setAttribute(tag, originValue)
                        }
                    }
                    saveExifInterface.saveAttributes()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                fileOutputStream?.close()
            }

            // Media Scanning.
            context?.mediaScanning(path) { scanPath, scanUri ->
                action?.invoke(scanPath, scanUri)
            }
        }
    } else {
        /* 내부 저장소 사용. */
        var fileOutputStream: FileOutputStream? = null
        try {
            val directory = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (directory?.mkdirs() != true) {
                Timber.i(">>>>> Directory not created : %s", directory)
            }
            path = "${directory?.absolutePath}/$filename.$extension"
            fileOutputStream = FileOutputStream(path)
            this?.compress(format, jpegQuality, fileOutputStream)

            // Exif 태그 데이터를 이미지 파일에 저장.
            exifInterface?.let { exif ->
                val saveExifInterface = ExifInterface(File(path!!))
                val exifInterfaceTags = ALL_EXIF_TAGS.toMutableList()
                exifInterfaceTags.removeAll(DO_NOT_COPY_EXIF_TAGS)
                exifInterfaceTags.forEach { tag ->
                    exif.getAttribute(tag)?.let { originValue ->
                        saveExifInterface.setAttribute(tag, originValue)
                    }
                }
                saveExifInterface.saveAttributes()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            fileOutputStream?.close()
        }
    }

    // Debug.
    Timber.i(">>>>> Save Bitmap Finish : %s", path)

    return path
}

// toByteArray.
fun Bitmap?.toByteArray(format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG): ByteArray? {
    val stream = ByteArrayOutputStream()
    this?.compress(format, 95, stream)
    return stream.toByteArray()
}

// 특정 컬러 투명처리.
fun Bitmap?.toTransparentBitmap(replaceThisColor: Int): Bitmap? {
    if (this != null) {
        val picw = this.width
        val pich = this.height
        val pix = IntArray(picw * pich)
        this.getPixels(pix, 0, picw, 0, 0, picw, pich)
        for (y in 0 until pich) {
            for (x in 0 until picw) {
                val index = y * picw + x
                if (pix[index] == replaceThisColor) {
                    pix[index] = Color.TRANSPARENT
                }
            }
            for (x in picw - 1 downTo 0) {
                val index = y * picw + x
                if (pix[index] == replaceThisColor) {
                    pix[index] = Color.TRANSPARENT
                }
            }
        }
        return Bitmap.createBitmap(pix, picw, pich, Bitmap.Config.ARGB_8888)
    }
    return null
}

// 이미지 Uri에서 회전후 중앙 기준 Crop한 Bitmap 반환.
fun Bitmap.rotateAndCenterCrop(
    cropSize: Size,
    rotationDegrees: Int
): Bitmap? {
    val matrix = Matrix().apply {
        preRotate(rotationDegrees.toFloat())
    }
    if (cropSize.width < width && cropSize.height < height) {
        val (widthCrop, heightCrop) = when (rotationDegrees) {
            90, 270 -> Pair(cropSize.height, cropSize.width)
            else -> Pair(cropSize.width, cropSize.height)
        }
        return Bitmap.createBitmap(
            this,
            (width / 2) - (widthCrop / 2),
            (height / 2) - (heightCrop / 2),
            widthCrop,
            heightCrop,
            matrix,
            true
        )
    } else {
        return Bitmap.createBitmap(
            this,
            0,
            0,
            width,
            height,
            matrix,
            true
        )
    }
}

// 이미지 Uri에서 중앙 기준 Crop한 Bitmap 반환.
fun Bitmap.centerCrop(
    cropPercent: Array<Float>,
    rotationDegrees: Int? = null
): Bitmap? {
    val (widthRatio, heightRatio) = when (rotationDegrees) {
        90, 270 -> Pair(cropPercent[1], cropPercent[0])
        else -> Pair(cropPercent[0], cropPercent[1])
    }
    val widthCrop = (width * widthRatio).toInt()
    val heightCrop = (height * heightRatio).toInt()
    val x = (width / 2) - (widthCrop / 2)
    val y = (height / 2) - (heightCrop / 2)
    val cropRect = Rect(x, y, x + widthCrop, y + heightCrop)
    val cropSize = Size(widthCrop, heightCrop)
    return Bitmap.createBitmap(
        this,
        cropRect.left,
        cropRect.top,
        cropSize.width,
        cropSize.height
    )
}

// 이미지 Uri에서 회전후 중앙 기준 Crop한 Bitmap 반환.
fun Bitmap.rotateAndCenterCrop(
    cropPercent: Array<Float>,
    rotationDegrees: Int
): Bitmap? {
    val matrix = Matrix().apply {
        preRotate(rotationDegrees.toFloat())
    }
    val (widthRatio, heightRatio) = when (rotationDegrees) {
        90, 270 -> Pair(cropPercent[1], cropPercent[0])
        else -> Pair(cropPercent[0], cropPercent[1])
    }
    val widthCrop = (width * widthRatio).toInt()
    val heightCrop = (height * heightRatio).toInt()
    val x = (width / 2) - (widthCrop / 2)
    val y = (height / 2) - (heightCrop / 2)
    val cropRect = Rect(x, y, x + widthCrop, y + heightCrop)
    val cropSize = Size(widthCrop, heightCrop)
    return Bitmap.createBitmap(
        this,
        cropRect.left,
        cropRect.top,
        cropSize.width,
        cropSize.height,
        matrix,
        true
    )
}

// 리사이징.
fun Bitmap?.resize(resizePixcel: Int): Bitmap? {
    try {
        return this?.let { bitmap ->
            val sample = if (width >= height) {
                resizePixcel.toFloat() / width.toFloat()
            } else {
                resizePixcel.toFloat() / height.toFloat()
            }
            val (sampleWidth, sampleHeight) = if (sample < 1) {
                Pair((width.toFloat() * sample).toInt(), (height.toFloat() * sample).toInt())
            } else {
                Pair(width, height)
            }
            Bitmap.createScaledBitmap(bitmap, sampleWidth, sampleHeight, true)
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return null
}

// 리사이징.
fun Bitmap?.optimumResize(resize: Int): Bitmap? {
    try {
        return this?.let { bitmap ->
//            val byteArray = bitmap.toByteArray()
//            val options = BitmapFactory.Options().apply {
//                inJustDecodeBounds = true
//            }
//            BitmapFactory.decodeByteArray(byteArray, 0, byteArray?.size ?: 0, options)
//            var width = options.outWidth
//            var height = options.outHeight
//            var sampleSize = 1
            while (true) {
                if (width / 2 < resize || height / 2 < resize) {
                    break
                }
                width /= 2
                height /= 2
//                sampleSize *= 2
            }
//            options.inSampleSize = sampleSize
            Bitmap.createScaledBitmap(bitmap, width, height, true)
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return null
}

// Base64.
fun Bitmap?.toBase64(
    flags: Int = Base64.NO_WRAP,
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
): String? {
    return Base64.encodeToString(toByteArray(format), flags)
}
