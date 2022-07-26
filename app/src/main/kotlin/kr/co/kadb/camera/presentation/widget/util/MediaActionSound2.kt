/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kr.co.kadb.camera.presentation.widget.util

import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import timber.log.Timber
import java.lang.RuntimeException

/**
 * Modified by Miloš Černilovský on March 16 2021: converted original class to Kotlin,
 * fixed a minor bug and added support for respecting stream volume or setting an arbitrary
 * volume. New methods added: [playWithStreamVolume], volume parameters added to [play].
 *
 * A class for producing sounds that match those produced by various actions
 * taken by the media and camera APIs.
 *
 * This class is recommended for use with the [android.hardware.camera2] API, since the
 * camera2 API does not play any sounds on its own for any capture or video recording actions.
 *
 * With the older [android.hardware.Camera] API, use this class to play an appropriate
 * camera operation sound when implementing a custom still or video recording mechanism (through the
 * Camera preview callbacks with
 * [Camera.setPreviewCallback][android.hardware.Camera.setPreviewCallback], or through GPU
 * processing with [Camera.setPreviewTexture][android.hardware.Camera.setPreviewTexture], for
 * example), or when implementing some other camera-like function in your application.
 *
 * There is no need to play sounds when using
 * [Camera.takePicture][android.hardware.Camera.takePicture] or
 * [android.media.MediaRecorder] for still images or video, respectively,
 * as the Android framework will play the appropriate sounds when needed for
 * these calls.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
internal class MediaActionSound2 {
    private val mSounds = SOUND_FILES.indices.map {
        SoundState(it)
    }.toTypedArray()
    private val mLoadCompleteListener = SoundPool.OnLoadCompleteListener { soundPool, sampleId, status ->
        for (sound in mSounds) {
            if (sound.id != sampleId) {
                continue
            }
            var playSoundId = 0
            synchronized(sound) {
                if (status != 0) {
                    sound.state = STATE_NOT_LOADED
                    sound.id = 0
                    Timber.e(
                        TAG, "OnLoadCompleteListener() error: " + status +
                                " loading sound: " + sound.name
                    )
                    return@OnLoadCompleteListener
                }
                when (sound.state) {
                    STATE_LOADING -> sound.state = STATE_LOADED
                    STATE_LOADING_PLAY_REQUESTED -> {
                        playSoundId = sound.id
                        sound.state = STATE_LOADED
                    }
                    else -> Timber.e(
                        TAG, "OnLoadCompleteListener() called in wrong state: "
                                + sound.state + " for sound: " + sound.name
                    )
                }
            }
            if (playSoundId != 0) {
                soundPool.play(playSoundId, sound.volumeLeft, sound.volumeRight, 0, 0, 1.0f)
            }
            break
        }
    }
    private var mSoundPool: SoundPool? = SoundPool.Builder()
        .setMaxStreams(NUM_MEDIA_SOUND_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build().also {
            it.setOnLoadCompleteListener(mLoadCompleteListener)
        }

    private inner class SoundState(val name: Int) {
        var id = 0

        //0 is an invalid sample ID.
        var state: Int = STATE_NOT_LOADED
        var volumeLeft: Float = 1f
        var volumeRight: Float = 1f
    }

    /**
     * Construct a new MediaActionSound instance. Only a single instance is
     * needed for playing any platform media action sound; you do not need a
     * separate instance for each sound type.
     */
    @Suppress("ConvertSecondaryConstructorToPrimary", "RemoveEmptySecondaryConstructorBody")
    constructor() {
    }

    private fun loadSound(sound: SoundState): Int {
        val soundFileName = SOUND_FILES[sound.name]
        for (soundDir in SOUND_DIRS) {
            val id = mSoundPool!!.load(soundDir + soundFileName, 1)
            if (id > 0) {
                sound.state = STATE_LOADING
                sound.id = id
                return id
            }
        }
        return 0
    }

    /**
     * Preload a predefined platform sound to minimize latency when the sound is
     * played later by [playWithStreamVolume].
     * @param soundName The type of sound to preload, selected from
     * SHUTTER_CLICK, FOCUS_COMPLETE, START_VIDEO_RECORDING, or
     * STOP_VIDEO_RECORDING.
     * @return True if the sound was successfully loaded.
     * @see playWithStreamVolume
     * @see SHUTTER_CLICK
     * @see FOCUS_COMPLETE
     * @see START_VIDEO_RECORDING
     * @see STOP_VIDEO_RECORDING
     */
    fun load(soundName: Int): Boolean {
        if (soundName < 0 || soundName >= SOUND_FILES.size) {
            throw RuntimeException("Unknown sound requested: $soundName")
        }
        val sound = mSounds[soundName]
        return synchronized(sound) {
            when (sound.state) {
                STATE_NOT_LOADED -> {
                    if (loadSound(sound) <= 0) {
                        Timber.e(TAG, "load() error loading sound: $soundName")
                        false
                    } else {
                        true
                    }
                }
                else -> {
                    Timber.e(TAG, "load() called in wrong state: $sound for sound: $soundName")
                    false
                }
            }
        }
    }

    /**
     * Attempts to retrieve [AudioManager] from the given [context] and plays the given sound with the given [streamType] volume.
     * If retrieving volume is not successful, [defaultLeftVolume] and [defaultRightVolume] are used. Finally calls [play].
     * @param streamType One of [AudioManager] constants beginning with "STREAM_" prefix, e. g. [AudioManager.STREAM_MUSIC]
     */
    fun playWithStreamVolume(soundName: Int, context: Context, streamType: Int = AudioManager.STREAM_MUSIC, defaultLeftVolume: Float = 1f, defaultRightVolume: Float = 1f) {
        playWithStreamVolume(soundName, context.getSystemService(AUDIO_SERVICE) as AudioManager?, streamType, defaultLeftVolume, defaultRightVolume)
    }

    /**
     * Plays the given sound with the given [streamType] volume. If retrieving volume is not successful,
     * [defaultLeftVolume] and [defaultRightVolume] are used. Finally calls [play].
     * @param streamType One of [AudioManager] constants beginning with "STREAM_" prefix, e. g. [AudioManager.STREAM_MUSIC]
     */
    fun playWithStreamVolume(soundName: Int, audioManager: AudioManager?, streamType: Int = AudioManager.STREAM_MUSIC, defaultLeftVolume: Float = 1f, defaultRightVolume: Float = 1f) {
        val volume = audioManager?.let { it.getStreamVolume(streamType) / it.getStreamMaxVolume(streamType).toFloat() }
        play(soundName, volume ?: defaultLeftVolume, volume ?: defaultRightVolume)
    }

    /**
     * Play one of the predefined platform sounds for media actions.
     *
     * Use this method to play a platform-specific sound for various media
     * actions. The sound playback is done asynchronously, with the same
     * behavior and content as the sounds played by
     * [Camera.takePicture][android.hardware.Camera.takePicture],
     * [MediaRecorder.start][android.media.MediaRecorder.start], and
     * [MediaRecorder.stop][android.media.MediaRecorder.stop].
     *
     * With the [camera2][android.hardware.camera2] API, this method can be used to play
     * standard camera operation sounds with the appropriate system behavior for such sounds.
     *
     * With the older [android.hardware.Camera] API, using this method makes it easy to
     * match the default device sounds when recording or capturing data through the preview
     * callbacks, or when implementing custom camera-like features in your application.
     *
     * If the sound has not been loaded by [load] before calling play,
     * play will load the sound at the cost of some additional latency before
     * sound playback begins.
     *
     * @param soundName The type of sound to play, selected from
     * [SHUTTER_CLICK], [FOCUS_COMPLETE], [START_VIDEO_RECORDING], or
     * [STOP_VIDEO_RECORDING].
     * @param leftVolume left volume value (range= 0.0 to 1.0)
     * @param rightVolume right volume value (range= 0.0 to 1.0)
     * @see android.hardware.Camera.takePicture
     * @see android.media.MediaRecorder
     * @see SHUTTER_CLICK
     * @see FOCUS_COMPLETE
     * @see START_VIDEO_RECORDING
     * @see STOP_VIDEO_RECORDING
     */
    @JvmOverloads //for backward Java compatibility
    fun play(soundName: Int, leftVolume: Float = 1f, rightVolume: Float = 1f) {
        if (soundName < 0 || soundName >= SOUND_FILES.size) {
            throw RuntimeException("Unknown sound requested: $soundName")
        }
        val sound = mSounds[soundName]
        synchronized(sound) {
            when (sound.state) {
                STATE_NOT_LOADED -> {
                    if (loadSound(sound) <= 0) {
                        Timber.e(TAG, "play() error loading sound: $soundName")
                    } else {
                        setRequestPlayStatus(sound, leftVolume, rightVolume)
                    }
                }
                STATE_LOADING -> setRequestPlayStatus(sound, leftVolume, rightVolume)
                STATE_LOADED -> mSoundPool!!.play(sound.id, leftVolume, rightVolume, 0, 0, 1.0f)
                else -> Timber.e(TAG, "play() called in wrong state: " + sound.state + " for sound: " + soundName)
            }
        }
    }

    private fun setRequestPlayStatus(sound: SoundState, leftVolume: Float, rightVolume: Float) {
        with(sound) {
            state = STATE_LOADING_PLAY_REQUESTED
            volumeLeft = leftVolume
            volumeRight = rightVolume
        }
    }

    /**
     * Free up all audio resources used by this MediaActionSound instance. Do
     * not call any other methods on a MediaActionSound instance after calling
     * release().
     */
    fun release() {
        if (mSoundPool != null) {
            for (sound in mSounds) {
                synchronized(sound) {
                    sound.state = STATE_NOT_LOADED
                    sound.id = 0
                }
            }
            mSoundPool!!.release()
            mSoundPool = null
        }
    }

    companion object {
        private const val NUM_MEDIA_SOUND_STREAMS = 1
        private val SOUND_DIRS = arrayOf(
            "/product/media/audio/ui/",
            "/system/media/audio/ui/"
        )
        private val SOUND_FILES = arrayOf(
            "camera_click.ogg",
            "camera_focus.ogg",
            "VideoRecord.ogg",
            "VideoStop.ogg"
        )
        private const val TAG = "MediaActionSound"

        /**
         * The sound used by
         * [Camera.takePicture][android.hardware.Camera.takePicture] to
         * indicate still image capture.
         * @see playWithStreamVolume
         */
        const val SHUTTER_CLICK = 0

        /**
         * A sound to indicate that focusing has completed. Because deciding
         * when this occurs is application-dependent, this sound is not used by
         * any methods in the media or camera APIs.
         * @see playWithStreamVolume
         */
        const val FOCUS_COMPLETE = 1

        /**
         * The sound used by
         * [MediaRecorder.start()][android.media.MediaRecorder.start] to
         * indicate the start of video recording.
         * @see playWithStreamVolume
         */
        const val START_VIDEO_RECORDING = 2

        /**
         * The sound used by
         * [MediaRecorder.stop()][android.media.MediaRecorder.stop] to
         * indicate the end of video recording.
         * @see playWithStreamVolume
         */
        const val STOP_VIDEO_RECORDING = 3

        /**
         * States for SoundState.
         * STATE_NOT_LOADED             : sample not loaded
         * STATE_LOADING                : sample being loaded: waiting for load completion callback
         * STATE_LOADING_PLAY_REQUESTED : sample being loaded and playback request received
         * STATE_LOADED                 : sample loaded, ready for playback
         */
        private const val STATE_NOT_LOADED = 0
        private const val STATE_LOADING = 1
        private const val STATE_LOADING_PLAY_REQUESTED = 2
        private const val STATE_LOADED = 3
    }
}