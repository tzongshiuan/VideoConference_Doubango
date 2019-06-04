package com.gorilla.vc.model

import org.json.JSONArray
import org.json.JSONObject

class OnGoingVcSession(baseSession: BaseVcSession) : BaseVcSession() {

    val isReserve: Boolean = false

    var hostName: String ?= null

    init {
        this.id = baseSession.id
        this.name = baseSession.name
        this.password = baseSession.password
        this.startDate = baseSession.startDate
        this.endDate = baseSession.endDate
        this.hostId = baseSession.hostId
        this.creatorId = baseSession.creatorId
        this.recordDefault = baseSession.recordDefault
        this.hasRecord = baseSession.hasRecord
        this.agenda = baseSession.agenda
        this.status = baseSession.status
        this.createDate = baseSession.createDate
        this.sip = baseSession.sip
        this.participants = baseSession.participants
    }
}