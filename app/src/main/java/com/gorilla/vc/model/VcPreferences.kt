package com.gorilla.vc.model

import android.content.Context
import android.util.Log
import com.gorilla.vc.utils.DefaultSettings
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VcPreferences @Inject constructor(val context: Context) : PreferencesHelper{

    private val tag = VcPreferences::class.simpleName

    private val APP_VERSION = "version"
    private val SIP_SETTING = "sip_setting"
    private val SETTING_ACCOUNT = "account"
    private val SETTING_PASSWORD = "password"
    private val SETTING_ADDRESS = "ip"
    private val SETTING_VOLUME_ENABLE = "volume_enable"
    private val SETTING_MIC_ENABLE = "mic_enable"
    private val SETTING_CAMERA_ENABLE = "camera_enable"

    override var account = ""
    override var password = ""
    override var ip = ""
    override var isVolumeEnable = false
    override var isMicEnable = false
    override var isCameraEnable = false

    private fun packPreferencesToJson(): JSONObject {
        val jSetting = JSONObject()
        jSetting.put(SETTING_ACCOUNT, account)
        jSetting.put(SETTING_PASSWORD, password)
        jSetting.put(SETTING_ADDRESS, ip)
        jSetting.put(SETTING_VOLUME_ENABLE, isVolumeEnable)
        jSetting.put(SETTING_MIC_ENABLE, isMicEnable)
        jSetting.put(SETTING_CAMERA_ENABLE, isCameraEnable)

        return jSetting
    }

    private fun initPreferences() {
        account         = DefaultSettings.ACCOUNT
        password        = DefaultSettings.PASSWORD
        ip              = DefaultSettings.ADDRESS
        isVolumeEnable  = DefaultSettings.VOLUME_ENABLE
        isMicEnable     = DefaultSettings.MIC_ENABLE
        isCameraEnable  = DefaultSettings.CAMERA_ENABLE
    }

    override fun readPreferences() {
        val preferences = context.getSharedPreferences("preference", Context.MODE_PRIVATE)

        // to check whether use the setting in the same version
        val versionName = preferences.getString(APP_VERSION, "")
        if (versionName != getVersionName()) {
            Log.d(tag, "Read reference failed, previous version ${getVersionName()}")
            initPreferences()
            return
        }

        try {
            val jSettings = JSONObject(preferences.getString(SIP_SETTING, ""))
            Log.d(tag, "Read preference Setting: $jSettings")

            // read setting from preferences
            account         = jSettings.getString(SETTING_ACCOUNT)
            password        = jSettings.getString(SETTING_PASSWORD)
            ip              = jSettings.getString(SETTING_ADDRESS)
            isVolumeEnable  = jSettings.getBoolean(SETTING_VOLUME_ENABLE)
            isMicEnable     = jSettings.getBoolean(SETTING_MIC_ENABLE)
            isCameraEnable  = jSettings.getBoolean(SETTING_CAMERA_ENABLE)
        } catch (e: Exception) {
            Log.d(tag, e.toString())
        }
    }

    override fun savePreferences() {
        val preferences = context.getSharedPreferences("preference", Context.MODE_PRIVATE)
        val preferenceEditor = preferences.edit()

        preferenceEditor.putString(SIP_SETTING, packPreferencesToJson().toString())
        preferenceEditor.putString(APP_VERSION, getVersionName())
        preferenceEditor.apply()
    }

    /**
     * Get App version name
     */
    private fun getVersionName(): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName
    }
}