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
import android.graphics.RectF
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface
import timber.log.Timber

/** Processor for the text detector demo. */
class MileageRecognitionProcessor(
    context: Context,
    textRecognizerOptions: TextRecognizerOptionsInterface
) : VisionProcessorBase<Text, String>(context) {
    // Detected Items.
    private val detectedItems = mutableListOf<DetectedItem>()

    // Success.
    private var onSuccess: ((String, RectF) -> Unit)? = null

    // Failure.
    private var onFailure: ((Exception) -> Unit)? = null

    // Recognizer.
    private val textRecognizer: TextRecognizer = TextRecognition.getClient(textRecognizerOptions)

    override fun stop() {
        super.stop()
        textRecognizer.close()
    }

    override fun detectInImage(image: InputImage): Task<Text> {
        return textRecognizer.process(image)
    }

    override fun onSuccess(results: Text, graphicOverlay: GraphicOverlay) {
        // Detected Items.
        var drawMileage = 0
        var drawRectf = RectF()
        // 정규화.
        results.textBlocks.forEach { textBlock ->
            // Debug.
            //Timber.i(">>>>> ${javaClass.simpleName} > TEXT_BLOCK > ${textBlock.text}")
            textBlock.lines.forEach { line ->
                // Debug.
                //Timber.i(">>>>> ${javaClass.simpleName} > LINE > ${line.text}")
                line.elements.forEach { element ->
                    // 주행거리 정규식(0~9 4자리에서 6자리).
                    val regex = Regex("[0-9]{3,6}")
                    val matchResult = regex.find(element.text)
                    val mileage = matchResult?.value?.toIntOrNull() ?: 0

                    // found.
                    if (/*element.confidence >= 0.7f && */mileage > 1000 && mileage > drawMileage) {
                        // Debug.
                        Timber.d(
                            ">>>>> ${javaClass.simpleName} > ELEMENT > " +
                                    "[$mileage] => ${element.text} : ${element.confidence}"
                        )

                        // 가장 큰 값 취합.
                        drawMileage = mileage
                        drawRectf = RectF(element.boundingBox)
                    }
                }
            }
        }

        // Draw & Result invoke.
        if (drawMileage > 0) {
            // Debug.
            Timber.i(">>>>> ${javaClass.simpleName} > DRAW > $drawMileage : $drawRectf")

            // Add & Draw.
            DetectedItem(drawMileage.toString(), drawRectf).also {
                // Add.
                detectedItems.add(it)
                // Draw.
                graphicOverlay.add(MileageGraphic(graphicOverlay, listOf(it)))
            }

            // Grouping & Result.
            detectedItems.groupingBy { it.text }.eachCount().also { map ->
                /*val max = map.maxBy { it.value }
                if (max.value > 10) {
                    onSuccess?.invoke(drawMileage.toString(), drawRectf)
                }*/
                val sortedItems = map.toList().sortedByDescending { (_, value) -> value }
                if (sortedItems.size == 1 && sortedItems[0].second > 5) {
                    onSuccess?.invoke(drawMileage.toString(), drawRectf)
                } else if (sortedItems.size > 1 &&
                    sortedItems[0].second > 5 &&
                    (sortedItems[0].second * 0.5f) > sortedItems[1].second) {
                    onSuccess?.invoke(drawMileage.toString(), drawRectf)
                }
            }
        }
    }

    override fun onFailure(ex: Exception) {
        // Debug.
        Timber.w(">>>>> ${javaClass.simpleName} > onFailure : $ex")
        onFailure?.invoke(ex)
    }

    override fun onComplete(
        onFailure: ((Exception) -> Unit)?,
        onSuccess: ((String, RectF) -> Unit)?
    ) {
        this.onSuccess = onSuccess
        this.onFailure = onFailure
    }
}
