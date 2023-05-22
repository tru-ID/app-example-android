package id.tru.android.login

import android.Manifest
import android.app.Activity
import android.content.Context
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
import com.google.android.material.textfield.TextInputEditText
import id.tru.android.R
import id.tru.android.databinding.ActivityLoginBinding
import id.tru.android.model.Step
import id.tru.android.util.PhoneNumberUtil
import android.telephony.TelephonyManager
import id.tru.sdk.TruSDK


/**
 * Add blazingly fast mobile phone verification to your app for 2FA or passwordless onboarding.
 * Leveraging the tru.ID PhoneCheck API confirms ownership of a mobile phone number by verifying
 * the possession of an active SIM card with the same number.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var phoneCheckViewModel: PhoneCheckViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val phone = binding.phone
        val tcAccepted = binding.tcAccepted
        val login = binding.login
        val loading = binding.loading

        tcAccepted.movementMethod = LinkMovementMethod.getInstance()

//         Check for permission and request if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) !=
                PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_PHONE_NUMBERS),
                    REQUEST_PHONE_STATE_PERMISSION
                )
            } else {
                retrievePhoneNumber()
            }
        } else {
            // For Android 10 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) !=
                PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    REQUEST_PHONE_STATE_PERMISSION
                )
            } else {
                retrievePhoneNumber()
            }
        }


        phoneCheckViewModel = ViewModelProvider(this, VerifyViewModelFactory()).get(PhoneCheckViewModel::class.java)

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
                when(step) {
                    Step.FIRST -> {
                        binding.progressStep1.visibility = View.VISIBLE
                        binding.progressStep1.check()
                        binding.step1Tv.text = msg //success or error msg
                    }
                    Step.SECOND -> {
                        binding.step2.visibility = View.VISIBLE
                        if (shouldCheck) binding.progressStep2.check()
                        binding.step2Tv.text = msg
                    }
                    Step.THIRD -> {
                        binding.step3.visibility = View.VISIBLE
                        if (shouldCheck) binding.progressStep3.check()
                        binding.step3Tv.text = msg //Either done on cellular or not
                    }
                    Step.FOURTH -> {
                       binding.step4.visibility = View.VISIBLE
                        if (shouldCheck) binding.progressStep4.check()
                        binding.step4Tv.text = msg //success or error msg
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
                phoneCheckViewModel.loginDataChanged(phone.text.toString(), tcAccepted = tcAccepted.isChecked)
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
    private fun retrievePhoneNumber():String?  {
        val phoneNumberUtil = PhoneNumberUtil(this)
        val phoneNumber = phoneNumberUtil.getPhoneNumber()
        return phoneNumber
        Log.d(TAG, "phoneNumber  ${phoneNumber}" )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PHONE_STATE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Before phone number retrieved")
               retrievePhoneNumber()
                val phoneNumber = retrievePhoneNumber()
                Log.d(TAG, "phone number retrieved")
                Log.d(TAG, "Phone number from Telephony Manager: $phoneNumber")
            } else {
                //Permission denied
                Log.d(TAG, "Permission denied")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: TruSDK is being initialised")
        TruSDK.initializeSdk(applicationContext)
    }

    private fun resetProgress() {
        binding.loadingLayout.visibility = View.VISIBLE

        binding.step1.visibility = View.INVISIBLE
        binding.step2.visibility = View.INVISIBLE
        binding.step3.visibility = View.INVISIBLE
        binding.step4.visibility = View.INVISIBLE

        binding.progressStep1.uncheck()
        binding.progressStep2.uncheck()
        binding.progressStep3.uncheck()
        binding.progressStep4.uncheck()

    }

    private fun updateUIonError(additionalInfo: String) {
        Log.e(TAG, "$additionalInfo")
        binding.phone.setText("")
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

    private fun updateUIonSuccess(model: VerifiedPhoneNumberView) {
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

