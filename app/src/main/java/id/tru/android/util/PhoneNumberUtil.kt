package id.tru.android.util

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager

import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import id.tru.android.login.LoginActivity

/**
 * Class that performs phone check from the remote data source
 */
class PhoneNumberUtil(private val context: Context) {
    fun getPhoneNumber(): String? {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_NUMBERS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                telephonyManager.line1Number
            } else {
                null
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
                return if (activeSubscriptionInfoList != null && activeSubscriptionInfoList.isNotEmpty()) {
                    val subscriptionInfo = activeSubscriptionInfoList[0]
                    subscriptionInfo.number
                } else {
                    println(" subscriptionInfo.number null")
                    null
                }

            } else {
                return null
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getMccMncNumber(): String? {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_NUMBERS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                println("networkOperator from Telephony Manager: ${telephonyManager.networkOperator}")
                return telephonyManager.networkOperator

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
                val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
                println("subscriptionInfo List $activeSubscriptionInfoList")
                if (activeSubscriptionInfoList != null && activeSubscriptionInfoList.isNotEmpty()) {
                    val subscriptionInfo = activeSubscriptionInfoList[0]
                    println(" before subscriptionInfo ${subscriptionInfo.mccString} + ${subscriptionInfo.mncString}")
                    val mccString = subscriptionInfo.mccString
                    val mncString = subscriptionInfo.mncString
                    val mccMncString = "$mccString+$mncString"
                    println(" mccMncString $mccMncString")
                    return mccMncString
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





