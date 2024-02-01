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
import kr.co.kadb.cameralibrary.presentation.widget.util.DebugLog

/** Processor for the text detector demo. */
internal class VinNumberRecognitionProcessor(
    context: Context,
    textRecognizerOptions: TextRecognizerOptionsInterface
) : VisionProcessorBase<Text, String>(context) {
    // 차대번호 정규식(A~Z, 0~9 혼합 11자리 + 0~9 6자리(생산번호)).
    // Regex("[a-zA-Z0-9]{11}[a-zA-Z0-9]{1}[0-9]{5}")
    // 주의수입차는 생산번호 첫자리에 영문인 경우가 있음.
    private val regex = """([a-zA-Z0-9]{12})([bBgGiIoOqsSzZ0-9]{5})""".toRegex()
    private val conversionData = mapOf(
        'b' to '6',
        'B' to '8',
        'g' to '9',
        'G' to '6',
        'i' to '1',
        'I' to '1',
        'o' to '0',
        'O' to '0',
        'q' to '9',
        's' to '5',
        'S' to '5',
        'z' to '2',
        'Z' to '2',
    )

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
            textBlock.lines.forEach { line ->
                if (line.confidence >= 0.5f) {
                    // Find & Add
                    regex.find(line.text)?.let { matchResult ->
                        var (groupNumber, serial) = matchResult.destructured
                        conversionData.forEach {
                            serial = serial.replace(it.key, it.value)
                        }
                        drawItems.add(DetectedItem(groupNumber + serial, RectF(line.boundingBox)))

                        // Debug.
                        //DebugLog.i { ">>>>> find: ${matchResult.value}, ${line.confidence}" }
                    }
                }

                // Debug.
                //DebugLog.i { ">>>>> ${line.text}, ${line.confidence}" }
            }
        }

        // Draw & Result invoke.
        if (drawItems.isNotEmpty()) {
            // Add.
            detectedItems.addAll(drawItems)
            // Draw.
            graphicOverlay.add(BorderingGraphic(graphicOverlay, drawItems))
            // Grouping & Result.
            detectedItems.groupingBy { it.text }.eachCount().also { map ->
                val sortedItems = map.toList().sortedByDescending { (_, value) -> value }
                if (sortedItems[0].second >= 5) {
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
    }

    override fun onComplete(
        onFailure: ((Exception) -> Unit)?,
        onSuccess: ((String, RectF) -> Unit)?
    ) {
        this.onSuccess = onSuccess
        this.onFailure = onFailure
    }
}
