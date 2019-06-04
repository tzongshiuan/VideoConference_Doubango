package com.gorilla.vc.view.ui.setting

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Switch
import com.gorilla.vc.model.VcManager
import org.doubango.ngn.sip.NgnAVSession

class SettingActivityHandler(private val context: SettingActivity,
                             private val vcManager: VcManager,
                             private val settingViewModel: SettingViewModel?) {

    val tag = SettingActivityHandler::class.simpleName

    val preferences = vcManager.mPreferences

    val mAVSession = settingViewModel?.mAVSession

    /*
    fun onVolumeValueChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        Log.d(tag, "Volume SeekBar value is changed to $progress")

        var newProgress = 0
        if (progress < 1) {
            newProgress = 1
            seekBar?.progress = newProgress
            return
        }

        // let activity to call [SettingViewModel::setVolume]
        settingViewModel?.setVolume(newProgress)
    }
    */

    fun onVolumeSwitchClick(view: View) {
        Log.d(tag, "onVolumeSwitchClick()")

        val switch= view as Switch
        preferences.isVolumeEnable = switch.isChecked

        setVolumeStatus()
    }

    fun onMicSwitchClick(view: View) {
        Log.d(tag, "onMicSwitchClick()")

        val switch= view as Switch
        preferences.isMicEnable = switch.isChecked

        setMicStatus()
    }

    fun onCameraSwitchClick(view: View) {
        Log.d(tag, "onCameraSwitchClick()")

        val switch= view as Switch
        preferences.isCameraEnable = switch.isChecked
        mAVSession?.isSendingVideo = preferences.isCameraEnable

        settingViewModel?.setCameraViewStatus(preferences.isCameraEnable)
        if (preferences.isCameraEnable) {
            mAVSession?.startCamera2(view.context)
        } else {
            mAVSession?.stopCamera2()
        }

        setCameraStatus()
    }

    fun setVolumeStatus() {
        // Send mute command
        Log.d(tag, "Set volume enable: ${preferences.isVolumeEnable}")
        mAVSession?.setSpeakerphoneOn(preferences.isVolumeEnable)

//        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        if (preferences.isVolumeEnable) {
//            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 1, 0)
//        } else {
//            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, 0)
//        }

        settingViewModel?.resetVolumeStatus()
    }

    fun setMicStatus() {
        // Send mute command
        mAVSession?.isOnMute = !preferences.isMicEnable
    }

    fun setCameraStatus() {
        // Stop video producer
        mAVSession?.isSendingVideo = preferences.isCameraEnable

        val localPreview = mAVSession?.startVideoProducerPreview()
        if (localPreview != null) {
            if (!preferences.isCameraEnable) {
                localPreview.visibility = View.GONE
            } else {
                localPreview.visibility = View.VISIBLE
            }
        }
    }

    fun onConfirmBtnClick() {
        Log.d(tag, "onConfirmBtnClick()")

        preferences.savePreferences()
        context.finish()
    }
}