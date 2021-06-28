package id.tru.android.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import id.tru.android.R
import id.tru.android.login.VerifiedPhoneNumberView
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

            // Signal Step0 Update
            dataSourcePhoneCheckResult.postValue(VerificationCheckResult(
                progressUpdate = Triple(Step.FIRST, R.string.phone_check_step0,true)))

            // Step 0 (optional): Find Device IP
            val reachabilityDetails = dataSource.isReachable()
            if (reachabilityDetails != null) {
                reachabilityDetails?.let {
                    Log.d(TAG, "Reachability: Network Name =>" + it.networkName)
                    Log.d(TAG, "Error:" + it.error.toString())
                }
            } else {
                Log.d(TAG, "No reachability details provided")
            }

            // Signal Step1 Update
            dataSourcePhoneCheckResult.postValue(VerificationCheckResult(
                progressUpdate = Triple(Step.FIRST, R.string.phone_check_step1,true)))
            // Step 1: Create Phone Check
            // Blocking network request code
            var checkResult = dataSource.createPhoneCheck(phoneNumber)
            currentTime = System.currentTimeMillis()
            Log.d(TAG, "phoneCheck $checkResult [" + (currentTime - startTime) + "ms]")

            if (checkResult is Result.Success) {
                //Here - Signal Step2 Update
                dataSourcePhoneCheckResult.postValue(VerificationCheckResult(
                    progressUpdate = Triple(Step.SECOND, R.string.phone_check_step2,true)))

                // Step 2: Open the check_url
                val isExecutedOnCellular = dataSource.openCheckURL(checkResult.data.check_url)
                currentTime = System.currentTimeMillis()
                Log.d(TAG, "redirect done [" + (currentTime - startTime) + "ms]")

                //Here - Signal Step3 Update
                val step3Update = if (isExecutedOnCellular) {
                    Triple(Step.THIRD, R.string.phone_check_step3,true)
                } else {
                    Triple(Step.THIRD, R.string.phone_check_step3_error,false)
                }

                dataSourcePhoneCheckResult.postValue(VerificationCheckResult(progressUpdate = step3Update))

                val phoneCheckResult = dataSource.retrievePhoneCheckResult(checkResult.data.check_id)
                val currentTime = System.currentTimeMillis()
                Log.d(TAG,"phoneCheckResult  $checkResult [" + (currentTime - startTime) + "ms]")

                //Here - Signal Step4 Update
                // NOT SURE ABOUT THIS perhaps this should be done as part of RETURN
                if (phoneCheckResult is Result.Success) {
                    val view = VerifiedPhoneNumberView(phoneNumber = phoneNumber,
                        checkId = phoneCheckResult.data.check_id,
                        match = phoneCheckResult.data.match)

                    if(phoneCheckResult.data.match) {
                        val step4Update = Triple(Step.FOURTH, R.string.phone_check_step4,true)
                        dataSourcePhoneCheckResult.postValue(VerificationCheckResult(progressUpdate = step4Update))
                        Result.Success(VerificationCheckResult(success = view))
                    } else {
                        val step4Update = Triple(Step.FOURTH, R.string.phone_check_step4_error,false)
                        dataSourcePhoneCheckResult.postValue(VerificationCheckResult(progressUpdate = step4Update))
                        Result.Error(Exception("Phone Check failed !"))
                    }

                } else {
                    dataSourcePhoneCheckResult.postValue(VerificationCheckResult(
                        progressUpdate = Triple(Step.FOURTH, R.string.phone_check_step4_error,false)))
                    Result.Error(Exception("Cannot open HttpURLConnection"))
                }

            } else {
                if (checkResult is Result.Error){
                    Result.Error(checkResult.exception)
                } else {
                    Result.Error(Exception("Unknown error (incorrect Loading update?)"))
                }
            }
        }
    }

    companion object {
        private const val TAG = "PhoneCheckRepository"
    }
}