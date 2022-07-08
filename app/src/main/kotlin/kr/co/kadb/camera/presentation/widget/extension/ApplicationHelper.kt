@file:Suppress("unused")

package kr.co.kadb.camera.presentation.widget.extension

import android.app.Application
import androidx.annotation.ArrayRes

/**
 * Created by oooobang on 2018. 7. 30..
 * Application Extension.
 */
fun Application.getStringArray(@ArrayRes id: Int): Array<out String> = resources.getStringArray(id)
