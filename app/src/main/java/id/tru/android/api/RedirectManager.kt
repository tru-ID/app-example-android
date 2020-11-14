package id.tru.android.api

import android.util.Log
import id.tru.sdk.TruSDK

class RedirectManager {
    private val truSdk = TruSDK.getInstance()

    fun openCheckUrl(phoneCheckUrl: String): String {
        Log.println(Log.DEBUG, "RedirectManager", "Triggering open check url")
        return truSdk.openCheckUrl(phoneCheckUrl)
    }
}