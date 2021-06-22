package id.tru.android.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.tru.android.model.PhoneCheckDataSource
import id.tru.android.model.PhoneCheckRepository

/**
 * ViewModel provider factory to instantiate VerifyViewModel.
 * Required given VerifyViewModel has a non-empty constructor
 */
class VerifyViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(PhoneCheckViewModel::class.java)) {
            return PhoneCheckViewModel( PhoneCheckRepository(PhoneCheckDataSource()) ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}