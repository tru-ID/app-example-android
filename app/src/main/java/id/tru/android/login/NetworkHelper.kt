package id.tru.android.login

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.util.*
import javax.net.ssl.*

/**
 * Use a custom socket factory.
 * This requires okhttp 4 ( I presume the okhttp library is already used https://square.github.io/okhttp/)
 *
 * Just ensure latest version is used: implementation "com.squareup.okhttp3:okhttp:${"4.8.0}" 
 * And also include logging-interceptor implementation "com.squareup.okhttp3:logging-interceptor:4.8.0”
 */
class NetworkHelper
constructor(okHttpClient: OkHttpClient) {


    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor(logger = object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.d("OkHttp", message)
            }
        }).apply { level = HttpLoggingInterceptor.Level.BODY }
    }


    val okHttpClient = okHttpClient.let {
        var trustManager: X509TrustManager?
        val sslSocketFactory: SSLSocketFactory
        val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers: Array<TrustManager> =
            trustManagerFactory.getTrustManagers()
        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
            ("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers))
        }

        try {
            trustManager =
                trustManagers[0] as X509TrustManager

            val sslContext: SSLContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
            sslSocketFactory = sslContext.socketFactory
        } catch (e: GeneralSecurityException) {
            throw RuntimeException(e)
        }

        //we have to ensure we follow redirects
        if (okHttpClient.followRedirects || okHttpClient.followSslRedirects) {
            okHttpClient.newBuilder()
                .followRedirects(true)
                .followSslRedirects(true)
                .sslSocketFactory(sslSocketFactory, trustManager)
                .addInterceptor(loggingInterceptor)
                .build()
        } else okHttpClient
    }

    /**
     * Perform your request
     */
    fun run(url: String): String {
        val request = Request.Builder()
            .url(url)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw java.io.IOException("Unexpected code $response")

            for ((name, value) in response.headers) {
                println("$name: $value")
            }

            return response.body!!.string()
        }
    }

}