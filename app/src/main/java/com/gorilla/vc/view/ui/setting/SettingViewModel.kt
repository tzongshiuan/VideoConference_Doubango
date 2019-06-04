package com.gorilla.vc.view.ui.setting

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.graphics.Bitmap
import android.media.AudioManager
import android.os.Build
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.gorilla.vc.di.Injectable
import com.gorilla.vc.model.VcManager
import org.doubango.ngn.sip.NgnAVSession
import org.doubango.ngn.utils.NgnStringUtils
import javax.inject.Inject

// Because we want to keep persistence of data, so I use application to instead of context here
class SettingViewModel @Inject constructor(val vcManager: VcManager)
    : Injectable, ISettingViewModelCallback, ViewModel() {

    private val tag = SettingViewModel::class.simpleName

    val mPreferences = vcManager.mPreferences

    val volumeValueLiveData: MutableLiveData<Int> = MutableLiveData()
    val isVolumeEnableLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val micValueLiveData: MutableLiveData<Int> = MutableLiveData()
    val isMicEnableLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val isCameraEnableLiveData: MutableLiveData<Boolean> = MutableLiveData()

    var mAVSession: NgnAVSession? = null

    var audioManager: AudioManager? = null

    var mCameraPreview: FrameLayout ?= null
    var mCameraImage: ImageView? = null

    /**
     * Expose the LiveData [volumeValueLiveData]
     */
    fun getVolumeValueObservable() : MutableLiveData<Int> {
        return volumeValueLiveData
    }

    /**
     * Expose the LiveData [isVolumeEnableLiveData]
     */
    fun getIsVolumeEnableObservable() : MutableLiveData<Boolean> {
        return isVolumeEnableLiveData
    }

    /**
     * Expose the LiveData [micValueLiveData]
     */
    fun getMicValueObservable() : MutableLiveData<Int> {
        return micValueLiveData
    }

    /**
     * Expose the LiveData [isMicEnableLiveData]
     */
    fun getIsMicEnableObservable() : MutableLiveData<Boolean> {
        return isMicEnableLiveData
    }

    /**
     * Expose the LiveData [isCameraEnableLiveData]
     */
    fun getIsCameraEnableObservable() : MutableLiveData<Boolean> {
        return isCameraEnableLiveData
    }

    fun setCameraViewStatus(isEnable: Boolean) {
        if (isEnable) {
            mCameraImage?.visibility = View.VISIBLE
        } else {
            mCameraImage?.visibility = View.INVISIBLE
        }
    }

    fun initAVSession(sessionId: String) {
        mAVSession = NgnAVSession.getSession(NgnStringUtils.parseLong(sessionId, -1))
        mAVSession?.setSettingCameraView(mCameraImage)
        setCameraViewStatus(mPreferences.isCameraEnable)
    }

    fun destroyAVSession() {
        mAVSession?.setSettingCameraView(null)
    }

    fun setVolume(index: Int) {
        audioManager?.setStreamVolume(AudioManager.STREAM_VOICE_CALL, index, AudioManager.STREAM_VOICE_CALL)

        //val size = audioManager?.getStreamVolume(AudioManager.STREAM_VOICE_CALL)
        //Log.d(tag, "setStreamVolume:  $size")
    }

    fun resetVolumeStatus() {
        //val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val value = audioManager?.getStreamVolume(AudioManager.STREAM_VOICE_CALL)
        volumeValueLiveData.value = value

        /**
         * Phone call can not set mute on Android device
         */
        // check whether system is mute or not
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//            isVolumeEnableLiveData.value = !audioManager.isStreamMute(AudioManager.STREAM_VOICE_CALL)
//        } else {
//            isVolumeEnableLiveData.value = (audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL) != 0)
//        }
    }
}