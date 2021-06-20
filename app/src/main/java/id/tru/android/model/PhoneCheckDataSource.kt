package id.tru.android.model

import android.util.Log
import id.tru.android.api.RetrofitBuilder
import id.tru.sdk.TruSDK
import java.io.IOException
import java.util.*

/**
 * Class that handles phone check
 */
class PhoneCheckDataSource {

    // MOCK
    fun login(phoneNumber: String): Result<VerifiedPhoneNumber> {
        return try {
            val fakeUser = VerifiedPhoneNumber(UUID.randomUUID().toString())
            Result.Success(fakeUser)
        } catch (e: Throwable) {
            Result.Error(IOException("Error logging in", e))
        }
    }

    // Step 1: Create a Phone Check
    @Throws(Exception::class)
    suspend fun createPhoneCheck(phone: String): Result<PhoneCheck> {
        val response = RetrofitBuilder.apiClient.getPhoneCheck(PhoneCheckPost(phone))
        return if (response.isSuccessful && response.body() != null) {
            val phoneCheck = response.body() as PhoneCheck
            Result.Success(phoneCheck)
        } else {
            Result.Error(IOException("Error Occurred: ${response.message()}"))
        }
    }

    // Step 2: Check URL
    @Throws(Exception::class)
    suspend fun openCheckURL(checkURL: String): Boolean {
        Log.d("TruSDK", "Triggering open check url $checkURL")
        val truSdk = TruSDK.getInstance()
        return truSdk.openCheckUrl(checkURL)
    }

    // Step 3: Get Phone Check Result
    @Throws(Exception::class)
    suspend fun retrievePhoneCheckResult(checkID: String): Result<PhoneCheckResult> {
        val response = RetrofitBuilder.apiClient.getPhoneCheckResult(checkID)
        return if(response.isSuccessful && response.body() != null) {
            val phoneCheckResult = response.body() as PhoneCheckResult
            Result.Success(phoneCheckResult)
        } else {
            Result.Error(Exception("HTTP Error getting phone check results"))
        }
    }

    companion object {
        private const val TAG = "PhoneCheckActivity"
    }
}