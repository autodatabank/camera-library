package kr.co.kadb.cameralibrary.presentation.model

/**
 * Created by oooobang on 2022. 7. 17..
 * UI State.
 */
internal data class ShootUiState(
    val isMultiplePicture: Boolean
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
            isMultiplePicture = false
        )
    }
}
//
//fun List<ShootItem>.toUiState(): List<ShootUiState> = map { it.toUiState() }
//
//fun ShootItem.toUiState(): ShootUiState = ShootUiState(
//    isMultiplePicture = isMultiplePicture
//)
