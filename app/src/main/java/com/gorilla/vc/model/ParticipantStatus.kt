package com.gorilla.vc.model

import com.google.gson.annotations.SerializedName

class ParticipantStatus {

    @SerializedName("id")
    var id: Int ?= null

    @SerializedName("isMicEnabled")
    var isMicEnabled: Boolean ?= null

    @SerializedName("isCamEnabled")
    var isCamEnabled: Boolean ?= null

    @SerializedName("streamingContent")
    var streamingContent: Int ?= null

    @SerializedName("rtspUrl")
    var rtspUrl: String ?= null

    @SerializedName("streamingContentstatus")
    var streamingContentStatus: Int ?= null

    override fun equals(other: Any?): Boolean {
        val status = other as ParticipantStatus

        return (this.id == status.id)
    }

    override fun hashCode(): Int {
        return id ?: 0
    }
}