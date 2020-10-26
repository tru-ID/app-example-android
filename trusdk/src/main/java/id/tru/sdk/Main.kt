package id.tru.sdk

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import id.tru.sdk.network.Client


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class TruSDK private constructor(context: Context) {
    private val client = Client(context)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun openCheckUrl(checkUrl: String): String {

        Log.println(Log.INFO, "SDK::checkUrl", "Triggering check url")
        return client?.requestSync(checkUrl, method = "GET")!!
    }

    companion object {
        private var instance: TruSDK? = null

        @Synchronized
        fun initializeSdk(context: Context): TruSDK {
            var currentInstance = instance
            if (null == currentInstance) {
                currentInstance = TruSDK(context)
            }
            instance = currentInstance
            return currentInstance
        }

        @Synchronized
        fun getInstance(): TruSDK {
            var currentInstance = instance
            checkNotNull(currentInstance) {
                TruSDK::class.java.simpleName +
                        " is not initialized, call initializeSdk(...) first"
            }
            return currentInstance
        }

    }
}
