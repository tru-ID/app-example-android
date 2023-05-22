package id.tru.android.util

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import id.tru.android.R
import id.tru.android.login.VerifiedPhoneNumberView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber

/**
 * Class that performs phone check from the remote data source
 */
class PhoneNumberUtil(private val context: Context) {
    fun getPhoneNumber(): String? {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_NUMBERS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return telephonyManager.line1Number
            } else {
                return null
            }
        } else {
            // For Android 10 and below
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
                println("subscriptionInfo List $activeSubscriptionInfoList")
                if (activeSubscriptionInfoList != null && activeSubscriptionInfoList.isNotEmpty()) {
                    val subscriptionInfo = activeSubscriptionInfoList[0]
                    println(" before subscriptionInfo.number $subscriptionInfo.number")
                    return subscriptionInfo.number
                    println(" subscriptionInfo.number $subscriptionInfo.number")
                } else {
                    println(" subscriptionInfo.number null")
                    return null
                }

            } else {
                return null
            }
        }

    }

    fun isPhoneNumberValid(phoneNumber: String): Boolean {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        return try {
            phoneNumberUtil.isValidNumber(
                phoneNumberUtil.parse(
                    phoneNumber, Phonenumber.PhoneNumber.CountryCodeSource.UNSPECIFIED.name
                )
            )
        } catch (e: NumberParseException) {
            false
        }
    }
}





