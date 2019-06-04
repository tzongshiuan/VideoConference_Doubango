package com.gorilla.vc.model

import com.google.gson.annotations.SerializedName

class Participant {

    @SerializedName("id")
    var id: String ?= null

    @SerializedName("name")
    var name: String ?= null

    @SerializedName("password")
    var password: String ?= null

    @SerializedName("emailAddress")
    var email: String ?= null

    @SerializedName("selfieImageLocation")
    var imageLocation: String ?= null

    @SerializedName("isValid")
    var isValid: Int ?= null

    @SerializedName("createDate")
    var createDate: String ?= null

    @SerializedName("memberId")
    var memberId: String ?= null

    @SerializedName("meetingRoomMapParticipants")
    var joinSessions: ArrayList<SessionParticipant> ?= null

    @SerializedName("roleId")
    var roleId: String ?= null

    @SerializedName("rank")
    var rank: String ?= null

    @SerializedName("duties")
    var duties: String ?= null

    @SerializedName("unit")
    var unit: String ?= null
}