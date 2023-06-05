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
class PhoneNumberUtil() {
    /**
     * This method receives the JSONObject returned by isReachable method. Specifically, the
     * body of the response.
     *      jsonResponse.optJSONObject("response_body")
     * It compares the network operator received from either the Telephony Manager (Android 11 and above)
     * or the Subscription Manager (Android 10 and below) to the network aliases from reachability
     * response body and gets the phone number if it's a match.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getDataConnectivityPhoneNumber(reachabilityResponseBody: JSONObject, context: Context): String? {
        val networkId = reachabilityResponseBody.optString("network_id")
        val networkArray = reachabilityResponseBody.getJSONArray("network_aliases")
        val networkAliases: MutableList<String> = mutableListOf()
        for (i in 0 until networkArray.length()) {
            val alias = networkArray.getString(i)
            networkAliases.add(alias)
        }
        networkAliases.add(networkId)
        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_NUMBERS
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val subscriptionManager =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
            val defaultDataSlotId = SubscriptionManager.getDefaultDataSubscriptionId()
            if (!activeSubscriptionInfoList.isNullOrEmpty()) {
                for (subscriptionInfo in activeSubscriptionInfoList) {
                    if (subscriptionInfo.subscriptionId == defaultDataSlotId) {
                        val mccString = subscriptionInfo.mccString
                        val mncString = subscriptionInfo.mncString
                        val mccMncString = "$mccString$mncString"
                        if (mccMncString.isNotEmpty()) {
                            for (item in networkAliases) {
                                if (item == mccMncString) {
                                    return subscriptionInfo.number
                                }
                            }
                        }
                    }
                }
            }
        }
        return null
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




