package id.tru.android.login

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
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
import id.tru.sdk.TruSDK


/**
 * Add blazingly fast mobile phone verification to your app for 2FA or passwordless onboarding.
 * Leveraging the tru.ID PhoneCheck API confirms ownership of a mobile phone number by verifying
 * the possession of an active SIM card with the same number.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var phoneCheckViewModel: PhoneCheckViewModel
    private lateinit var binding: ActivityLoginBinding

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TruSDK.initializeSdk(applicationContext)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val phone = binding.phone
        val tcAccepted = binding.tcAccepted
        val tcLink = binding.tcLink
        val login = binding.login
        val loading = binding.loading

        tcLink.text = Html.fromHtml("<a href='https://tru.id/terms'>tru.ID Terms and Conditions</a>")
        tcLink.movementMethod = LinkMovementMethod.getInstance()

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

//        phoneNumber.afterTextChanged {
//            if (!isPhoneNumberValid(binding.phoneNumber.text.toString())) {
//                binding.step1Tv.text = getString(R.string.phone_check_step1_errror)
//                binding.loginButton.isEnabled = false
//            }
//        }
//
//        tcAccepted.apply {
//            setOnCheckedChangeListener { buttonView, isChecked ->
//                if (isPhoneNumberValid(binding.phoneNumber.text.toString()) && isChecked) {
//                    binding.loginButton.isEnabled = isChecked
//                }
//            }
//        }
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

