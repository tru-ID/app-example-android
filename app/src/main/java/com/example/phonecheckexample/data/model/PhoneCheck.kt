package com.example.phonecheckexample.data.model

import com.squareup.moshi.JsonClass

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