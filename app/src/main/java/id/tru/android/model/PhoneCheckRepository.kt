package id.tru.android.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import id.tru.android.R
import id.tru.android.login.VerifiedPhoneNumberModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Class that performs phone check from the remote data source
 */
class PhoneCheckRepository(val dataSource: PhoneCheckDataSource) {

    val dataSourcePhoneCheckResult = MutableLiveData<VerificationCheckResult>()

    @Throws(Exception::class)
    suspend fun signIn(phoneNumber: String): Result<VerificationCheckResult> {
        // Create a new coroutine off the UI main thread, on an I/O thread
        // Move the execution of the coroutine to the I/O dispatcher
        return withContext(Dispatchers.IO) {
            var startTime: Long = System.currentTimeMillis()
            var currentTime: Long = 0
            var reachabilityStatus: Int = 500
            // Signal Step0 Update
            dataSourcePhoneCheckResult.postValue(
                VerificationCheckResult(
                    progressUpdate = Triple(Step.FIRST, R.string.phone_check_step0, true)
                )
            )
            // Step 0 (optional): Find Device IP
            val resp = dataSource.isReachable("https://eu.api.tru.id/coverage/v0.1/device_ip")
            if (resp.optString("error") != "") {
                println("not reachable: ${resp.optString("error_description")}")
                Result.Error( Exception("Not reachable: ${resp.optString("error_description")} "))
            } else {
                reachabilityStatus = resp.optInt("http_status")
                if (reachabilityStatus == 200 ) {
                    if (resp.optJSONObject("response_body") != null) {
                        val body = resp.optJSONObject("response_body")
                        Log.d(TAG, "is reachable on " + body.optString("network_name"))

                        val networkId = body.optString("network_id")
                        val networkArray = body.getJSONArray("network_aliases")
                        val networkAliases: MutableList<String> = mutableListOf()
                        for (i in 0 until networkArray.length()) {
                            val alias = networkArray.getString(i)
                            networkAliases.add(alias)
                        }
                        networkAliases.add(networkId)
                        Log.d(TAG, "network array  + $networkAliases")
                        // Signal Step1 Update
                        dataSourcePhoneCheckResult.postValue(
                            VerificationCheckResult(
                                progressUpdate = Triple(Step.FIRST, R.string.phone_check_step1, true)
                            )
                        )
                        // Step 1: Create Phone Check
                        // Blocking network request code
                        var checkResult = dataSource.createPhoneCheck(phoneNumber)
                        currentTime = System.currentTimeMillis()
                        Log.d(TAG, "phoneCheck $checkResult [" + (currentTime - startTime) + "ms]")

                        if (checkResult is Result.Success) {
                            //Here - Signal Step2 Update
                            dataSourcePhoneCheckResult.postValue(
                                VerificationCheckResult(
                                    progressUpdate = Triple(Step.SECOND, R.string.phone_check_step2, true)
                                )
                            )
                            // Step 2: Open the check_url
                            val resp = dataSource.openCheckURL(checkResult.data.check_url)
                            if (resp.optString("error") != "") {
                                dataSourcePhoneCheckResult.postValue(
                                    VerificationCheckResult(
                                        progressUpdate = Triple(
                                            Step.FOURTH,
                                            R.string.phone_check_step4_error,
                                            false
                                        )
                                    )
                                )
                                Result.Error(Exception("Cannot open check url"))
                            } else {
                                val status = resp.optInt("http_status")
                                if (status == 200) {
                                    val body = resp.optJSONObject("response_body")
                                    if (body != null) {
                                        val code = body.optString("code")
                                        currentTime = System.currentTimeMillis()
                                        Log.d(TAG, "redirect done [" + (currentTime - startTime) + "ms]")
                                        dataSourcePhoneCheckResult.postValue(
                                            VerificationCheckResult(
                                                progressUpdate = Triple(
                                                    Step.THIRD,
                                                    R.string.phone_check_step3,
                                                    true
                                                )
                                            )
                                        )
                                        if (code != "") {
                                            var phoneCheckResult = dataSource.exchangePhoneCheck(
                                                body.optString("check_id"),
                                                code,
                                                body.optString("reference_id")
                                            )
                                            val currentTime = System.currentTimeMillis()
                                            Log.d(
                                                TAG,
                                                "phoneCheckResult  $checkResult [" + (currentTime - startTime) + "ms]"
                                            )
                                            //Here - Signal Step4 Update
                                            if (phoneCheckResult is Result.Success) {
                                                val view = VerifiedPhoneNumberModel(
                                                    phoneNumber = phoneNumber,
                                                    checkId = phoneCheckResult.data.check_id,
                                                    match = phoneCheckResult.data.match
                                                )

                                                if (phoneCheckResult.data.match) {
                                                    val step4Update =
                                                        Triple(
                                                            Step.FOURTH,
                                                            R.string.phone_check_step4,
                                                            true
                                                        )
                                                    dataSourcePhoneCheckResult.postValue(
                                                        VerificationCheckResult(
                                                            progressUpdate = step4Update
                                                        )
                                                    )
                                                    Result.Success(VerificationCheckResult(success = view))
                                                } else {
                                                    val step4Update =
                                                        Triple(
                                                            Step.FOURTH,
                                                            R.string.phone_check_step4_error,
                                                            false
                                                        )
                                                    dataSourcePhoneCheckResult.postValue(
                                                        VerificationCheckResult(
                                                            progressUpdate = step4Update
                                                        )
                                                    )
                                                    Result.Error(Exception("Phone Check failed!"))
                                                }

                                            } else {
                                                dataSourcePhoneCheckResult.postValue(
                                                    VerificationCheckResult(
                                                        progressUpdate = Triple(
                                                            Step.FOURTH,
                                                            R.string.phone_check_step4_error,
                                                            false
                                                        )
                                                    )
                                                )
                                                Result.Error(Exception("Cannot open HttpURLConnection"))
                                            }
                                        } else {
                                            Result.Error(Exception("Exchange PhoneCheck Error"))
                                        }
                                    } else {
                                        Result.Error(Exception("Error in retrieving response body"))
                                    }
                                } else {
                                    dataSourcePhoneCheckResult.postValue(
                                        VerificationCheckResult(
                                            progressUpdate = Triple(
                                                Step.THIRD,
                                                R.string.phone_check_step3_error,
                                                false
                                            )
                                        )
                                    )
                                    Result.Error( Exception("Cannot open HttpURLConnection"))
                                }
                            }

                        } else {
                            if (checkResult is Result.Error) {
                                Result.Error( checkResult.exception)
                            } else {
                                Result.Error( Exception("Unknown error (incorrect Loading update?)"))
                            }
                        }
                    } else {
                        Result.Error( Exception("Error in retrieving reachability response body"))
                    }
                }
                else if (reachabilityStatus == 400) {
                    Log.d(TAG, "not reachable: not a supported MNO")
                    Result.Error( Exception("not reachable: not a supported MNO"))
                } else if (reachabilityStatus == 412) {
                    Log.d(TAG, "not reachable: not a mobile IP")
                    Result.Error( Exception("not reachable: not a mobile IP"))
                } else {
                    Log.d(TAG, "not reachable: other error")
                    Result.Error( Exception("not reachable: other error"))
                }
            }
        }
    }

    @Throws(Exception::class)
    suspend fun retrieveNetworkInfo(): Result<ReachabilityResult> {
        // Create a new coroutine off the UI main thread, on an I/O thread
        // Move the execution of the coroutine to the I/O dispatcher
        return withContext(Dispatchers.IO) {
            var reachabilityStatus: Int = 500
            val resp = dataSource.isReachable("https://eu.api.tru.id/coverage/v0.1/device_ip")
            if (resp.optString("error") != "") {
                println("not reachable: ${resp.optString("error_description")}")
                Result.Error( Exception("Not reachable: ${resp.optString("error_description")} "))
            } else {
                reachabilityStatus = resp.optInt("http_status")
                if (reachabilityStatus == 200 ) {
                    if (resp.optJSONObject("response_body") != null) {
                        val body = resp.optJSONObject("response_body")
                        Log.d(TAG, "is reachable on " + body.optString("network_name"))
                        val networkId = body.optString("network_id")
                        val networkArray = body.getJSONArray("network_aliases")
                        val networkAliases: MutableList<String> = mutableListOf()
                        for (i in 0 until networkArray.length()) {
                            val alias = networkArray.getString(i)
                            networkAliases.add(alias)
                        }
                        networkAliases.add(networkId)
                        Log.d(TAG, "network array  + $networkAliases")
                        Result.Success(ReachabilityResult(networkAliases))
                    } else {
                        Result.Error( Exception("Error in retrieving reachability response body"))
                    }
                }
                else if (reachabilityStatus == 400) {
                    Log.d(TAG, "not reachable: not a supported MNO")
                    Result.Error( Exception("not reachable: not a supported MNO"))
                } else if (reachabilityStatus == 412) {
                    Log.d(TAG, "not reachable: not a mobile IP")
                    Result.Error( Exception("not reachable: not a mobile IP"))
                } else {
                    Log.d(TAG, "not reachable: other error")
                    Result.Error( Exception("not reachable: other error"))
                }
            }
        }
    }

    companion object {
        private const val TAG = "PhoneCheckRepository"
    }
}


