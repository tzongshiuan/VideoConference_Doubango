package com.gorilla.vc.model

import com.google.gson.JsonObject

class ParticipantListener(id: Int?, department: String?, title: String?, name: String?) {

    val jsonContent = JsonObject()

    init {
        jsonContent.addProperty("id", id)
        jsonContent.addProperty("department", department)
        jsonContent.addProperty("title", title)
        jsonContent.addProperty("name", name)
    }
}