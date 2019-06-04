package com.gorilla.vc.model

import com.google.gson.annotations.SerializedName

open class BaseVcSession {

    @SerializedName("id")
    var id: String ?= null

    @SerializedName("name")
    var name: String ?= null

    @SerializedName("password")
    var password: String ?= null

    @SerializedName("startDate")
    var startDate: String ?= null

    @SerializedName("endDate")
    var endDate: String ?= null

    @SerializedName("hostParticipantId")
    var hostId: String ?= null

    @SerializedName("creatorParticipantId")
    var creatorId: String ?= null

    @SerializedName("recordDefault")
    var recordDefault: Int ?= null

    @SerializedName("hasRecord")
    var hasRecord: Int ?= null

    @SerializedName("agenda")
    var agenda: String ?= null

    @SerializedName("status")
    var status: Int ?= null

    @SerializedName("createDate")
    var createDate: String ?= null

    @SerializedName("sip")
    var sip: SessionSipInfo ?= null

    @SerializedName("meetingRoomMapParticipants")
    var participants: ArrayList<SessionParticipant> ?= null
}