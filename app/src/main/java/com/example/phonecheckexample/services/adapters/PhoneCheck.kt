package com.example.phonecheckexample.services.adapters

import android.os.Build
import androidx.annotation.RequiresApi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

import com.example.phonecheckexample.data.model.PhoneCheck
import com.example.phonecheckexample.data.model.PhoneCheckPost
import com.example.phonecheckexample.data.model.PhoneCheckResult

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