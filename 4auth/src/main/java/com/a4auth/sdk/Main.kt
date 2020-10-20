package com.a4auth.sdk

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.a4auth.sdk.network.Client


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SDK : Activity() {
    private val client = Client(this.applicationContext)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun triggerCheck(checkUrl: String): String {
        Log.println(Log.INFO, "SDK::checkUrl", "Triggering check url")
        return client.requestSync(checkUrl, method = "GET")!!
    }
}