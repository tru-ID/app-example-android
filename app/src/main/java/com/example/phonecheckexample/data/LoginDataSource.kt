package com.example.phonecheckexample.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.phonecheckexample.data.model.LoggedInUser
import com.example.phonecheckexample.services.api.ApiManager
import com.example.phonecheckexample.services.api.RedirectManager
import java.io.IOException
import java.util.*


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun login(phoneNumber: String): Result<LoggedInUser> {
        val apiManager = ApiManager()
        val redirectManager = RedirectManager()

        try {
            var phoneCheck = apiManager.getPhoneCheck(phoneNumber)
            redirectManager.openCheckUrl(phoneCheck.check_url)
            var phoneCheckResult = apiManager.getPhoneCheckResult(phoneCheck.check_id)

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

    fun logout() {
        // TODO: revoke authentication
    }
}