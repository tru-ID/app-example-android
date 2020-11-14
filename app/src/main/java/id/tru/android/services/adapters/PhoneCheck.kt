package id.tru.android.services.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class PhoneCheckAdapter {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val mediaType = "application/json; charset=utf-8;".toMediaType()

    fun getPhoneCheckPostBody(phoneNumber: String): RequestBody {
        val phoneCheckPostJsonAdapter: JsonAdapter<PhoneCheckPost> = moshi.adapter(PhoneCheckPost::class.java)
        println(phoneNumber)
        val phoneCheckPost = PhoneCheckPost(phone_number = phoneNumber)
        return phoneCheckPostJsonAdapter.toJson(phoneCheckPost).toRequestBody(mediaType)
    }

    fun parsePhoneCheckResponse(response: String): PhoneCheck {
        val phoneCheckJsonAdapter:JsonAdapter<PhoneCheck> = moshi.adapter(PhoneCheck::class.java)
        return phoneCheckJsonAdapter.fromJson(response)!!
    }

    fun parsePhoneCheckResults(response: String): PhoneCheckResult {
        val phoneCheckResultJsonAdapter:JsonAdapter<PhoneCheckResult> = moshi.adapter(PhoneCheckResult::class.java)
        return phoneCheckResultJsonAdapter.fromJson(response)!!
    }
}


@JsonClass(generateAdapter = true)
class PhoneCheckPost(
        var phone_number: String
)

@JsonClass(generateAdapter = true)
class PhoneCheck(
        val check_url: String,
        val check_id: String
)

@JsonClass(generateAdapter = true)
class PhoneCheckResult(
        val match: Boolean,
        val check_id: String
)