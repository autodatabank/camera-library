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
import com.google.mlkit.vision.text.*
import kr.co.kadb.cameralibrary.presentation.widget.extension.removeCurrency
import kr.co.kadb.cameralibrary.presentation.widget.util.DebugLog

/** Processor for the text detector demo. */
internal class MileageRecognitionProcessor(
    context: Context,
    textRecognizerOptions: TextRecognizerOptionsInterface
) : VisionProcessorBase<Text, String>(context) {
    // 주행거리 정규식(0~9 4자리에서 6자리).
    private val regex = """[0-9]{1,3}(,)?[0-9]{3}""".toRegex()

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
            textBlock.lines.forEach { line ->
                line.elements.forEach { element ->
                    // Find & Add(가장 큰 값 취합).
                    val matchResult = regex.find(element.text)
                    val mileage = matchResult?.value?.removeCurrency()?.toIntOrNull() ?: 0
                    if (mileage > 1000 && mileage > drawMileage) {
                        drawMileage = mileage
                        drawRectf = RectF(element.boundingBox)
                    }
                }
            }
        }

        // Draw & Result invoke.
        if (drawMileage > 0) {
            // Add & Draw.
            DetectedItem(drawMileage.toString(), drawRectf).also {
                // Add.
                detectedItems.add(it)
                // Draw.
                graphicOverlay.add(BorderingGraphic(graphicOverlay, listOf(it)))
            }
            // Grouping & Result.
            detectedItems.groupingBy { it.text }.eachCount().also { map ->
                val sortedItems = map.toList().sortedByDescending { (_, value) -> value }
                if (sortedItems[0].second >= 3) {
                    detectedItems.find { it.text == sortedItems[0].first }?.also {
                        onSuccess?.invoke(it.text, it.rect)
                    }
                }
            }
        }
    }

    override fun onFailure(ex: Exception) {
        // Debug.
        DebugLog.w { ">>>>> onFailure : $ex" }
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
