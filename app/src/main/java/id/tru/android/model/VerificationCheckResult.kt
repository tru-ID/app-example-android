package id.tru.android.model

import id.tru.android.login.VerifiedPhoneNumberModel

/**
 * Phone Check verification result : success or error message.
 */
data class VerificationCheckResult(
    val success: VerifiedPhoneNumberModel? = null,
    val progressUpdate: Triple<Step, Int, Boolean>? = null,
    val error: Int? = null
)

data class ReachabilityResult(
    val networkAliases: MutableList<String>? = null
)

enum class Step {
    FIRST, SECOND, THIRD, FOURTH
}