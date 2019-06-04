package com.gorilla.vc.model

import com.google.gson.annotations.SerializedName

class PrivateMessageUser {
    constructor(id: Int?, name: String?) {
        this.id = id
        this.name = name
    }

    var id: Int ?= null

    var name: String ?= null

    var isHaveUnreadMessage = false

    var isOnline = false

    // Discarded
    var sessionId: Long ?= null

    override fun equals(other: Any?): Boolean {
        val msgUser = other as PrivateMessageUser

        return (this.id == msgUser.id)
    }

    override fun hashCode(): Int {
        return id ?: 0
    }
}