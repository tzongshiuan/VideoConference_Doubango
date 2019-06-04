package com.gorilla.vc.view.ui.setting

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.database.ContentObserver
import android.databinding.DataBindingUtil
import android.media.AudioManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gorilla.vc.R
import com.gorilla.vc.databinding.ActivitySettingBinding
import com.gorilla.vc.di.Injectable
import com.gorilla.vc.model.PreferencesHelper
import com.gorilla.vc.model.VcManager
import com.gorilla.vc.utils.VcViewModelFactory
import org.doubango.ngn.sip.NgnAVSession
import org.doubango.ngn.utils.NgnStringUtils
import javax.inject.Inject
import android.content.Context.AUDIO_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.widget.ImageView
import android.widget.SeekBar


class SettingActivity : AppCompatActivity(), Injectable {
    val tag = SettingActivity::class.simpleName

    private var binding: ActivitySettingBinding ?= null

    @Inject
    lateinit var mPreferences: PreferencesHelper

    @Inject
    lateinit var vcManager: VcManager

    @Inject
    lateinit var factory: VcViewModelFactory

    private var sessionId: String = ""

    private var mSettingsContentObserver: SettingsContentObserver? = null

    /**
     * For settings
     */
    private var settingViewModel: SettingViewModel?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate()")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        setContentView(binding?.root)

        volumeControlStream = AudioManager.STREAM_VOICE_CALL

        initUI()

        mSettingsContentObserver = SettingsContentObserver(this, Handler())
        this.applicationContext.contentResolver.registerContentObserver(
                android.provider.Settings.System.CONTENT_URI, true,
                mSettingsContentObserver as ContentObserver)
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume()")

        settingViewModel?.resetVolumeStatus()
    }

    override fun onPause() {
        super.onPause()
        Log.d(tag, "onPause()")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy()")

        settingViewModel?.destroyAVSession()

        this.applicationContext.contentResolver.unregisterContentObserver(mSettingsContentObserver as ContentObserver)
    }

    private fun initUI() {
        initViews()
        initSettingViewModel()

        binding?.handler = SettingActivityHandler(this, vcManager, settingViewModel)
        binding?.handler?.setMicStatus()
        binding?.handler?.setCameraStatus()
    }

    /**
     * Initialize all views
     */
    private fun initViews() {
        // initial maximum value of volume SeekBar
        val audioManager: AudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
        Log.d(tag, "Audio maximum volume: $maxVolume")
        //binding?.volumeSeekBar?.max = maxVolume
        binding?.maxVolume = maxVolume.toString()

        // volume switch button
        //binding?.isVolumeChecked = mPreferences.isVolumeEnable

        // initial maximum value of mic SeekBar
        //binding?.micSeekBar?.progress = binding?.micSeekBar?.max!!
        //binding?.micSeekBar?.isEnabled = false

        // microphone switch button
        binding?.isMicChecked = mPreferences.isMicEnable

        // camera switch button
        binding?.isCameraChecked = mPreferences.isCameraEnable

        sessionId = intent.getStringExtra("id")
        if (NgnStringUtils.isNullOrEmpty(sessionId)) {
            Log.e(tag, "Invalid audio/video session")
            finish()
            return
        }
    }

    /**
     * Initialize log in ViewModel
     */
    private fun initSettingViewModel() {
        settingViewModel = ViewModelProviders.of(this, factory).get(SettingViewModel::class.java)

        // volume observer
        settingViewModel?.getVolumeValueObservable()?.observe(this, Observer<Int> {volumeValue ->
            Log.d(tag, "Volume value: $volumeValue")

            if (volumeValue != null) {
                //binding?.volumeSeekBar?.progress = volumeValue
                binding?.volNumber = volumeValue.toString()
                binding?.isVolumeChecked = mPreferences.isVolumeEnable//(volumeValue != 0)
                //mPreferences.isVolumeEnable = binding?.isVolumeChecked!!
            }
        })

        settingViewModel?.getIsVolumeEnableObservable()?.observe(this, Observer<Boolean> { isVolumeEnable ->
            Log.d(tag, "isVolumeEnable: $isVolumeEnable")

            if (isVolumeEnable != null) {
                binding?.volumeSwitch?.isChecked = isVolumeEnable
            }
        })

        // Camera preview
        settingViewModel?.mCameraPreview = binding?.cameraPreview
        settingViewModel?.mCameraImage = binding?.cameraView
        settingViewModel?.initAVSession(sessionId)

        settingViewModel?.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    inner class SettingsContentObserver(val context: Context, handler: Handler) : ContentObserver(handler) {

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            Log.d(tag, "Settings change detected")

            settingViewModel?.resetVolumeStatus()
        }
    }
}
