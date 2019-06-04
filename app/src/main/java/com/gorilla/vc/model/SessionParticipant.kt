package com.gorilla.vc.model

import com.google.gson.annotations.SerializedName

open class SessionParticipant {

    @SerializedName("id")
    var id: String ?= null

    @SerializedName("isOnline")
    var isOnline: Int ?= null

    override fun equals(other: Any?): Boolean {
        val participant = other as SessionParticipant

        return (this.id == participant.id)
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}