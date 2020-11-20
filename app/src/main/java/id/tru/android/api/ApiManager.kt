package id.tru.android.api

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.lang.Exception
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import id.tru.android.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class ApiManager {
    private val SERVER_BASE_URL : String = BuildConfig.SERVER_BASE_URL?: "change_me"
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val phoneCheckPostJsonAdapter: JsonAdapter<PhoneCheckPost> = moshi.adapter(
        PhoneCheckPost::class.java)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getPhoneCheck(phoneNumber: String): PhoneCheck {
        val localPhoneCheckCreateUrl = "$SERVER_BASE_URL/check"

        Log.println(Log.DEBUG, "ApiManager", String.format("Triggering phone check for \"%s\" request to \"%s\"", phoneNumber, localPhoneCheckCreateUrl))

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val phoneCheckPost =
            PhoneCheckPost(phone_number = phoneNumber)
        val body = phoneCheckPostJsonAdapter.toJson(phoneCheckPost).toRequestBody(mediaType)
        val response = HttpClient.getInstance()
            .requestSync(localPhoneCheckCreateUrl, method = "POST", body = body)
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
        var localPhoneCheckResultUrl = "$SERVER_BASE_URL/check_status?check_id=$checkId"
        try {
            var response =  HttpClient.getInstance()
                .requestSync(localPhoneCheckResultUrl, method = "GET")
            return PhoneCheckAdapter().parsePhoneCheckResults(response)
        } catch(error: Exception) {
            Log.println(Log.ERROR, "ApiManager", "Failed to retrieve phone check results: $error")
            throw error
        }
    }
}