package id.tru.android.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import id.tru.android.model.PhoneCheckRepository
import id.tru.android.model.Result
import id.tru.android.R
import id.tru.android.model.VerificationCheckResult
import kotlinx.coroutines.launch

class PhoneCheckViewModel(private val phoneCheckRepository: PhoneCheckRepository) : ViewModel() {

    private val _verificationFormState = MutableLiveData<VerificationFormState>()
    val verificationFormState: LiveData<VerificationFormState> = _verificationFormState

    private val _phoneCheckResult = phoneCheckRepository.dataSourcePhoneCheckResult//MutableLiveData<PhoneCheckResult>()
    val phoneCheckResult: LiveData<VerificationCheckResult> = _phoneCheckResult

    fun login(phoneNumber: String) {

        // Any coroutines launched from viewModelScope without a Dispatchers.IO param, runs in the main thread.
        viewModelScope.launch {

            // signIn moves the execution off the main thread
            // Make the network call and suspend execution until it finishes
            val result: Result<VerificationCheckResult> = try {
                phoneCheckRepository.signIn(phoneNumber)
            } catch (e: Exception) {
                Result.Error(e)
            }
            // Display result of the network request to the user
            // Update _phoneCheckResults.value either error or success.
            when (result) {
                is Result.Success -> _phoneCheckResult.value = VerificationCheckResult(success = result.data.success)
                is Result.Error -> {
                    _phoneCheckResult.value = VerificationCheckResult(error = R.string.login_failed)
                    Log.e(TAG, result.exception.toString())
                }
                else -> _phoneCheckResult.value = VerificationCheckResult(error = R.string.invalid_app_state)
            }
        }
    }

    fun loginDataChanged(phoneNumber: String, tcAccepted: Boolean) {
        if (tcAccepted) {
            if (isPhoneNumberValid(phoneNumber)) {
                _verificationFormState.value = VerificationFormState(isDataValid = true)
            } else {
                _verificationFormState.value = VerificationFormState(phoneNumberError = R.string.invalid_phone_number)
            }
        } else {
            _verificationFormState.value = VerificationFormState(isDataValid = false)
        }
    }

    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
        return Patterns.PHONE.matcher(phoneNumber).matches()
    }

    companion object {
        private const val TAG = "PhoneCheckViewModel"
    }

}