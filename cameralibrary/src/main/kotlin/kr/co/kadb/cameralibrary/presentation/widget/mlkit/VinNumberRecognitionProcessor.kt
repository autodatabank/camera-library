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
class VinNumberRecognitionProcessor(
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
        // 정규화.
        results.textBlocks.forEach { textBlock ->
            // Debug.
            //Timber.i(">>>>> ${javaClass.simpleName} > TEXT_BLOCK > ${textBlock.text}")
            textBlock.lines.forEach { line ->
                // Debug.
                //Timber.i(">>>>> ${javaClass.simpleName} > LINE > ${line.text}")

                // Find & Add
                // 차대번호 정규식(A~Z, 0~9 혼합 17자리).
                Regex("[A-Z0-9]{17}").find(line.text)?.let { matchResult ->
                    drawItems.add(DetectedItem(matchResult.value, RectF(line.boundingBox)))

                    // Debug.
                    Timber.i(">>>>> ${javaClass.simpleName} > REGEX > ${matchResult.value}")
                }
            }
        }

        // Draw & Result invoke.
        if (drawItems.isNotEmpty()) {
            // Add.
            detectedItems.addAll(drawItems)
            // Draw.
            graphicOverlay.add(VinNumberGraphic(graphicOverlay, drawItems))

            // Grouping & Result.
            detectedItems.groupingBy { it.text }.eachCount().also { map ->
                /*val max = map.maxBy { it.value }
                if (max.value > 10) {
                    onSuccess?.invoke(drawMileage.toString(), drawRectf)
                }*/
                val sortedItems = map.toList().sortedByDescending { (_, value) -> value }
                if (sortedItems.size == 1 && sortedItems[0].second > 5) {
                    drawItems.find { it.text == sortedItems[0].first }?.also {
                        onSuccess?.invoke(it.text, it.rect)
                    }
                } else if (sortedItems.size > 1 &&
                    sortedItems[0].second > 5 &&
                    (sortedItems[0].second * 0.75f) > sortedItems[1].second
                ) {
                    drawItems.find { it.text == sortedItems[0].first }?.also {
                        onSuccess?.invoke(it.text, it.rect)
                    }
                }
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
}
