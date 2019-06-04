package com.gorilla.vc.model

class MessageList(var id: Int?) {

    var messages: ArrayList<ChatMessage> ?= null

    init {
        this.messages = ArrayList()
    }

    override fun equals(other: Any?): Boolean {
        val messageList = other as MessageList

        return (this.id == messageList.id)
    }

    override fun hashCode(): Int {
        return id ?: 0
    }
}