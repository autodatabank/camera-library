//package kr.co.kadb.cameralibrary.presentation.widget.util
//
//import android.graphics.Bitmap
//import androidx.camera.core.ImageAnalysis
//import androidx.camera.core.ImageProxy
//import kr.co.kadb.cameralibrary.presentation.widget.extension.rotateAndCenterCrop
//
//
//internal class ImageAnalyzer(
//    private val cropPercent: Array<Float>,
//    private val action: ((bitmap: Bitmap?) -> Unit)? = null
//) : ImageAnalysis.Analyzer {
//    private var needUpdateGraphicOverlayImageSourceInfo = false
//    override fun analyze(imageProxy: ImageProxy) {
//        val image = imageProxy.image ?: return
//        var bitmap: Bitmap? = ImageUtils.convertYuv420888ImageToBitmap(image)
//        bitmap = bitmap?.rotateAndCenterCrop(
//            cropPercent,
//            imageProxy.imageInfo.rotationDegrees
//        )
//
////        //compress bitmap
////        val resizedBitmap = bitmap.resize(1024) ?: return
////        val rgbaMat = Mat()
////        val grayMat = Mat()
////        val bwMat = Mat()
////        val hierarchy = Mat()
////
////        // 윤곽 List.
////        val contours: List<MatOfPoint> = ArrayList()
////        // 색상.
////        val scalar = Scalar(255.0, 255.0, 0.0)
////        // 두께.
////        val thickness = 5
////
////
////        // Bitmap > Mat.
////        Utils.bitmapToMat(resizedBitmap, rgbaMat)
////        // RGB > Gray.
////        Imgproc.cvtColor(rgbaMat, grayMat, Imgproc.COLOR_RGB2GRAY)
////        // 히스토그램 평활화.
////        //Imgproc.equalizeHist(grayMat, grayMat)
////
////
////        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
////        //Imgproc.erode(grayMat, grayMat, kernel)
////        //Imgproc.dilate(grayMat, grayMat, kernel)
//////        Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_DILATE, kernel)
//////        Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_ERODE, kernel)
//////        Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_GRADIENT, kernel)
////////        Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_CLOSE, kernel)
////////        Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_TOPHAT, kernel)
////////        Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_BLACKHAT, kernel)
//////
//////        /* Gaussian Blur. */
//////        Imgproc.GaussianBlur(grayMat, grayMat, kernel.size(), 0.0)
////
////        // 윤곽선.
////        /* Canny */
////        // grayMat 입력.
////        // bwMat 출력.
////        // threshold1 최소 임계값.
////        // threshold2 최대 임계값.
//////        Imgproc.Canny(grayMat, bwMat, 100.0, 100.0)//, 3, false)
////
////        /* Global Threshold. */
////        //Imgproc.threshold(grayMat, bwMat, 0.0, 255.0, Imgproc.THRESH_BINARY+Imgproc.THRESH_OTSU)
////
////        /* Adaptive Threshold. */
////        // adaptiveMethod: Adaptive Threshold 알고리즘(ADAPTIVE_THRESH_MEAN_C, ADAPTIVE_THRESH_GAUSSIAN_C)
////        Imgproc.adaptiveThreshold(
////            grayMat,
////            bwMat,
////            255.0,
////            Imgproc.ADAPTIVE_THRESH_MEAN_C,
////            Imgproc.THRESH_BINARY_INV,
////            3,
////            2.0
////        )
////
////        //Imgproc.erode(grayMat, grayMat, kernel)
////        //Imgproc.dilate(grayMat, grayMat, kernel)
//////        Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_DILATE, kernel)
//////        Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_ERODE, kernel)
//////        Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_GRADIENT, kernel)
////////        Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_CLOSE, kernel)
////////        Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_TOPHAT, kernel)
////////        Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_BLACKHAT, kernel)
//////
//////        /* Gaussian Blur. */
//////        Imgproc.GaussianBlur(grayMat, grayMat, kernel.size(), 0.0)
////
////
////        // 윤곽선 찾기.
////        // image : 입력영상, 0이 아닌 픽셀을 객체로 간주함.
////        // contours : 검출된 윤곽선 좌표
////        // hierarchy : 윤곽선 계층정보
////        // mode : 윤곽선 검출 모드(RETR_LIST: 이미지에서 발견한 모든 Contour들을 계층에 상관 없이 나열, RETR_TREE: 모든 Contour들의 관계를 명확히 해서 리턴)
////        // method : 좌표 값 이동 오프셋, 기본값 (0,0)
////        Imgproc.findContours(
////            bwMat,
////            contours,
////            hierarchy,
////            // 윤곽선 검출 모드.
//////            Imgproc.RETR_LIST,
//////            Imgproc.RETR_TREE,
////            Imgproc.RETR_EXTERNAL,
////            // 윤곽선 근사화 방법.
//////            Imgproc.CHAIN_APPROX_NONE
////            Imgproc.CHAIN_APPROX_SIMPLE
////        )
////
////        //Imgproc.cornerHarris(grayMat, bwMat, 2, 3, 0.04)
////        //Imgproc.goodFeaturesToTrack(grayMat, 25, 2, 0.01, 10)
////
//////        for (int idx = 0;
//////            idx >= 0;
//////            idx = (int) hierarchy.get (0, idx)[0]) {
//////            MatOfPoint matOfPoint = contours . get (idx);
//////            Rect rect = Imgproc.boundingRect (matOfPoint);
//////            if (rect.width < 30 || rect.height < 30 || rect.width <= rect.height || rect.x < 20 || rect.y < 20 || rect.width <= rect.height * 3 || rect.width >= rect.height * 6) continue;
//////            // 사각형 크기에 따라 출력 여부 결정
//////            // ROI 출력
//////            Bitmap roi = Bitmap.createBitmap(myBitmap, (int)rect.tl().x, (int)rect.tl().y, rect.width, rect.height);
//////            ImageView imageView1 = (ImageView)findViewById(R.id.image_result_ROI);
//////            imageView1.setImageBitmap(roi);
//////            }
////
////
//////        contours.forEach { matOfPoint ->
//////            val approxCurve = MatOfPoint2f()
//////            val curve = MatOfPoint2f(*matOfPoint.toArray())
//////            val approxDistance = Imgproc.arcLength(curve, true) * 0.02
//////
//////            // 다각형 곡선 근사화.
//////            // curve: 입력.
//////            // approxCurve: 출력.
//////            // epsilon: 직선과의 허용 거리, 크면 좌표점의 개수가 적어진다
//////            // closed: true 닫힌곡선, false 열린곡선.
//////            Imgproc.approxPolyDP(curve, approxCurve, approxDistance, true)
//////
//////            // 면적.
//////            val contourArea = Imgproc.contourArea(matOfPoint)
//////            val numberVertices = approxCurve.total()
//////
//////
////////            if (abs(contourArea) < 0.1) {
////////                return
////////            }
//////
//////
//////            // Rectangle detected
//////            if (contourArea > 10.0 && numberVertices in 4..6) {
//////                val cos: MutableList<Double> = ArrayList()
//////                for (j in 2 until numberVertices + 1) {
//////                    cos.add(
//////                        angle(
//////                            approxCurve.toArray()[(j % numberVertices).toInt()],
//////                            approxCurve.toArray()[(j - 2).toInt()],
//////                            approxCurve.toArray()[(j - 1).toInt()]
//////                        )
//////                    )
//////                }
//////                cos.sort()
//////
//////                val mincos = cos[0]
//////                val maxcos = cos[cos.size - 1]
//////                if ((numberVertices == 4L) && (mincos >= -0.1) && (maxcos <= 0.3)) {
//////                    val rect = Imgproc.boundingRect(matOfPoint)
//////                    Imgproc.rectangle(bwMat, rect, scalar, thickness)
////////                Imgproc.rectangle(rgbaMat, rect, scalar, thickness)
//////                }
//////            }
//////        }
////
////
//////        contours.forEach { thisContour ->
////////            //check if this contour is a square
////////            val curve = MatOfPoint2f(*matOfPoint.toArray())
////////            val contourSize = matOfPoint.total().toInt()
//////
//////            val thisContour2f = MatOfPoint2f()
//////            val approxContour = MatOfPoint()
//////            val approxContour2f = MatOfPoint2f()
//////
//////            thisContour.convertTo(thisContour2f, CvType.CV_32FC2)
//////            Imgproc.approxPolyDP(thisContour2f, approxContour2f, 2.0, true)
//////            approxContour2f.convertTo(approxContour, CvType.CV_32S)
//////
//////            val contourArea = Imgproc.contourArea(thisContour)
//////
//////            if (contourArea > 100 && approxContour.size().height == 4.0) {
//////                Imgproc.boundingRect(approxContour).also {
////////                    it.width >= minWidth && it.height >= minHeight
////////                }?.run {
////////                    Timber.i(">>>>> OPENCV => contourArea : $contourArea , approxCurve : ${approxCurve.total()} , contourSize : $contourSize , ${this.width} x ${this.height}")
//////                    Imgproc.rectangle(bwMat, it, scalar, thickness)
//////                }
//////            }
//////        }
////
////
////        val minArea = 500.0
////        val minWidth = 100
////        val minHeight = 50
////        val approxCurve = MatOfPoint2f()
//////        val largestContours: MutableList<MatOfPoint?> = ArrayList()
////        contours.forEach { matOfPoint ->
////            val contourArea = Imgproc.contourArea(matOfPoint)
////            //compare this contour to the previous largest contour found
////            //Timber.i(">>>>> OPENCV => Area : $contourArea , $minArea")
////            if (contourArea > minArea) {
////                //check if this contour is a square
////                val curve = MatOfPoint2f(*matOfPoint.toArray())
//////                val contourSize = matOfPoint.total().toInt()
////                //
////                val epsilon = Imgproc.arcLength(curve, true) * 0.05
////                Imgproc.approxPolyDP(curve, approxCurve, epsilon, true)
//////                if (approxCurve.total() > 4L) {
////                if (approxCurve.total() in 4L..16L) {
//////                    maxArea = contourArea
//////                    largestContours.add(matOfPoint)
////                    Imgproc.boundingRect(matOfPoint).takeIf {
////                        it.width >= minWidth && it.height >= minHeight
////                    }?.run {
////                        Timber.i(
////                            ">>>>> OPENCV => contourArea : $contourArea , approxCurve : ${approxCurve.total()} , ${this.width} x ${this.height}"
////                        )
////                        Imgproc.rectangle(bwMat, this, scalar, thickness)
////                    }
////                }
////            }
////        }
////
////
//////        if (largestContours.size >= 1) {
////////            val temp_largest = largestContours[largestContours.size - 1]
////////            largestContours = ArrayList()
////////            largestContours.add(temp_largest)
//////            Imgproc.cvtColor(bwMat, bwMat, Imgproc.COLOR_BayerBG2RGB)
//////            Imgproc.drawContours(rgbaMat, largestContours, -1, scalar, thickness)
//////        }
////
////
////        //
////        Utils.matToBitmap(bwMat, resizedBitmap)
//
//        //
//        imageProxy.close()
//
//        //
//        action?.invoke(resizedBitmap)
//    }
//
//
//    /*private fun angle(pt1: Point, pt2: Point, pt0: Point): Double {
//        val dx1: Double = pt1.x - pt0.x
//        val dy1: Double = pt1.y - pt0.y
//        val dx2: Double = pt2.x - pt0.x
//        val dy2: Double = pt2.y - pt0.y
//        return (dx1 * dx2 + dy1 * dy2) / sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10)
//    }*/
//}