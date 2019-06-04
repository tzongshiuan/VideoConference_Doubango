package com.gorilla.vc.model


interface PreferencesHelper {
    var account : String
    var password : String
    var ip: String
    var isVolumeEnable: Boolean
    var isMicEnable: Boolean
    var isCameraEnable: Boolean
    fun readPreferences()
    fun savePreferences()
}