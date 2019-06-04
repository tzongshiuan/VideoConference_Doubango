package com.gorilla.vc.model

import android.content.Context
import android.util.Log
import org.doubango.ngn.NgnEngine
import org.doubango.ngn.services.impl.NgnSipService
import org.doubango.ngn.utils.NgnConfigurationEntry
import org.doubango.tinyWRAP.MediaSessionMgr
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VcManager @Inject constructor(val context: Context,val mPreferences:PreferencesHelper,val mEngine:NgnEngine): NgnEngine(), IVcManagerCallback {

    private val tag = VcManager::class.simpleName

    val mConfigService = configurationService

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
    
    var sipInfo: SessionSipInfo ?= null
    var sessionInfo: OnGoingVcSession ?= null

    var idToNameMap = HashMap<String, String>()

    fun startEngine() {
        initSipSettings()
        mEngine.start()
    }

    fun stopEngine() {
        mEngine.stop()
    }

    fun initSipSettings() {
        mConfigurationService.putInt(NgnConfigurationEntry.MEDIA_CODECS, 0x10FFF0)
        MediaSessionMgr.defaultsSetStunEnabled(true) // Public IP/port in SIP Contact/Via headers and SDP connection info.
        MediaSessionMgr.defaultsSetStunServer(sipInfo?.proxyIp, 3478)
    }

    fun initIdToNameMap(list: ArrayList<Participant>) {
        idToNameMap.clear()

        list.forEach { participant ->
            if (participant.id != null && participant.name != null) {
                idToNameMap[participant.id!!] = participant.name!!
            }
        }
    }

    fun getNameFromId(id: String?): String? {
        if (id == null) {
            return null
        }

        if (idToNameMap.containsKey(id)) {
            return idToNameMap[id]
        }

        return "Default_$id"
    }

    private fun useCurrentSettings() {
        // for test
        //sipInfo?.proxyIp = "192.168.11.26"

        // about identity
        mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, mPreferences.account)

        val publicId = String.format("sip:%s@%s", userId, sipInfo?.proxyIp)
        mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, publicId)
        mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, mPreferences.account)
        // mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, mPreferences.password)
        mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, sipInfo?.proxyIp)

        // about network
        mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST, sipInfo?.proxyIp)
        mConfigurationService.putInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT, 20080)

        mConfigurationService.putString(NgnConfigurationEntry.NETWORK_TRANSPORT, "TCP")
        mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY, NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_DISCOVERY)

        mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_SIGCOMP,  false)
        mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_WIFI, true)
        mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_3G, true)
        mConfigurationService.putString(NgnConfigurationEntry.NETWORK_IP_VERSION, "ipv4")

        mConfigurationService.commit()
    }

    /**
     * register to SIP server with specific IP getting from [OnGoingVcSession]
     */
    @Synchronized fun register(): Boolean {
        Log.d(tag, "register()")

        if (userId.isNullOrEmpty() || sipInfo?.proxyIp.isNullOrEmpty()) {
            return false
        }

        useCurrentSettings()

        return mEngine.sipService!!.register(context)
    }

    @Synchronized fun unRegister(): Boolean {
        Log.d(tag, "unregister()")

        return mEngine.sipService!!.unRegister()
    }

    companion object {
        var DEBUG_OPTIONS_ENABLE = false     // in release version, need to be false

        var DEBUG_IS_ENABLE_STUN = true
        set(value) {
            NgnSipService.IS_ENABLE_STUN = value
            field = value
        }

        var DEBUG_AV_CHAT_PORT_APART = false
        set(value) {
            NgnSipService.IS_AV_CHAT_PORT_APART = value
            field = value
        }

        var DEBUG_CONCALL_BY_VIRTUAL_SESSION = false

        var DEBUG_QOS = false
    }
}