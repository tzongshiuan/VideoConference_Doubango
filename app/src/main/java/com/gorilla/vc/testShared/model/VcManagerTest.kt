package com.gorilla.vc.testShared.model

import com.gorilla.vc.model.OnGoingVcSession
import com.gorilla.vc.model.SessionParticipant
import com.gorilla.vc.model.SessionSipInfo

open class VcManagerTest {

    var mPreferences = PreferencesHelperTest()

    var userId: String ?= null
        @Synchronized
        set(value) {
            field = value
        }

    var joinSessions = ArrayList<SessionParticipant>()
        @Synchronized
        set(value) {
            field = value
        }

    var sipInfo: SessionSipInfo?= null
    var sessionInfo: OnGoingVcSession?= null

    var idToNameMap = HashMap<String, String>()
}