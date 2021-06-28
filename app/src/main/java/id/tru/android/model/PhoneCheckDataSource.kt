package id.tru.android.model

import android.util.Log
import id.tru.android.api.RetrofitBuilder
import id.tru.sdk.ReachabilityDetails
import id.tru.sdk.TruSDK
import id.tru.sdk.network.TraceInfo
import java.io.IOException
import java.net.URL
import java.util.*

/**
 * Class that handles phone check
 */
class PhoneCheckDataSource {

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

    @Throws(Exception::class)
    suspend fun checkWithTrace(checkURL: String): TraceInfo {
        Log.d("TruSDK", "Triggering checkWithTrace url $checkURL")
        val truSdk = TruSDK.getInstance()
        return truSdk.checkWithTrace(URL(checkURL))
    }

    @Throws(Exception::class)
    fun isReachable(): ReachabilityDetails? {
        Log.d("TruSDK", "Triggering isReachable")
        val truSdk = TruSDK.getInstance()
        return truSdk.isReachable()
    }

    @Throws(Exception::class)
    fun getJSON(): String? {
        val baseURL = "https://tidy-crab-73.loca.lt/my-ip"
        val truSdk = TruSDK.getInstance()
        return truSdk.getJsonPropertyValue(baseURL, "ip_address")
    }

    companion object {
        private const val TAG = "PhoneCheckActivity"
    }
}