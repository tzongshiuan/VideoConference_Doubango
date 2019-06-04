package com.gorilla.vc.model


class ChatMessage {

    var id: String ?= null

    var name: String?= null

    var time: String ?= null

    var message: String ?= null

    var isSystemMessage = false

    constructor()

    /**
     * @param chatMessage message
     * @param isGlobal is global chat message
     */
    constructor(dateTime: String, userId: String, message: String, isGlobal: Boolean = true) {
        if (isGlobal) {
            this.id = userId
            this.time = dateTime
            this.message = message
        } else {
            this.id = userId
            this.time = dateTime
            this.message = message
        }
    }

    override fun equals(other: Any?): Boolean {
        val chatMessage = other as ChatMessage

        return (this.id == chatMessage.id)
                && (this.time == chatMessage.time)
                && (this.message == chatMessage.message)
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (time?.hashCode() ?: 0)
        result = 31 * result + (message?.hashCode() ?: 0)
        return result
    }
}