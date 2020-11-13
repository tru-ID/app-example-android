package id.tru.android.login

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import id.tru.android.data.LoginRepository
import id.tru.android.data.Result

import com.example.phonecheckexample.R
import id.tru.android.data.model.LoggedInUser
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun login(phoneNumber: String) {
        // can be launched in a separate asynchronous job
        viewModelScope.launch {
            val result = loginRepository.login(phoneNumber)

            when(result) {
                is Result.Success<LoggedInUser> -> {
                    _loginResult.value =
                        LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
                }
                else -> _loginResult.value = LoginResult(error = R.string.login_failed)
            }
        }

    }

    fun loginDataChanged(phoneNumber: String) {
        if (!isPhoneNumberValid(phoneNumber)) {
            _loginForm.value = LoginFormState(phoneNumberError = R.string.invalid_phone_number)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun isPhoneNumberValid(phoneNumber: String): Boolean {
        return phoneNumber.isNotBlank() && Patterns.PHONE.matcher(phoneNumber).matches()
    }

}