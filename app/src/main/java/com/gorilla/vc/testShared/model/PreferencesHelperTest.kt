package com.gorilla.vc.testShared.model


open class PreferencesHelperTest {
    var account : String = "hsuan"
    var password : String = "hsuan"
    var ip: String = "192.168.11.45"
    var isVolumeEnable: Boolean = true
    var isMicEnable: Boolean = true
    var isCameraEnable: Boolean = true

    open fun readPreferences(str: String): String {
        return str
    }

    open fun savePreferences(str: String): String {
        return str
    }
}