package id.tru.android.util

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import org.json.JSONObject

/**
 * Class that gets phone number from TelephonyManager or Subscription Manager
 */
class PhoneNumberUtil(private val context: Context) {
    /**
     * This method receives the JSONObject returned by isReachable method. Specifically, the
     * body of the response.
     *      jsonResponse.optJSONObject("response_body")
     * It compares the network operator received from either the Telephony Manager (Android 11 and above)
     * or the Subscription Manager (Android 10 and below) to the network aliases from reachability
     * response body and gets the phone number if it's a match.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getDataConnectivityPhoneNumber(reachabilityResponseBody: JSONObject): String? {
        val networkId = reachabilityResponseBody.optString("network_id")
        val networkArray = reachabilityResponseBody.getJSONArray("network_aliases")
        val networkAliases: MutableList<String> = mutableListOf()
        for (i in 0 until networkArray.length()) {
            val alias = networkArray.getString(i)
            networkAliases.add(alias)
        }
        networkAliases.add(networkId)
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val subscriptionManager =
            context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        // For Android 11 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_NUMBERS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val networkOperator = telephonyManager.networkOperator
                println("networkOperator from Telephony Manager: ${telephonyManager.networkOperator}")
                if (!networkOperator.isNullOrEmpty()) {
                    for (item in networkAliases) {
                        if (item == networkOperator) {
                            return telephonyManager.line1Number
                        }
                    }
                    return null
                } else {
                    return null
                }
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
                if (!activeSubscriptionInfoList.isNullOrEmpty()) {
                    val subscriptionInfo = activeSubscriptionInfoList[0]
                    val mccString = subscriptionInfo.mccString
                    val mncString = subscriptionInfo.mncString
                    val mccMncString = "$mccString+$mncString"
                    println(" mccMncString $mccMncString")
                    if (!mccMncString.isNullOrEmpty()) {
                        for (item in networkAliases) {
                            if (item == mccMncString) {
                                return subscriptionInfo.number
                            }
                        }
                        return null
                    } else {
                        return null
                    }
                } else {
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




