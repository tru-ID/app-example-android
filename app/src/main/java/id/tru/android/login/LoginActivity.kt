package id.tru.android.login

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import id.tru.android.R
import id.tru.android.databinding.ActivityLoginBinding
import id.tru.android.model.Step
import id.tru.android.util.PhoneNumberUtil
import id.tru.sdk.TruSDK
import org.json.JSONObject

/**
 * Add blazingly fast mobile phone verification to your app for 2FA or passwordless onboarding.
 * Leveraging the tru.ID PhoneCheck API confirms ownership of a mobile phone number by verifying
 * the possession of an active SIM card with the same number.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var phoneCheckViewModel: PhoneCheckViewModel
    private lateinit var activityLoginBinding: ActivityLoginBinding

    //---> Activity Lifecycle calls -- START -->
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(activityLoginBinding.root)

        val phone = activityLoginBinding.phone
        val tcAccepted = activityLoginBinding.tcAccepted
        val login = activityLoginBinding.login
        val loading = activityLoginBinding.loading

        tcAccepted.movementMethod = LinkMovementMethod.getInstance()

        phoneCheckViewModel =
            ViewModelProvider(this, VerifyViewModelFactory()).get(PhoneCheckViewModel::class.java)

        phoneCheckViewModel.verificationFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // Disable login button unless both phone number is valid and user accepts the T&C
            login.isEnabled = loginState.isDataValid

            if (loginState.tcNotAcceptedError != null) {
                phone.error = getString(loginState.tcNotAcceptedError)
            }

            if (loginState.phoneNumberError != null) {
                phone.error = getString(loginState.phoneNumberError)
            }
        })

        phoneCheckViewModel.phoneCheckResult.observe(this@LoginActivity, Observer {
            val result = it ?: return@Observer
            //LiveData observers are always called on the main thread

            result.progressUpdate?.let { triple ->

                val (step, msgReference, shouldCheck) = triple
                val msg = getString(msgReference)
                when (step) {
                    Step.FIRST -> {
                        activityLoginBinding.progressStep1.visibility = View.VISIBLE
                        activityLoginBinding.progressStep1.check()
                        activityLoginBinding.step1Tv.text = msg //success or error msg
                    }
                    Step.SECOND -> {
                        activityLoginBinding.step2.visibility = View.VISIBLE
                        if (shouldCheck) activityLoginBinding.progressStep2.check()
                        activityLoginBinding.step2Tv.text = msg
                    }
                    Step.THIRD -> {
                        activityLoginBinding.step3.visibility = View.VISIBLE
                        if (shouldCheck) activityLoginBinding.progressStep3.check()
                        activityLoginBinding.step3Tv.text = msg //Either done on cellular or not
                    }
                    Step.FOURTH -> {
                        activityLoginBinding.step4.visibility = View.VISIBLE
                        if (shouldCheck) activityLoginBinding.progressStep4.check()
                        activityLoginBinding.step4Tv.text = msg //success or error msg
                    }
                }
            }

            if (result.error != null || result.success != null) {
                loading.visibility = View.GONE
                login.isEnabled = true
                result.error?.let { updateUIonError(result.error) }
                result.success?.let { updateUIonSuccess(result.success) }
                setResult(Activity.RESULT_OK)
            }

        })

        tcAccepted.apply {
            setOnCheckedChangeListener { buttonView, isChecked ->
                phoneCheckViewModel.loginDataChanged(phone.text.toString(), tcAccepted = isChecked)
            }
        }

        phone.apply {
            afterTextChanged {
                phoneCheckViewModel.loginDataChanged(
                    phone.text.toString(),
                    tcAccepted = tcAccepted.isChecked
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> phoneCheckViewModel.login(phone.text.toString())
                }
                false
            }

            /** Called when the user taps the Sign In button */
            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                login.isEnabled = false
                resetProgress()
                Log.d(TAG, "phoneNumber " + phone.text)
                // close virtual keyboard when sign in starts
                phone.onEditorAction(EditorInfo.IME_ACTION_DONE)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: TruSDK is being initialised")
        TruSDK.initializeSdk(applicationContext)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun retrieveDataConnectivityPhoneNumber(reachabilityResponseBody: JSONObject): String? {
        val phoneNumberUtil = PhoneNumberUtil()
        return phoneNumberUtil.getDataConnectivityPhoneNumber(reachabilityResponseBody, this)
    }

    //-->> UI Update Utility methods

    private fun resetProgress() {
        activityLoginBinding.loadingLayout.visibility = View.VISIBLE

        activityLoginBinding.step1.visibility = View.INVISIBLE
        activityLoginBinding.step2.visibility = View.INVISIBLE
        activityLoginBinding.step3.visibility = View.INVISIBLE
        activityLoginBinding.step4.visibility = View.INVISIBLE

        activityLoginBinding.progressStep1.uncheck()
        activityLoginBinding.progressStep2.uncheck()
        activityLoginBinding.progressStep3.uncheck()
        activityLoginBinding.progressStep4.uncheck()

    }

    private fun updateUIonError(additionalInfo: String) {
        Log.e(TAG, "$additionalInfo")
        activityLoginBinding.phone.setText("")
        Toast.makeText(
            applicationContext,
            additionalInfo,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun updateUIonError(@StringRes errorString: Int) {
        Toast.makeText(
            applicationContext,
            errorString,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateUIonSuccess(model: VerifiedPhoneNumberModel) {
        val welcome = getString(R.string.welcome)
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $model.phoneNumber",
            Toast.LENGTH_LONG,
        ).show()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

