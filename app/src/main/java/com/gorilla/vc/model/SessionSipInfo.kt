package com.gorilla.vc.model

import com.google.gson.annotations.SerializedName

open class SessionSipInfo {

    @SerializedName("id")
    var id: String ?= null

    @SerializedName("proxyIp")
    var proxyIp: String ?= null

    @SerializedName("proxyPort")
    var proxyPort: String ?= null

    @SerializedName("videoCodecs")
    var videoCodecs: String ?= null

    @SerializedName("audioCodecs")
    var audioCodecs: String ?= null
}