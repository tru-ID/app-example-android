package id.tru.android.services.api

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import id.tru.android.data.model.PhoneCheck
import id.tru.android.data.model.PhoneCheckPost
import id.tru.android.data.model.PhoneCheckResult
import java.lang.Exception
import id.tru.android.services.adapters.PhoneCheckAdapter
import id.tru.android.services.network.Client
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class ApiManager {
    private val AUTH_ENDPOINT : String = System.getenv("AUTH_ENDPOINT") ?: "https://rta.tru.id/rta/0/phone_check"
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val phoneCheckPostJsonAdapter: JsonAdapter<PhoneCheckPost> = moshi.adapter(PhoneCheckPost::class.java)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getPhoneCheck(phoneNumber: String): PhoneCheck {
        Log.println(Log.DEBUG, "ApiManager", "Triggering phone check request")

        val localPhoneCheckCreateUrl = "$AUTH_ENDPOINT/check"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val phoneCheckPost = PhoneCheckPost(phone_number = phoneNumber)
        val body = phoneCheckPostJsonAdapter.toJson(phoneCheckPost).toRequestBody(mediaType)
        val response = Client.getInstance().requestSync(localPhoneCheckCreateUrl, method = "POST", body = body)
        try {
            return PhoneCheckAdapter().parsePhoneCheckResponse(response)
        }
        catch(error: Exception) {
            Log.println(Log.ERROR, "ApiManager", "Failed to trigger phone check: $error")
            throw error
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getPhoneCheckResult(checkId: String): PhoneCheckResult {
        Log.println(Log.DEBUG, "ApiManager", "Retrieve phone check request")
        var localPhoneCheckResultUrl = "$AUTH_ENDPOINT/check_status?check_id=$checkId"
        try {
            var response =  Client.getInstance().requestSync(localPhoneCheckResultUrl, method = "GET")
            return PhoneCheckAdapter().parsePhoneCheckResults(response)
        } catch(error: Exception) {
            Log.println(Log.ERROR, "ApiManager", "Failed to retrieve phone check results: $error")
            throw error
        }
    }
}