package kr.co.kadb.camera.presentation.model

/**
 * Created by oooobang on 2022. 5. 26..
 * UI State.
 */
internal data class ShootUiState(
    val companyCode: String,
    val companyName: String,
    val vehicleNumber: String,
    val customerName: String,
    val phoneNumber: String,
    val serial: String,
    // 수집동의.
    val isCollection: Boolean = false,
    // 위탁동의.
    val isConsignment: Boolean = false,
    // 제공동의.
    val isThirdParty: Boolean = false
) {
    /*class DiffCallback : DiffUtil.ItemCallback<NewsItemUiState>() {
        override fun areItemsTheSame(
            oldItem: NewsItemUiState,
            newItem: NewsItemUiState
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: NewsItemUiState,
            newItem: NewsItemUiState
        ): Boolean = oldItem == newItem
    }*/
    companion object {
        val Uninitialized = ShootUiState(
            companyCode = "",
            companyName = "",
            vehicleNumber = "",
            customerName = "",
            phoneNumber = "",
            serial = ""
        )
    }
}
//
//fun List<AgreeItem>.toUiState(): List<AgreeUiState> = map { it.toUiState() }
//
//fun AgreeItem.toUiState(): AgreeUiState = AgreeUiState(
//    companyCode = companyCode,
//    companyName = companyName,
//    vehicleNumber = vehicleNumber,
//    customerName = customerName,
//    phoneNumber = phoneNumber,
//    serial = serial
//)
//
//fun FcmSigningItem.toAgreeUiState(): AgreeUiState = AgreeUiState(
//    companyCode = companyCode ?: "",
//    companyName = companyName ?: "",
//    vehicleNumber = vehicleNumber ?: "",
//    customerName = customerName ?: "",
//    phoneNumber = phoneNumber ?: "",
//    serial = serial ?: ""
//)
