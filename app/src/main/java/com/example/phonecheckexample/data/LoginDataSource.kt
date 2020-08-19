package com.example.phonecheckexample.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.phonecheckexample.data.model.LoggedInUser
import com.example.phonecheckexample.ui.login.NetworkManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Response
import java.io.IOException
import java.lang.Exception
import java.util.*


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    interface LoginResultListener {
        fun onLoginSuccess(result: Result.Success<LoggedInUser>)

        fun onLoginFailed(result: Result.Error)
    }

    @JsonClass(generateAdapter = true)
    class PhoneCheck(
        val check_url: String,
        val check_id: String
    )

    @JsonClass(generateAdapter = true)
    class PhoneCheckResult(
        val match: Boolean,
        val check_id: String
    )

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val phoneCheckJsonAdapter:JsonAdapter<PhoneCheck> = moshi.adapter(PhoneCheck::class.java)
    private val phoneCheckResultJsonAdapter:JsonAdapter<PhoneCheckResult> = moshi.adapter(PhoneCheckResult::class.java)
    private val LOCAL_ENDPOINT = "https://357835e890a9.ngrok.io"

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun login(phoneNumber: String): Result<LoggedInUser> {

        try {

            println("getting PhoneCheck")
            val phoneCheck = getPhoneCheck(phoneNumber)

            println("navigating to check_url")
            navigateToCheckUrl(phoneCheck.check_url)

            println("checking result of phone check")
            var phoneCheckResult: PhoneCheckResult = getPhoneCheckResult(phoneCheck.check_id)

            if(phoneCheckResult.match) {
                val fakeUser = LoggedInUser(UUID.randomUUID().toString(), "Jane Doe")
                return Result.Success(fakeUser)
            }
            else {
                return Result.Error(IOException("Error logging in"))
            }

        } catch (e: Throwable) {
            println("exception caught $e")
            return Result.Error(IOException("Error logging in", e))
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getPhoneCheck(phoneNumber: String): PhoneCheck {
        var localPhoneCheckCreateUrl = "$LOCAL_ENDPOINT/check?phone_number=$phoneNumber"
        var response = NetworkManager.getInstance()?.requestSync(localPhoneCheckCreateUrl, method = "GET")
        try {
            val phoneCheck = phoneCheckJsonAdapter.fromJson(response)
            return phoneCheck!!
        }
        catch(e: Exception) {
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun navigateToCheckUrl(checkUrl: String): String {
        return NetworkManager.getInstance()?.requestSync(checkUrl, method = "GET")!!
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getPhoneCheckResult(checkId: String): PhoneCheckResult {
        var localPhoneCheckResultUrl = "$LOCAL_ENDPOINT/check_status?check_id=$checkId"
        var response = NetworkManager.getInstance()?.requestSync(localPhoneCheckResultUrl, method = "GET")
        return phoneCheckResultJsonAdapter.fromJson(response)!!
    }

    fun logout() {
        // TODO: revoke authentication
    }
}