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

import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.util.Size
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface
import timber.log.Timber
import java.lang.Integer.max
import java.lang.Integer.min

/** Processor for the text detector demo. */
class VehicleNumberRecognitionProcessor(
    context: Context,
    textRecognizerOptions: TextRecognizerOptionsInterface
) : VisionProcessorBase<Text, String>(context) {
    // Detected Items.
    private val detectedItems = mutableListOf<DetectedItem>()

    // Success.
    private var onSuccess: ((String, RectF) -> Unit)? = null

    // Failure
    private var onFailure: ((Exception) -> Unit)? = null

    // Recognizer.
    private val textRecognizer: TextRecognizer = TextRecognition.getClient(textRecognizerOptions)

    // 차량번호 정규식(테스트).
    // 지역별: 서울 부산 대구 인천 광주 대전 울산 세종 경기 강원 충북 충남 전북 전남 경북 경남 제주
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
    /*val regex = Regex(
        "((서울|부산|대구|인천|광주|대전|울산|세종|경기|강원|충북|충남|전북|전남|경북|경남|제주)?" +
                "\\s?" +
                "[0-9]{2,3}" +
                "\\s?" +
                "([가나다라마거너더러머버서어저고노도로모보소오조구누두루무부수우주바사아자하허호배])" +
                "\\s?" +
                "[0-9]{4})|" + // 자가용, 사업용, 렌터카, 택배용.
                "(외교\s?[0-9]{3}-[0-9]{3})|" + // 외교용.
                "([0-9]{2}\\(([육공해국합])\\)[0-9]{4})" // 군사용.
    )*/

    // Find & Add
    // 인식률이 떨어져 간단한 정규식으로 처리.
    private val regex = Regex(
        //"([0-9]{2,3}\\s?" +
        //"([가나다라마거너더러머버서어저고노도로모보소오조구누두루무부수우주바사아자하허호배])\\s?" +
        //"[0-9]{4})|" + // 자가용, 사업용, 렌터카, 택배용.
        "([^0-9]{0,2}\\s?[0-9]{2,3}\\s?[^0-9\\s]{1,2}\\s?[0-9]{4})|" + // 자가용, 사업용, 렌터카, 택배용.
                "(외교\\s?[0-9]{3}-[0-9]{3})|" + // 외교용.
                "([0-9]{2}\\(([육공해국합])\\)[0-9]{4})" // 군사용.
    )

    override fun stop() {
        super.stop()
        textRecognizer.close()
    }

    override fun detectInImage(image: InputImage): Task<Text> {
        return textRecognizer.process(image)
    }

    override fun onSuccess(results: Text, graphicOverlay: GraphicOverlay) {
        // Detected Items.
        val drawItems = mutableListOf<DetectedItem>()

        // 최근 감지 데이터.
        var latelyText = ""
        var latelyRect: Rect? = null

        // 이미지 크기.
        val imageSize = Size(graphicOverlay.imageWidth, graphicOverlay.imageHeight)

        // 정규화.
        results.textBlocks.forEach { textBlock ->
            // Debug.
            //Timber.i(">>>>> ${javaClass.simpleName} > TEXT_BLOCK[$index] > ${textBlock.text}")
            textBlock.lines.forEachIndexed { index, line ->
                // Debug.
                //Timber.i(">>>>> ${javaClass.simpleName} > TEXT_LINE[$index] > $latelyText + ${line.text}")

                // Find & Add.
                regex.find(latelyText + line.text)?.let { matchResult ->
                    createDrawItems(matchResult, latelyRect, line.boundingBox, imageSize).also {
                        drawItems.addAll(it)
                    }
                }
                if (latelyText.isNotEmpty() && line.text.isNotEmpty()) {
                    regex.find(line.text + latelyText)?.let { matchResult ->
                        createDrawItems(matchResult, latelyRect, line.boundingBox, imageSize).also {
                            drawItems.addAll(it)
                        }
                    }
                }
                latelyText = line.text
                latelyRect = line.boundingBox
            }
        }

        // Draw & Result invoke.
        if (drawItems.isNotEmpty()) {
            // Add.
            detectedItems.addAll(drawItems)
            // Draw.
            graphicOverlay.add(VehicleNumberGraphic(graphicOverlay, drawItems))

            // Grouping & Result.
            detectedItems.groupingBy { it.text }.eachCount().also { map ->
                /*val max = map.maxBy { it.value }
                if (max.value > 10) {
                    onSuccess?.invoke(drawMileage.toString(), drawRectf)
                }*/
                val sortedItems = map.toList().sortedByDescending { (_, value) -> value }
                if (/*sortedItems.size == 1 && */sortedItems[0].second > 5) {
                    drawItems.find { it.text == sortedItems[0].first }?.also {
                        onSuccess?.invoke(it.text, it.rect)
                    }
                }/* else if (sortedItems.size > 1 &&
                    sortedItems[0].second > 5 &&
                    (sortedItems[0].second * 0.5f) > sortedItems[1].second
                ) {
                    drawItems.find { it.text == sortedItems[0].first }?.also {
                        onSuccess?.invoke(it.text, it.rect)
                    }
                }*/
            }
        }
    }

    override fun onFailure(ex: Exception) {
        // Debug.
        Timber.w(">>>>> ${javaClass.simpleName} > onFailure : $ex")
    }

    override fun onComplete(
        onFailure: ((Exception) -> Unit)?,
        onSuccess: ((String, RectF) -> Unit)?
    ) {
        this.onSuccess = onSuccess
        this.onFailure = onFailure
    }

    private fun createDrawItems(
        matchResult: MatchResult,
        latelyRect: Rect?,
        boundingBox: Rect?,
        imageSize: Size
    ): List<DetectedItem> {
        // Detected Items.
        val drawItems = mutableListOf<DetectedItem>()

        // Add.
        var matchRect = if (latelyRect != null) {
            Rect(
                min(latelyRect?.left ?: 0, boundingBox?.left ?: 0),
                min(latelyRect?.top ?: 0, boundingBox?.top ?: 0),
                max(latelyRect?.right ?: 0, boundingBox?.right ?: 0),
                max(latelyRect?.bottom ?: 0, boundingBox?.bottom ?: 0)
            )
        } else {
            Rect(
                boundingBox?.left ?: 0,
                boundingBox?.top ?: 0,
                boundingBox?.right ?: 0,
                boundingBox?.bottom ?: 0
            )
        }
        val left = if (matchRect.left < 0) 0 else matchRect.left
        val top = if (matchRect.top < 0) 0 else matchRect.top
        val right = if (left + matchRect.width() > imageSize.width) {
            imageSize.width
        } else {
            matchRect.right
        }
        val bottom = if (top + matchRect.height() > imageSize.height) {
            imageSize.height
        } else {
            matchRect.bottom
        }
        matchRect = Rect(left, top, right, bottom)
        drawItems.add(DetectedItem(matchResult.value, RectF(matchRect)))

        // Debug.
        Timber.i(">>>>> ${javaClass.simpleName} > REGEX > ${matchResult.value}")

        return drawItems
    }
}
