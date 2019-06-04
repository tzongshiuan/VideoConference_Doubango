package com.gorilla.vc.model

import com.google.gson.annotations.SerializedName


class RtspInfo {

    /* Ex:
     {
        "channel": 1,
        "name": "TEST2",
        "description": "",
        "location": "衛生科，衛生科一樓",
        "url": "rtsp://192.168.2.69:8554/live/ch1?stream=0",
        "cameraIp": "10.1.99.87",
        "onlineTime": "00:00-00:00",
        "repairStatus": false
     },
     */

    @SerializedName("channel")
    var channel: Int? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("description")
    var description: String? = null

    @SerializedName("location")
    var location: String? = null

    @SerializedName("url")
    var url: String? = null

    @SerializedName("cameraIp")
    var cameraIp: String? = null

    @SerializedName("onlineTime")
    var onlineTime: String? = null

    @SerializedName("repairStatus")
    var repairStatus: Boolean ?= null
}