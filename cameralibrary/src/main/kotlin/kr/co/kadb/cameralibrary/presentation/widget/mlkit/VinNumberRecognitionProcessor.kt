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
    // 차대번호 정규식(A~Z, 0~9 혼합 11자리 + 0~9 6자리(생산번호)).
    // Regex("[a-zA-Z0-9]{11}[a-zA-Z0-9]{1}[0-9]{5}")
    // 주의수입차는 생산번호 첫자이에 영문인 경우가 있음.
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
            // Debug.
            //Timber.i(">>>>> ${javaClass.simpleName} > TEXT_BLOCK > ${textBlock.text}")
            textBlock.lines.forEach { line ->
                // Debug.
                //Timber.i(">>>>> ${javaClass.simpleName} > LINE > ${line.text}")

                // Find & Add
                regex.find(line.text)?.let { matchResult ->
                    var (groupNumber, serial) = matchResult.destructured
                    conversionData.forEach {
                        serial = serial.replace(it.key, it.value)
                    }
                    drawItems.add(DetectedItem(groupNumber + serial, RectF(line.boundingBox)))
                }
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
                /*val max = map.maxBy { it.value }
                if (max.value > 10) {
                    onSuccess?.invoke(drawMileage.toString(), drawRectf)
                }*/
                val sortedItems = map.toList().sortedByDescending { (_, value) -> value }
                if (sortedItems.size == 1 && sortedItems[0].second > 5) {
                    drawItems.find { it.text == sortedItems[0].first }?.also {
                        onSuccess?.invoke(it.text, it.rect)
                    }
                    // Debug.
                    Timber.i(">>>>> ${javaClass.simpleName} > DRAW > ${sortedItems[0]}")
                } else if (sortedItems.size > 1 &&
                    sortedItems[0].second > 5 &&
                    (sortedItems[0].second * 0.75f) > sortedItems[1].second
                ) {
                    drawItems.find { it.text == sortedItems[0].first }?.also {
                        onSuccess?.invoke(it.text, it.rect)
                    }
                }

                // Debug.
                Timber.i(">>>>> ${javaClass.simpleName} > DRAW > $sortedItems")
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
