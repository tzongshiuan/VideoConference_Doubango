package com.gorilla.vc.view.ui.concall

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.gorilla.vc.R
import com.gorilla.vc.model.StreamingContent
import com.gorilla.vc.model.VcManager
import com.gorilla.vc.view.ui.concall.RtspList.RtspListFragment
import com.gorilla.vc.view.ui.concall.whiteboard.WhiteBoardFragment
import com.gorilla.vc.view.ui.setting.SettingActivity

class ConcallActivityHandler(private val context: ConcallActivity, private val vcManager: VcManager, private val viewModel: ConcallViewModel?) {

    val tag = ConcallActivityHandler::class.simpleName

    var sessionId = ""

    @Suppress("UNUSED_PARAMETER")
    fun onSettingBtnClick(view: View) {
        Log.d(tag, "onSettingBtnClick()")

        val intent = Intent(context, SettingActivity::class.java)
        intent.putExtra("id", sessionId)
        context.startActivity(intent)
    }

    /**
     * About controlling speaking video stream
     */
    fun onSpeakingBtnClick(view: View) {
        Log.d(tag, "onSpeakingBtnClick()")

        val me = viewModel?.getParticipantStatus()

        if (me?.streamingContent == StreamingContent.CAMERA) {
            // Show already toast
            Toast.makeText(view.context, view.resources.getString(R.string.already_camera_source), Toast.LENGTH_SHORT).show()
        } else {
            // Change to camera source
            me?.id.let {
                if (it != null) {
                    viewModel?.setVideoSourceFromCamera(it)
                }
            }
        }
    }

    /**
     * About controlling camera video stream
     */
    @Suppress("UNUSED_PARAMETER")
    fun onCameraBtnClick(view: View) {
        Log.d(tag, "onCameraBtnClick()")

        //val me = viewModel?.getParticipantStatus()
        //if (me?.streamingContent == StreamingContent.RTSP) {
        //    // Show already toast
        //}

        if (vcManager.userId == null) {
            return
        }

        // Show RTSP selection UI
        val fragmentTransaction = context.supportFragmentManager.beginTransaction()
        val rtspListFragment = RtspListFragment()
        fragmentTransaction.add(R.id.concallMainLayout, rtspListFragment).addToBackStack(null)
        fragmentTransaction.commit()

//        val info = RtspInfo()
//        info.channel = 1
//        info.name = "GorillaCoffee"
//        info.description = "內側門"
//        info.location = "7F"
//        info.url = "rtsp://192.168.7.66/LV/ch2"
//        info.cameraIp = "12.2.3.4"
//        info.onlineTime = "08:00-20:00"
//        info.repairStatus = false

        // Change to RTSP source
//        viewModel?.setVideoSourceFromRTSP(me?.id!!, info)
    }

    /**
     * Screen sharing
     */
    fun onScreenCastBtnClick(view: View) {
        Log.d(tag, "onScreenCastBtnClick()")
        this.viewModel?.setIsWhiteboardLiveData(false)

        val me = viewModel?.getParticipantStatus()
        if (me?.streamingContent == StreamingContent.DESKTOP) {
            // Show already toast
            viewModel?.setVideoSourceFromScreen()
            Toast.makeText(view.context, view.resources.getString(R.string.already_screen_source), Toast.LENGTH_SHORT).show()
        } else {
            val mediaProjectionManager = context.getSystemService(AppCompatActivity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
            context.startActivityForResult(captureIntent, ConcallActivity.REQUEST_CODE)
        }
    }

    /**
     * Whiteboard
     */
    @Suppress("UNUSED_PARAMETER")
    fun onWhiteboardBtnClick(view: View) {
        Log.d(tag, "onWhiteboardBtnClick()")
        this.viewModel?.setIsWhiteboardLiveData(true)

        val me = viewModel?.getParticipantStatus()
        if (me?.streamingContent == StreamingContent.DESKTOP) {
            viewModel?.setVideoSourceFromScreen()
            startWhiteboardFragment()
        } else {
            val mediaProjectionManager = context.getSystemService(AppCompatActivity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
            context.startActivityForResult(captureIntent, ConcallActivity.REQUEST_CODE)
        }
    }

    /**
     * Show whiteboard fragment
     */
    fun startWhiteboardFragment() {
        val fragmentTransaction = context.supportFragmentManager.beginTransaction()
        val whiteboardFragment = WhiteBoardFragment()
        fragmentTransaction.add(R.id.concallMainLayout, whiteboardFragment).addToBackStack(null)
        fragmentTransaction.commit()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onLeaveBtnClick(view: View) {
        Log.d(tag, "onLeaveBtnClick()")

        // avoid show leave dialog when fragment of sub-functions is showing
        val fm = context.supportFragmentManager
        if (fm.backStackEntryCount == 0) {
            this.viewModel?.leaveConcall(context)
        }
    }
}