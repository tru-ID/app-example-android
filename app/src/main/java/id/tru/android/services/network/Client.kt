package id.tru.android.services.network

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
class Client private constructor(context: Context) {

    private val context = context
    private val client = OkHttpClient()

    init {
        val capabilities = intArrayOf(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val transportTypes = intArrayOf(NetworkCapabilities.TRANSPORT_CELLULAR)
        alwaysPreferNetworksWith(capabilities, transportTypes)
    }

    private fun alwaysPreferNetworksWith(
        capabilities: IntArray,
        transportTypes: IntArray
    ) {
        val request = NetworkRequest.Builder()

        for (cap in capabilities) {
            request.addCapability(cap)
        }

        for (trans in transportTypes) {
            request.addTransportType(trans)
        }
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(request.build(), object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        ConnectivityManager.setProcessDefaultNetwork(network)
                    } else {
                        connectivityManager.bindProcessToNetwork(network)
                    }
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "ConnectivityManager.NetworkCallback.onAvailable: ", e)
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                println("onLost: $network")
            }
            override fun onUnavailable() {
                super.onUnavailable()
                println("onUnavailable")
            }

        })
    }

    fun requestSync(url: String, method: String, body: RequestBody?=null): String {
        val request = Request.Builder()
            .method(method, body).addHeader("x-rta", "change_me")
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val rawResponse = response.body!!.string()
            println("Response to $url")
            println(rawResponse)

            return rawResponse
        }
    }

    companion object {
        private var instance: Client? = null

        @Synchronized
        fun setContext(context: Context): Client {
            var currentInstance = instance
            if (null == currentInstance) {
                currentInstance = Client(context)
            }
            instance = currentInstance
            return currentInstance
        }

        @Synchronized
        fun getInstance(): Client {
            var currentInstance = instance
            checkNotNull(currentInstance) {
                Client::class.java.simpleName +
                        " is not initialized, call getInstance(...) first"
            }
            return currentInstance
        }

    }
}