package id.tru.android.api

import id.tru.android.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("/v0.2/phone-check")
    suspend fun getPhoneCheck(@HeaderMap headers: Map<String, String>,@Body user: PhoneCheckPost): Response<PhoneCheck>

    @POST("/v0.2/phone-check/exchange-code")
    suspend fun getExchangePhoneCheck(@HeaderMap headers: Map<String, String>,@Query(value = "check_id") checkId: String, @Query(value = "code") code: String, @Query(value = "reference_id") referenceId: String, @Body body: PhoneCheckExchange): Response<PhoneCheckResult>

    @GET("check_status")
    suspend fun getPhoneCheckResult(@HeaderMap headers: Map<String, String>,@Query(value = "check_id") checkId: String): Response<PhoneCheckResult>

    @GET("coverage_access_token")
    suspend fun getCoverageAccessToken(@HeaderMap headers: Map<String, String>): Response<Token>

}
