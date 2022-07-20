package kr.co.kadb.cameralibrary.presentation.model

/**
 * Created by oooobang on 2022. 7. 17..
 * UI State.
 */
internal data class ShootUiState(
    val action: String? = null,
    val isShooted: Boolean = false,
    val isMultiplePicture: Boolean = false
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
