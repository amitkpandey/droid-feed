package com.droidfeed.data.api.mailchimp

import android.support.annotation.Keep
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException

internal class ErrorAdapter {

    @FromJson
    @Keep
    fun fromJson(errorJson: ErrorJson): Error {
        val errorType = when (errorJson.title) {
            "Member Exists" -> ErrorType.MEMBER_ALREADY_EXIST
            "Invalid Resource" -> ErrorType.INVALID_RESOURCE
            else -> throw JsonDataException("unknown suit: $errorJson.title")
        }

        return Error(errorType, errorJson.status, errorJson.detail)
    }
}