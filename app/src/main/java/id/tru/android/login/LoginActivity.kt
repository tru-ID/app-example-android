package id.tru.android.login

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import id.tru.android.R
import id.tru.android.databinding.ActivityLoginBinding
import id.tru.android.model.Step
import id.tru.android.util.PhoneNumberUtil
import id.tru.sdk.TruSDK

/**
 * Add blazingly fast mobile phone verification to your app for 2FA or passwordless onboarding.
 * Leveraging the tru.ID PhoneCheck API confirms ownership of a mobile phone number by verifying
 * the possession of an active SIM card with the same number.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var phoneCheckViewModel: PhoneCheckViewModel
    private lateinit var activityLoginBinding: ActivityLoginBinding

    //---> Activity Lifecycle calls -- START -->
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(activityLoginBinding.root)

        val phone = activityLoginBinding.phone
        val tcAccepted = activityLoginBinding.tcAccepted
        val login = activityLoginBinding.login
        val loading = activityLoginBinding.loading

        tcAccepted.movementMethod = LinkMovementMethod.getInstance()

//        Check for permission and request if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPhoneStatePermission()
        } else {
            //TODO: isReachable test is required need to sort this out.
            populatePhoneNumber()
        }

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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || hasPhoneStatePermission()) {
            //TODO: isReachable test is required need to sort this out.
            populatePhoneNumber()
        }
    }

    //---> Activity Lifecycle calls -- END -->

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestPhoneStatePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_PHONE_NUMBERS),
                    REQUEST_PHONE_STATE_PERMISSION
                )
            } else {
                retrieveAndUpdateUIWithPhoneNumber()
            }
        } else {
            // For Android 10 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    REQUEST_PHONE_STATE_PERMISSION
                )
            } else {
                retrieveAndUpdateUIWithPhoneNumber()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun retrieveAndUpdateUIWithPhoneNumber() {
        retrieveMccMncNumber()
        Log.d(TAG, "Mcc Mnc  ${retrieveMccMncNumber()}")
        populatePhoneNumber()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun retrieveMccMncNumber(): String? {
        val phoneNumberUtil = PhoneNumberUtil(this)
        return phoneNumberUtil.getMccMncNumber()
    }

    private fun retrievePhoneNumber(): String? {
        val phoneNumberUtil = PhoneNumberUtil(this)
        return phoneNumberUtil.getPhoneNumber()
    }

    private fun populatePhoneNumber() {
        val phoneNumber = retrievePhoneNumber()
        if (!phoneNumber.isNullOrEmpty()) {
            activityLoginBinding.phone.setText(phoneNumber)
        } else {
            Log.d(TAG, "Phone number cannot be populated")
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PHONE_STATE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val mccMncNumber = retrieveMccMncNumber()
                phoneCheckViewModel.crossCheckPhoneNumberWithReachable(
                    mccMncNumber = mccMncNumber,
                    updateUI = this::updateUI
                )
            } else {
                //Permission denied
                Log.d(TAG, "Permission denied")
            }
        }
    }

    private fun updateUI(input: String) {
        println("NetworkId from Telephony/Subscription Manager: $input")
        populatePhoneNumber()
    }

    private fun hasPhoneStatePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_NUMBERS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        }
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
        private const val REQUEST_PHONE_STATE_PERMISSION = 1
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

