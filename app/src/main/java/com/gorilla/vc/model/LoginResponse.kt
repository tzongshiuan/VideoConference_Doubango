package com.gorilla.vc.model

import com.google.gson.annotations.SerializedName

class LoginResponse {
    @SerializedName("code")
    var code: Int = 0
    @SerializedName("message")
    var message: String? = null
    @SerializedName("innerMessage")
    var innerMessage: String? = null
}