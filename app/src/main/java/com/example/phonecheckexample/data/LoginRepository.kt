package com.example.phonecheckexample.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.phonecheckexample.data.model.LoggedInUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    suspend fun login(phoneNumber: String): Result<LoggedInUser> {
        return withContext(Dispatchers.IO) {

            val result =  dataSource.login(phoneNumber)

            if (result is Result.Success) {
                setLoggedInUser(result.data)
            }

            return@withContext result
        }
    }

    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }
}