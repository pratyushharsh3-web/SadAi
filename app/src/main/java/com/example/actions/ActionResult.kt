package com.example.actions

sealed class ActionResult {
    data class Success(
        val spokenFeedback: String,
        val details: String? = null,
        val actionType: String = "SUCCESS"
    ) : ActionResult()

    data class NeedsConfirmation(
        val title: String,
        val description: String,
        val confirmAction: () -> Unit,
        val cancelAction: () -> Unit = {}
    ) : ActionResult()

    data class ContactSelectionRequired(
        val contacts: List<ContactInfo>,
        val onSelected: (ContactInfo) -> Unit
    ) : ActionResult()

    data class Failure(
        val spokenFeedback: String,
        val errorDetail: String? = null
    ) : ActionResult()
}

data class ContactInfo(
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null
)

data class BatteryInfo(
    val percentage: Int,
    val isCharging: Boolean,
    val statusText: String
)

data class InstalledAppInfo(
    val name: String,
    val packageName: String
)
