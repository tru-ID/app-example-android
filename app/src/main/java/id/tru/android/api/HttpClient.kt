package id.tru.android.api

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import okhttp3.*
import okio.IOException


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class HttpClient private constructor(context: Context) {

    private val context = context
    private val client = OkHttpClient()

    fun requestSync(url: String, method: String, body: RequestBody?=null): String {
        val request = Request.Builder()
            .method(method, body)
            .url(url)
            .build()
        println("request to $url -> "+request)

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val rawResponse = response.body!!.string()
            println("Response from $url")
            println(rawResponse)

            return rawResponse
        }
    }

    companion object {
        private var instance: HttpClient? = null

        @Synchronized
        fun setContext(context: Context): HttpClient {
            var currentInstance =
                instance
            if (null == currentInstance) {
                currentInstance = HttpClient(context)
            }
            instance = currentInstance
            return currentInstance
        }

        @Synchronized
        fun getInstance(): HttpClient {
            var currentInstance =
                instance
            checkNotNull(currentInstance) {
                HttpClient::class.java.simpleName +
                        " is not initialized, call getInstance(...) first"
            }
            return currentInstance
        }

    }
}