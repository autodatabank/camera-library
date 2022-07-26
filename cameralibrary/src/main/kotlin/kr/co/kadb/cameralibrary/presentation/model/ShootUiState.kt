package kr.co.kadb.cameralibrary.presentation.model

import android.util.Size

/**
 * Created by oooobang on 2022. 7. 17..
 * UI State.
 */
internal data class ShootUiState(
    val action: String?,
    val isShooted: Boolean,
    val isMultiplePicture: Boolean,
    val canMute: Boolean,
    val canUiRotation: Boolean,
    val cropPercent: List<Float>,
//    val unusedAreaWidth: Int,
//    val unusedAreaHeight: Int,
    val uris: ArrayList<String>,
    val sizes: ArrayList<Size>
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
            isShooted = false,
            isMultiplePicture = false,
            canMute = false,
            canUiRotation = false,
            cropPercent = listOf(),
//            unusedAreaWidth = 0,
//            unusedAreaHeight = 0,
            uris = arrayListOf(),
            sizes = arrayListOf()
        )
    }
}
//
//fun List<ShootItem>.toUiState(): List<ShootUiState> = map { it.toUiState() }
//
//fun ShootItem.toUiState(): ShootUiState = ShootUiState(
//    isMultiplePicture = isMultiplePicture
//)
