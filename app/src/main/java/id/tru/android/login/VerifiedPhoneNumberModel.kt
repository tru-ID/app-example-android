package id.tru.android.login

/**
 * Phone Check details that is exposed to the UI
 */
data class VerifiedPhoneNumberModel(
    val phoneNumber: String,
    val checkId: String,
    val match: Boolean
)