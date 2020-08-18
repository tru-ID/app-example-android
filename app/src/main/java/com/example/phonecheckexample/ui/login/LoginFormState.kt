package com.example.phonecheckexample.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(val phoneNumber: String? = null,
                          val phoneNumberError: Int? = null,
                          val isDataValid: Boolean = false)