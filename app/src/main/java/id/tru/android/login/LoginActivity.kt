package id.tru.android.login

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import id.tru.android.R
import id.tru.android.api.RedirectManager
import id.tru.android.api.RetrofitBuilder
import id.tru.android.data.PhoneCheck
import id.tru.android.data.PhoneCheckPost
import id.tru.android.data.PhoneCheckResult
import id.tru.android.databinding.ActivityLoginBinding
import id.tru.android.util.isPhoneNumberValid
import id.tru.sdk.TruSDK
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Add blazingly fast mobile phone verification to your app for 2FA or passwordless onboarding.
 * Leveraging the tru.ID PhoneCheck API confirms ownership of a mobile phone number by verifying
 * the possession of an active SIM card with the same number.
 */
class LoginActivity : AppCompatActivity() {

    private val redirectManager by lazy { RedirectManager() }
    private var startTime: Long = 0
    private lateinit var phoneCheck: PhoneCheck

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TruSDK.initializeSdk(applicationContext)

        val binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    /** Called when the user taps the Sign In button */
    fun initSignIn(view: View) {
        Log.d(TAG, "phoneNumber " + phone_number.text)
        // close virtual keyboard when sign in starts
        phone_number.onEditorAction(EditorInfo.IME_ACTION_DONE)

        resetProgress()
        createPhoneCheck()
    }

    // region internal

    // Step 1: Create a Phone Check
    private fun createPhoneCheck() {
        step1.visibility = View.VISIBLE

        if (!isPhoneNumberValid(phone_number.text.toString())) {
            step1_tv.text = getString(R.string.phone_check_step1_errror)
            return
        }
        progress_step1.check()
        step1_tv.text = getString(R.string.phone_check_step1)
        startTime = System.currentTimeMillis()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitBuilder.apiClient.getPhoneCheck(
                    PhoneCheckPost(phone_number.text.toString()))

                if (response.isSuccessful && response.body() != null) {
                    phoneCheck = response.body() as PhoneCheck

                    val currentTime = System.currentTimeMillis()
                    Log.d(TAG, "phoneCheck $phoneCheck [" + (currentTime - startTime) + "ms]")

                    withContext(Dispatchers.Main) {
                        step2.visibility = View.VISIBLE
                        progress_step2.check()
                    }

                    // Step 2: Open the check_url
                    openCheckURL()
                } else {
                    // Show API error.
                    updateUIonError("Error Occurred: ${response.message()}")
                }
            } catch (e: Throwable) {
                updateUIonError("exception caught $e")
            }
        }
    }

    private fun openCheckURL() {
        CoroutineScope(Dispatchers.IO).launch {
            redirectManager.openCheckUrl(phoneCheck.check_url)

            val currentTime = System.currentTimeMillis()
            Log.d(TAG, "redirect done [" + (currentTime - startTime) + "ms]")

            withContext(Dispatchers.Main) {
                step3.visibility = View.VISIBLE
                progress_step3.check()
            }

            // Step 3: Get Phone Check Result
            getPhoneCheckResult()
        }
    }

    // Step 3: Get Phone Check Result
    private fun getPhoneCheckResult() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitBuilder.apiClient.getPhoneCheckResult(phoneCheck.check_id)
            if (response.isSuccessful && response.body() != null) {
                val phoneCheckResult = response.body() as PhoneCheckResult

                val currentTime = System.currentTimeMillis()
                Log.d(TAG, "phoneCheckResult  $phoneCheckResult [" + (currentTime - startTime) + "ms]")

                withContext(Dispatchers.Main) {
                    if (phoneCheckResult.match) {
                        step4.visibility = View.VISIBLE
                        progress_step4.check()
                        step4_tv.text = getString(R.string.phone_check_step4)
                    } else {
                        step4.visibility = View.VISIBLE
                        step4_tv.text = getString(R.string.phone_check_step4_error)
                    }
                }
            }
        }
    }

    private fun resetProgress() {
        loading_layout.visibility = View.VISIBLE

        step1.visibility = View.INVISIBLE
        step2.visibility = View.INVISIBLE
        step3.visibility = View.INVISIBLE
        step4.visibility = View.INVISIBLE

        progress_step1.uncheck()
        progress_step2.uncheck()
        progress_step3.uncheck()
        progress_step4.uncheck()

        startTime = System.currentTimeMillis()
    }

    private suspend fun updateUIonError(additionalInfo: String) {
        Log.e(TAG, "$additionalInfo")
        withContext(Dispatchers.Main) {
            phone_number.setText("")
            loading_layout.visibility = View.INVISIBLE
            // Toast "An error occurred. Please try again."
        }
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
    // endregion internal
}

