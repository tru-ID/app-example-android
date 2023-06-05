package id.tru.android.util

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import androidx.core.app.ActivityCompat
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import org.json.JSONObject

/**
 * Class that provides methods for getting phone number and checking if phone number is valid
 */
class PhoneNumberUtil {
    /**
     * This method receives the JSONObject returned by isReachable method. Specifically, the
     * body of the response.
     *      jsonResponse.optJSONObject("response_body")
     * It compares the MCC + MNC received from the Subscription Manager to the network aliases from
     * the reachability response body and gets the phone number if it's a match.
     * It requires an Android 29 (10) and above)
     */
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




