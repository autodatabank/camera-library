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
import kr.co.kadb.cameralibrary.presentation.widget.extension.toJsonPretty
import timber.log.Timber

/** Processor for the text detector demo. */
class VinNumberRecognitionProcessor(
    context: Context,
    textRecognizerOptions: TextRecognizerOptionsInterface
) : VisionProcessorBase<Text, String>(context) {
    // Data.
    private val drawTexts = mutableListOf<String>()
    private val drawRects = mutableListOf<RectF>()

    // Success.
    private var onSuccess: ((String) -> Unit)? = null

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
        for (textBlock in results.textBlocks) {
            // Debug.
            //Timber.i(">>>>> ${javaClass.simpleName} > TEXT_BLOCK > ${textBlock.text}")
            for (line in textBlock.lines) {
                // Debug.
                Timber.i(">>>>> ${javaClass.simpleName} > LINE > ${line.text}")
                if (line.confidence >= 0.0f) {
                    // Debug.
                    Timber.d(
                        ">>>>> ${javaClass.simpleName} > lines > " +
                                "[${line.text}] : [${line.confidence}], " +
                                "boundingBox : ${line.boundingBox}"
                        //", cornerPoints : ${line.cornerPoints.toJsonPretty()}"
                    )
                    // 차대번호 정규식(A~Z, 0~9 혼합 17자리).
                    val regex = Regex("[A-Z0-9]{17}")
                    val matchResult = regex.matchEntire(line.text)

                    // found.
                    if (matchResult != null) {
                        drawTexts.add(line.text)
                        drawRects.add(RectF(line.boundingBox))
                    }
                }
            }
        }

        if (drawTexts.isNotEmpty()) {
            graphicOverlay.add(VinNumberGraphic(graphicOverlay, drawTexts, drawRects))

            // Group Counting.
            drawTexts.groupingBy {
                it
            }.eachCount().also { map ->
                val max = map.maxBy {
                    it.value
                }
                if (max.value >= 10) {
                    onSuccess?.invoke(max.key)
                }

                Timber.i(">>>>> VINNUMBER GROUP ${map.size} : ${max.value} => ${map.toJsonPretty()}")
            }
        }
    }

    override fun onFailure(ex: Exception) {
        // Debug.
        Timber.w(">>>>> ${javaClass.simpleName} > onFailure : $ex")
    }

    override fun onComplete(onFailure: ((Exception) -> Unit)?, onSuccess: ((String) -> Unit)?) {
        this.onSuccess = onSuccess
        this.onFailure = onFailure
    }
}
