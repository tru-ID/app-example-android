package id.tru.android.api

import id.tru.android.data.PhoneCheck
import id.tru.android.data.PhoneCheckPost
import id.tru.android.data.PhoneCheckResult
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("/check")
    suspend fun getPhoneCheck(@Body user: PhoneCheckPost): Response<PhoneCheck>

    @GET("/check_status")
    suspend fun getPhoneCheckResult(@Query(value = "check_id") checkId: String): Response<PhoneCheckResult>
}
