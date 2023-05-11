package id.tru.android.model

import android.util.Log
import id.tru.android.api.RetrofitBuilder
import id.tru.sdk.TruSDK
import org.json.JSONObject
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
        val response = RetrofitBuilder.apiClient.getPhoneCheck(getHeaderMap(phone),PhoneCheckPost(phone))
        return if (response.isSuccessful && response.body() != null) {
            val phoneCheck = response.body() as PhoneCheck
            Result.Success(phoneCheck)
        } else {
            Result.Error(IOException("Error Occurred: ${response.message()}"))
        }
    }

    // Step 2: Check URL
    @Throws(Exception::class)
    suspend fun openCheckURL(checkURL: String): JSONObject {
        Log.d("TruSDK", "Triggering open check url $checkURL")
        val truSdk = TruSDK.getInstance()
        return truSdk.openWithDataCellular(URL(checkURL),true)
    }

    // Step 3: Exchange Code
    @Throws(Exception::class)
    suspend fun exchangePhoneCheck(checkId: String, code: String, referenceId: String): Result<PhoneCheckResult> {
        Log.d("TruSDK", "Exchange code for phone check")
        val response = RetrofitBuilder.apiClient.getExchangePhoneCheck(getHeaderMap(checkId),checkId, code, referenceId, PhoneCheckExchange(checkId,code,referenceId) )
        return if (response.isSuccessful && response.body() != null) {
            val phoneCheckResult = response.body() as PhoneCheckResult
            Result.Success(phoneCheckResult)
        } else {
            Result.Error(Exception("Error in exchanging code"))
        }
    }

    // Step 3: Get Phone Check Result
    @Throws(Exception::class)
    suspend fun retrievePhoneCheckResult(checkID: String): Result<PhoneCheckResult> {
        val response = RetrofitBuilder.apiClient.getPhoneCheckResult(getHeaderMap(checkID),checkID)
        return if(response.isSuccessful && response.body() != null) {
            val phoneCheckResult = response.body() as PhoneCheckResult
            Result.Success(phoneCheckResult)
        } else {
            Result.Error(Exception("HTTP Error getting phone check results"))
        }
    }

    @Throws(Exception::class)
    suspend fun isReachable(url: String): JSONObject {
        val response = RetrofitBuilder.apiClient.getCoverageAccessToken(getHeaderMap(""))
        return if(response.isSuccessful && response.code() == 200) {
            val token = response.body() as Token
            val truSdk = TruSDK.getInstance()
            return truSdk.openWithDataCellularAndAccessToken(URL(url),token.token, false)
        } else {
            val json =  JSONObject()
            json.put("error", "forbidden")
            json.put("error_description", "no coverage access token")
            return json
        }
    }

    private fun getHeaderMap(v: String?): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Content-Type"] = "application/json"
        return headerMap
    }


    companion object {
        private const val TAG = "PhoneCheckActivity"
    }
}