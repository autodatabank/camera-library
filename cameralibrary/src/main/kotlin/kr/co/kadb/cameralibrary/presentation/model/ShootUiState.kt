package kr.co.kadb.cameralibrary.presentation.model

import android.net.Uri
import android.util.Size
import kr.co.kadb.cameralibrary.domain.model.ShootItem

/**
 * Created by oooobang on 2022. 7. 17..
 * UI State.
 */
internal data class ShootUiState(
    val action: String?,
    val isDebug: Boolean,
    val isShooted: Boolean,
    val isMultiplePicture: Boolean,
    val canMute: Boolean,
    val hasHorizon: Boolean,
    val canUiRotation: Boolean,
    val cropPercent: List<Float>,
    val uris: ArrayList<Uri>,
    val sizes: ArrayList<Size>,
    val rotations: ArrayList<Int>,
    val horizonColor: Int,
    val unusedAreaBorderColor: Int
) {
    /*class DiffCallback : DiffUtil.ItemCallback<NewsItemUiState>() {
        override fun areItemsTheSame(
            oldItem: ShootUiState,
            newItem: ShootUiState
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ShootUiState,
            newItem: ShootUiState
        ): Boolean = oldItem == newItem
    }*/
    companion object {
        val Uninitialized = ShootUiState(
            action = null,
            isDebug = false,
            isShooted = false,
            isMultiplePicture = false,
            canMute = false,
            hasHorizon = false,
            canUiRotation = false,
            cropPercent = listOf(),
            uris = arrayListOf(),
            sizes = arrayListOf(),
            rotations = arrayListOf(),
            horizonColor = -1,
            unusedAreaBorderColor = -1
        )
    }
}

internal fun List<ShootItem>.toUiState(): List<ShootUiState> = map { it.toUiState() }

internal fun ShootItem.toUiState(): ShootUiState = ShootUiState(
    action = action,
    isDebug = isDebug,
    isShooted = isShooted,
    isMultiplePicture = isMultiplePicture,
    canMute = canMute,
    hasHorizon = hasHorizon,
    canUiRotation = canUiRotation,
    cropPercent = cropPercent,
    uris = uris,
    sizes = sizes,
    rotations = rotations,
    horizonColor = horizonColor,
    unusedAreaBorderColor = unusedAreaBorderColor
)
