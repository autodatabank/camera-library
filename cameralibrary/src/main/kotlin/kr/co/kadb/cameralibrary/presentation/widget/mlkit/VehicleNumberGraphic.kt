/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.co.kadb.cameralibrary.presentation.widget.mlkit

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.mlkit.vision.text.Text
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

/**
 * Graphic instance for rendering TextBlock position, size, and ID within an associated graphic
 * overlay view.
 */
class VehicleNumberGraphic constructor(
    overlay: GraphicOverlay?,
    private val text: Text
) : GraphicOverlay.Graphic(overlay) {

    private val rectPaint: Paint = Paint()
    private val labelPaint: Paint
    private val numberPaint: Paint

    init {
        rectPaint.color = MARKER_COLOR
        rectPaint.style = Paint.Style.STROKE
        rectPaint.strokeWidth = STROKE_WIDTH
        numberPaint = Paint()
        numberPaint.color = TEXT_COLOR
        numberPaint.textSize = TEXT_SIZE
        labelPaint = Paint()
        labelPaint.color = MARKER_COLOR
        labelPaint.style = Paint.Style.FILL
        // Redraw the overlay, as this graphic has been added.
        postInvalidate()
    }

    /** Draws the text block annotations for position, size, and raw value on the supplied canvas. */
    override fun draw(canvas: Canvas) {
        // Debug.
        //Timber.d(">>>>> ${javaClass.simpleName} > textBlocks : ${text.textBlocks}")
        for (textBlock in text.textBlocks) { // Renders the text at the bottom of the box.
            // Debug.
            //Timber.d(">>>>> ${javaClass.simpleName} > textBlock : $textBlock}")
            for (line in textBlock.lines) {
                // Debug.
                /*Timber.d(
                    ">>>>> ${javaClass.simpleName} > lines > " +
                            "[${line.text}] : [${line.confidence}], " +
                            "boundingBox : ${line.boundingBox}"
                    //", cornerPoints : ${line.cornerPoints.toJsonPretty()}"
                )*/

                // 차량번호 정규식(테스트).
                // 지역별: 서울 부산 대구 인천 광주 대전 울산 경기 강원 충북 충남 전북 전남 경북 경남 제주
                // 자가용: 가나다라마 거너더러머버서어저 고노도로모보소오조 구누두루무부수우주
                // 사업용: 바사아자
                // 렌터카: 하허호
                // 택배용: 배
                // 외교용: 외교123-001 - 외교 준외 준영 국기 협정 대표
                // 군사용: 23(육)1234 - 육공해국합
                // 이륜차: 울산 남 가 1234 - 가나다라마바사아자차카타파
                // (서울|부산|대구|인천|광주|대전|울산|경기|강원|충북|충남|전북|전남|경북|경남|제주)?
                // [0-9]{2,3}
                // (가|나|다|라|마|거|너|더|러|머|버|서|어|저|고|노|도|로|모|보|소|오|조|구|누|두|루|무|부|수|우|주|바|사|아|자|하|허|호|배)[0-9]{4}
//                val regex = Regex(
//                    "(서울|부산|대구|인천|광주|대전|울산|경기|강원|충북|충남|전북|전남|경북|경남|제주)?" +
//                            "\\s?" +
//                            "[0-9]{2,3}" +
//                            "\\s?" +
//                            "([가나다라마거너더러머버서어저고노도로모보소오조구누두루무부수우주바사아자하허호배])" +
//                            "\\s?" +
//                            "[0-9]{4}"
//                )
//                val matchResult = regex.matchEntire(line.text)
//
//                // found.
//                if (matchResult != null) {
                    // Draws the bounding box around the TextBlock.
                    val rect = RectF(line.boundingBox)
                    drawText(
                        line.text,
                        rect,
                        TEXT_SIZE + 2 * STROKE_WIDTH,
                        canvas
                    )
//                }


//                for (element in line.elements) {
//                    // 차량번호 정규식(테스트).
//                    // 지역별: 서울 부산 대구 인천 광주 대전 울산 경기 강원 충북 충남 전북 전남 경북 경남 제주
//                    // 자가용: 가나다라마 거너더러머버서어저 고노도로모보소오조 구누두루무부수우주
//                    // 사업용: 바사아자
//                    // 렌터카: 하허호
//                    // 택배용: 배
//                    // 외교용: 외교123-001 - 외교 준외 준영 국기 협정 대표
//                    // 군사용: 23(육)1234 - 육공해국합
//                    // 이륜차: 울산 남 가 1234 - 가나다라마바사아자차카타파
//                    // (서울|부산|대구|인천|광주|대전|울산|경기|강원|충북|충남|전북|전남|경북|경남|제주)?
//                    // [0-9]{2,3}
//                    // (가|나|다|라|마|거|너|더|러|머|버|서|어|저|고|노|도|로|모|보|소|오|조|구|누|두|루|무|부|수|우|주|바|사|아|자|하|허|호|배)[0-9]{4}
//                    val regex = Regex(
//                        "(서울|부산|대구|인천|광주|대전|울산|경기|강원|충북|충남|전북|전남|경북|경남|제주)?" +
//                                "\\s?" +
//                                "[0-9]{2,3}" +
//                                "\\s?" +
//                                "([가나다라마거너더러머버서어저고노도로모보소오조구누두루무부수우주바사아자하허호배])" +
//                                "\\s?" +
//                                "[0-9]{4}"
//                    )
//                    val matchResult = regex.matchEntire(element.text)
//
//                    // found.
//                    if (matchResult != null) {
////                            regex.findAll(element.text).forEach { matchResult ->
//                        // Debug.
//                        Timber.d(">>>>> ${javaClass.simpleName} > matchResult > ${matchResult.value}")
////                                Timber.d(
////                                    ">>>>> ${javaClass.simpleName} > elements > " +
////                                            "[${element.text}] : [${element.confidence}]" +
////                                            " - language : ${element.recognizedLanguage}, " +
////                                            "boundingBox : ${element.boundingBox}"
////                                    //", cornerPoints : ${element.cornerPoints.toJsonPretty()}"
////                                )
//
//
//                        // Draws the bounding box around the TextBlock.
//                        val rect = RectF(line.boundingBox)
//                        drawText(
//                            element.text/*matchResult.value*/,
//                            rect,
//                            TEXT_SIZE + 2 * STROKE_WIDTH,
//                            canvas
//                        )
//                    }
//                }
            }
        }
    }

    private fun drawText(text: String, rect: RectF, textHeight: Float, canvas: Canvas) {
        // If the image is flipped, the left will be translated to right, and the right to left.
        val x0 = translateX(rect.left)
        val x1 = translateX(rect.right)
        rect.left = min(x0, x1)
        rect.right = max(x0, x1)
        rect.top = translateY(rect.top)
        rect.bottom = translateY(rect.bottom)
        canvas.drawRect(rect, rectPaint)
        val textWidth = numberPaint.measureText(text)
        canvas.drawRect(
            rect.left - STROKE_WIDTH,
            rect.top - textHeight,
            rect.left + textWidth + 2 * STROKE_WIDTH,
            rect.top,
            labelPaint
        )
        // Renders the text at the bottom of the box.
        canvas.drawText(text, rect.left, rect.top - STROKE_WIDTH, numberPaint)
    }

    companion object {
        private const val TEXT_WITH_LANGUAGE_TAG_FORMAT = "%s:%s"
        private const val TEXT_COLOR = Color.BLACK
        private const val MARKER_COLOR = Color.WHITE
        private const val TEXT_SIZE = 54.0f
        private const val STROKE_WIDTH = 4.0f
    }
}
