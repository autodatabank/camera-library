package kr.co.kadb.cameralibrary.presentation.widget.util

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kr.co.kadb.cameralibrary.presentation.widget.extension.resize
import kr.co.kadb.cameralibrary.presentation.widget.extension.rotateAndCenterCrop
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.sqrt

internal class ImageAnalyzer(private val cropPercent: Array<Float>) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        val image = imageProxy.image ?: return
        var bitmap: Bitmap? = ImageUtils.convertYuv420888ImageToBitmap(image)
        bitmap = bitmap?.rotateAndCenterCrop(
            cropPercent,
            imageProxy.imageInfo.rotationDegrees
        )

        //compress bitmap
        val resizedBitmap = bitmap.resize(480) ?: return
        val rgbaMat = Mat()
        val grayMat = Mat()
        val bwMat = Mat()
        val hierarchy = Mat()

        // Bitmap > Mat.
        Utils.bitmapToMat(resizedBitmap, rgbaMat)
        // RGB > Gray.
        Imgproc.cvtColor(rgbaMat, grayMat, Imgproc.COLOR_RGB2GRAY)
        // 히스토그램 평활화.
        Imgproc.equalizeHist(grayMat, grayMat)
//        Imgproc.adaptiveThreshold(
//            grayMat,
//            grayMat,
//            255.0,
//            Imgproc.ADAPTIVE_THRESH_MEAN_C,
//            Imgproc.THRESH_BINARY,
//            15,
//            40.0
//        )
        // 윤곽선.
        // grayMat 입력.
        // bwMat 출력.
        // threshold1 최소 임계값.
        // threshold2 최대 임계값.
        Imgproc.Canny(grayMat, bwMat, 100.0, 250.0, 3, false)

        //find largest contour
        val contours: List<MatOfPoint> = ArrayList()

        // 윤곽선 검출.
        // image : 입력영상, 0이 아닌 픽셀을 객체로 간주함.
        // contours : 검출된 윤곽선 좌표
        // hierarchy : 윤곽선 계층정보
        // mode : 윤곽선 검출 모드
        // method : 좌표 값 이동 오프셋, 기본값 (0,0)
        Imgproc.findContours(
            bwMat,
            contours,
            hierarchy,
            //Imgproc.RETR_LIST,
            Imgproc.RETR_TREE,
            Imgproc.CHAIN_APPROX_NONE
        )
//
//        contours.forEach { matOfPoint ->
//            val rect = Imgproc.boundingRect(matOfPoint)
//            val bitmap = Bitmap.createBitmap(resizedBitmap, rect.tl().x.toInt(), rect.tl().y.toInt(), rect.width, rect.height)
//
//            lifecycleScope.launch {
//                binding.adbCameralibraryImageviewThumbnail.setImageBitmap(bitmap)
//            }
//        }
//        for (int idx = 0;
//            idx >= 0;
//            idx = (int) hierarchy.get (0, idx)[0]) {
//            MatOfPoint matOfPoint = contours . get (idx);
//            Rect rect = Imgproc.boundingRect (matOfPoint);
//            if (rect.width < 30 || rect.height < 30 || rect.width <= rect.height || rect.x < 20 || rect.y < 20 || rect.width <= rect.height * 3 || rect.width >= rect.height * 6) continue;
//            // 사각형 크기에 따라 출력 여부 결정
//            // ROI 출력
//            Bitmap roi = Bitmap.createBitmap(myBitmap, (int)rect.tl().x, (int)rect.tl().y, rect.width, rect.height);
//            ImageView imageView1 = (ImageView)findViewById(R.id.image_result_ROI);
//            imageView1.setImageBitmap(roi);
//            }

        val scalar = Scalar(255.0, 255.0, 0.0)
        val thickness = 2


//        contours.forEach { matOfPoint ->
//            val approxCurve = MatOfPoint2f()
//            val curve = MatOfPoint2f(*matOfPoint.toArray())
//            val approxDistance = Imgproc.arcLength(curve, true) * 0.02
//
//            // 다각형 곡선 근사화.
//            // curve: 입력.
//            // approxCurve: 출력.
//            // epsilon: 직선과의 허용 거리, 크면 좌표점의 개수가 적어진다
//            // closed: true 닫힌곡선, false 열린곡선.
//            Imgproc.approxPolyDP(curve, approxCurve, approxDistance, true)
//
//            // 면적.
//            val contourArea = Imgproc.contourArea(matOfPoint)
//            val numberVertices = approxCurve.total()
//
//
////            if (abs(contourArea) < 0.1) {
////                return
////            }
//
//
//            // Rectangle detected
//            if (contourArea > 10.0 && numberVertices in 4..6) {
//                val cos: MutableList<Double> = ArrayList()
//                for (j in 2 until numberVertices + 1) {
//                    cos.add(
//                        angle(
//                            approxCurve.toArray()[(j % numberVertices).toInt()],
//                            approxCurve.toArray()[(j - 2).toInt()],
//                            approxCurve.toArray()[(j - 1).toInt()]
//                        )
//                    )
//                }
//                cos.sort()
//
//                val mincos = cos[0]
//                val maxcos = cos[cos.size - 1]
//                if ((numberVertices == 4L) && (mincos >= -0.1) && (maxcos <= 0.3)) {
//                    val rect = Imgproc.boundingRect(matOfPoint)
//                    Imgproc.rectangle(bwMat, rect, scalar, thickness)
////                Imgproc.rectangle(rgbaMat, rect, scalar, thickness)
//                }
//            }
//        }

        val maxArea = 50.0
        val minWidth = 50
        val minHeight = 50
        val approxCurve = MatOfPoint2f()
//        val largestContours: MutableList<MatOfPoint?> = ArrayList()
        contours.forEach { matOfPoint ->
            val contourArea = Imgproc.contourArea(matOfPoint)
            //compare this contour to the previous largest contour found
            if (contourArea > maxArea) {
                //check if this contour is a square
                val curve = MatOfPoint2f(*matOfPoint.toArray())
                val contourSize = matOfPoint.total().toInt()
                Imgproc.approxPolyDP(curve, approxCurve, contourSize * 0.5, true)
//                val approxDistance = Imgproc.arcLength(curve, true) * 0.02
//                Imgproc.approxPolyDP(curve, approxCurve, approxDistance, true)
                if (approxCurve.total() == 4L) {
//                    maxArea = contourArea
//                    largestContours.add(matOfPoint)
                    Imgproc.boundingRect(matOfPoint).takeIf {
                        it.width >= minWidth && it.height >= minHeight
                    }?.run {
                        Imgproc.rectangle(rgbaMat, this, scalar, thickness)
                    }
                }
            }
        }
//        if (largestContours.size >= 1) {
////            val temp_largest = largestContours[largestContours.size - 1]
////            largestContours = ArrayList()
////            largestContours.add(temp_largest)
//            Imgproc.cvtColor(bwMat, bwMat, Imgproc.COLOR_BayerBG2RGB)
//            Imgproc.drawContours(rgbaMat, largestContours, -1, scalar, thickness)
//        }


        Utils.matToBitmap(rgbaMat, resizedBitmap)
//        Utils.matToBitmap(rgbaMat, resizedBitmap)
//
//
//        lifecycleScope.launch {
//            binding.adbCameralibraryImageviewThumbnail.setImageBitmap(resizedBitmap)
//        }
    }


    private fun angle(pt1: Point, pt2: Point, pt0: Point): Double {
        val dx1: Double = pt1.x - pt0.x
        val dy1: Double = pt1.y - pt0.y
        val dx2: Double = pt2.x - pt0.x
        val dy2: Double = pt2.y - pt0.y
        return (dx1 * dx2 + dy1 * dy2) / sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10)
    }
}