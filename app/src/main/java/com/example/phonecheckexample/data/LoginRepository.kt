package com.example.phonecheckexample.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.phonecheckexample.data.model.LoggedInUser

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    fun logout() {
        user = null
        dataSource.logout()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun login(phoneNumber: String, listener: LoginDataSource.LoginResultListener) {
        // handle login
        dataSource.login(phoneNumber, object : LoginDataSource.LoginResultListener {
            override fun onLoginSuccess(result: Result.Success<LoggedInUser>) {
                setLoggedInUser(result.data)

                listener.onLoginSuccess(result)
            }

            override fun onLoginFailed(result: Result.Error) {
                listener.onLoginFailed(result)
            }

        })
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}