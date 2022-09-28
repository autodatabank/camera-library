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
                if (line.confidence >= 0.0f) {
                    // Debug.
                    Timber.d(
                        ">>>>> ${javaClass.simpleName} > lines > " +
                                "[${line.text}] : [${line.confidence}], " +
                                "boundingBox : ${line.boundingBox}"
                        //", cornerPoints : ${line.cornerPoints.toJsonPretty()}"
                    )

                    /*// Draws the bounding box around the TextBlock.
                    val rect = RectF(line.boundingBox)
                    drawText(
                        getFormattedText(line.text, line.recognizedLanguage, line.confidence),
                        rect,
                        TEXT_SIZE + 2 * STROKE_WIDTH,
                        canvas
                    )*/



                    for (element in line.elements) {
                        // 차량번호 정규식(테스트).
                        val regex = Regex("[0-9]{2,3}[가-힣a-zA-Z0-9][0-9]{4}")
                        val matchResult = regex.matchEntire(element.text)

                        // found.
                        if (matchResult != null) {
//                            regex.findAll(element.text).forEach { matchResult ->
                            // Debug.
                            Timber.d(">>>>> ${javaClass.simpleName} > matchResult > ${matchResult.value}")
//                                Timber.d(
//                                    ">>>>> ${javaClass.simpleName} > elements > " +
//                                            "[${element.text}] : [${element.confidence}]" +
//                                            " - language : ${element.recognizedLanguage}, " +
//                                            "boundingBox : ${element.boundingBox}"
//                                    //", cornerPoints : ${element.cornerPoints.toJsonPretty()}"
//                                )


                            // Draws the bounding box around the TextBlock.
                            val rect = RectF(line.boundingBox)
                            drawText(
                                element.text/*matchResult.value*/,
                                rect,
                                TEXT_SIZE + 2 * STROKE_WIDTH,
                                canvas
                            )
                        }
                    }
                }
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
