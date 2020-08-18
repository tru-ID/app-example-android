package com.example.phonecheckexample.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.example.phonecheckexample.data.model.LoggedInUser
import com.example.phonecheckexample.ui.login.NetworkManager
import java.io.IOException


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    interface LoginResultListener {
        fun onLoginSuccess(result: Result.Success<LoggedInUser>)

        fun onLoginFailed(result: Result.Error)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun login(phoneNumber: String, listener: LoginResultListener) {
        try {
            val url = "https://www.google.com"

            // Request a string response from the provided URL.
            val stringRequest = StringRequest(
                Request.Method.GET,  url,
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    println("Response is: ${response.substring(0, 500)}")
                },
                Response.ErrorListener { println("That didn't work!") }
            )
            NetworkManager.getInstance()?.request(stringRequest)

            // TODO: handle loggedInUser authentication
            val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
            listener.onLoginSuccess(Result.Success(fakeUser))
        } catch (e: Throwable) {
            listener.onLoginFailed(Result.Error(IOException("Error logging in", e)))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}