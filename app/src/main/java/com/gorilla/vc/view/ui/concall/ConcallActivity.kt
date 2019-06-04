package com.gorilla.vc.view.ui.concall

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.AudioManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.gorilla.vc.R
import com.gorilla.vc.databinding.ActivityConcallBinding
import com.gorilla.vc.di.Injectable
import com.gorilla.vc.model.PreferencesHelper
import com.gorilla.vc.model.StreamingContent
import com.gorilla.vc.model.VcManager
import com.gorilla.vc.utils.VcViewModelFactory
import com.gorilla.vc.view.ui.concall.information.InformationFragment
import com.gorilla.vc.view.ui.concall.member.MemberFragment
import com.gorilla.vc.view.ui.concall.message.MessageFragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import org.doubango.ngn.events.*
import org.doubango.ngn.media.NgnMediaType
import org.doubango.ngn.sip.NgnAVSession
import org.doubango.ngn.sip.NgnInviteSession
import org.doubango.ngn.utils.NgnStringUtils
import org.doubango.ngn.utils.NgnTimer
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.thread

class ConcallActivity : AppCompatActivity(), HasSupportFragmentInjector, Injectable {

    val tag = ConcallActivity::class.simpleName

    private var binding: ActivityConcallBinding?= null

    @Inject
    lateinit var preferences: PreferencesHelper

    @Inject
    lateinit var vcManager: VcManager

    @Inject
    lateinit var factory: VcViewModelFactory

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private var mViewLocalVideoPreview: FrameLayout ?= null
    private var mViewLocalSndVideoPreview: FrameLayout ?= null
    private var mViewRemoteVideoPreview: FrameLayout ?= null
    private var mViewRemoteSndVideoPreview: FrameLayout ?= null

    private var mAVTransfSession: NgnAVSession? = null

    private var mBroadCastRecv: BroadcastReceiver? = null

    private var mCountBlankPacket = 0
    private var mLastRotation: Int = -1
    //private var mLastOrientation: Int = -1
    //private var mListener: OrientationEventListener? = null

    private var sessionId: String = ""

    // about view size
    private var mainVideoWidthUnit = 200
    private var mainVideoHeightUnit = 200
    private var sndVideoWidthUnit = 176
    private var sndVideoHeightUnit = 144

    // about screen sharing
    private var mMediaProjectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null
    private var mMediaProjectionCallback = MediaProjectionCallback()
    private var mImageReader: ImageReader? = null
    companion object {
        const val REQUEST_CODE = 1000
    }

//    private var mTimerInCall: NgnTimer
//    private var mTimerSuicide: NgnTimer
    private var mTimerBlankPacket: NgnTimer ?= null
    private var mTimerQoS: NgnTimer ?= null

    private var preWhiteboardMode = false
    private var isWhiteBoardMode = false

    /**
     * For concall
     */
    private var concallViewModel: ConcallViewModel?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate()")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_concall)
        setContentView(binding?.root)

        initUI()

        /**
         * Concall start
         */
//        mTimerInCall = NgnTimer()
//        mTimerSuicide = NgnTimer()
        mTimerBlankPacket = NgnTimer()
        mTimerQoS = NgnTimer()

        if (!intent.hasExtra("id")) {
            Log.e(tag, "Null session id")
            return
        }

        sessionId = intent.getStringExtra("id")
        if (NgnStringUtils.isNullOrEmpty(sessionId)) {
            Log.e(tag, "Invalid audio/video session")
            finish()
            return
        }
        binding?.handler?.sessionId = sessionId

        /**
         * init AV session
         */
        concallViewModel?.initAVSession(sessionId, this)

        /**
         * init message session
         */
        concallViewModel?.initGlobalChatSession()

        mBroadCastRecv = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action

                when (action) {
                    NgnInviteEventArgs.ACTION_INVITE_EVENT -> handleSipEvent(intent)
                    NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT -> handleMediaEvent(intent)
                    NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT -> handleRegistrationEvent(intent)
                    NgnMessagingEventArgs.ACTION_MESSAGING_EVENT -> handleMessagingEvent(intent)
                    NgnMsrpEventArgs.ACTION_MSRP_EVENT -> handleMsrpEvent(intent)
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT)
        intentFilter.addAction(NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT)
        intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT)
        intentFilter.addAction(NgnMessagingEventArgs.ACTION_MESSAGING_EVENT)
        intentFilter.addAction(NgnMsrpEventArgs.ACTION_MSRP_EVENT)
        registerReceiver(mBroadCastRecv, intentFilter)

        // set volume control stream type
        volumeControlStream = AudioManager.STREAM_VOICE_CALL


        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(InformationFragment(), getString(R.string.tab_information))
        adapter.addFragment(MemberFragment(),getString(R.string.tab_member))
        adapter.addFragment(MessageFragment(), getString(R.string.tab_message))
        binding?.viewpager?.adapter = adapter
        binding?.tabs?.setupWithViewPager(binding?.viewpager)

        mMediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume()")

        if (concallViewModel?.mAVSession != null) {
            if (concallViewModel?.mAVSession?.state == NgnInviteSession.InviteState.INCALL) {
                mTimerQoS?.schedule(mTimerTaskQoS, 0, 3000)
            }
        }

        loadInCallVideoView()

        // use to reload view to avoid preview failed
        thread(start = true) {
            Thread.sleep(3000)
            this@ConcallActivity.runOnUiThread {
                loadInCallVideoView()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(tag, "onPause()")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "onStop()")
    }

    private fun preDestroy() {
        mTimerQoS?.cancel()
        cancelBlankPacket()

        concallViewModel?.destroyAVSession()
        concallViewModel?.destoryChatSession()

        if (mMediaProjection != null) {
            mMediaProjection?.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy()")
        if (mBroadCastRecv != null) {
            unregisterReceiver(mBroadCastRecv)
            mBroadCastRecv = null
        }

        mMediaProjection?.unregisterCallback(mMediaProjectionCallback)
    }

    override fun onBackPressed() {
        Log.d(tag, "onBackPressed()")

        val fm = supportFragmentManager
        if (fm.backStackEntryCount == 0) {
            concallViewModel?.leaveConcall(this)
        } else {
            fm.popBackStack()
        }
    }

    private fun initUI() {
        initViews()
        initConcallViewModel()

        binding?.handler = ConcallActivityHandler(this, vcManager, concallViewModel)

        if (VcManager.DEBUG_QOS) {
            binding?.textViewQoS?.visibility = View.VISIBLE
        }
    }

    /**
     * Initialize all of the views
     */
    private var isShowControlPanel = false
    private fun initViews() {
        mViewRemoteVideoPreview = binding?.remoteVideoFrameLayout
        mViewRemoteVideoPreview?.setOnClickListener {
            if (!isShowControlPanel) {
                isShowControlPanel = true

                binding?.controlPanel?.visibility = View.VISIBLE

                object: CountDownTimer(3000, 3000) {
                    override fun onFinish() {
                        binding?.controlPanel?.visibility = View.GONE
                        isShowControlPanel = false
                    }

                    override fun onTick(millisUntilFinished: Long) {}
                }.start()
            }
        }

        mViewRemoteSndVideoPreview = binding?.remoteSndVideoFrameLayout
        mViewLocalVideoPreview = binding?.localVideoFrameLayout
        mViewLocalSndVideoPreview = binding?.localSndVideoFrameLayout

        // get video unit
        mainVideoWidthUnit = dpToPx(resources.getInteger(R.integer.secondary_video_width))
        mainVideoHeightUnit = dpToPx(resources.getInteger(R.integer.secondary_video_height))
        sndVideoWidthUnit = dpToPx(resources.getInteger(R.integer.secondary_video_width))
        sndVideoHeightUnit = dpToPx(resources.getInteger(R.integer.secondary_video_height))

        binding?.textViewQoS?.setOnClickListener {
            // use to restore video view for test
            concallViewModel?.refreshVideoView()
        }
    }

    /**
     * Initialize Concall ViewModel
     */
    private fun initConcallViewModel() {
        concallViewModel = ViewModelProviders.of(this, factory).get(ConcallViewModel::class.java)

        // Update the list when the data have changed
        concallViewModel?.isChatEnableLiveData?.observe(this, Observer<Boolean> { isChatEnable ->
            Log.d(tag, "[Observe] isChangeEnable: $isChatEnable")
        })

        concallViewModel?.joinChatUserLiveData?.observe(this, Observer<String> { joinChatUser ->
            Log.d(tag, "[Observe] joinChatUser: $joinChatUser")
        })

        concallViewModel?.isWaitingLiveData?.observe(this, Observer<Boolean> { isWaiting ->
            if (isWaiting!!) {
                binding?.progress?.setVisibleWithAnimate(true)
            } else {
                binding?.progress?.setVisibleImmediately(false)
            }
        })

        concallViewModel?.newMessageUserLiveData?.observe(this, Observer {
            if (it != null && it.id != -1) {
                binding?.tabs?.getTabAt(2)?.select()
            }
        })

        concallViewModel?.isVideoActivateLiveData?.observe(this, Observer<Boolean> { isVideoActivate ->
            Log.d(tag, "isVideoActivate status: $isVideoActivate")

            if (isVideoActivate!!) {
                binding?.speakingBtn?.visibility = View.VISIBLE
                binding?.cameraBtn?.visibility = View.VISIBLE
                binding?.screenSharingBtn?.visibility = View.VISIBLE
                binding?.whiteboardBtn?.visibility = View.VISIBLE

                val me = concallViewModel?.getParticipantStatus()

                when (me?.streamingContent) {
                    StreamingContent.CAMERA -> {
                        binding?.speakingBtn?.setBackgroundResource(R.mipmap.btn_speaking_p)
                        binding?.cameraBtn?.setBackgroundResource(R.drawable.btn_camera)
                        binding?.screenSharingBtn?.setBackgroundResource(R.drawable.btn_screen_sharing)
                        binding?.whiteboardBtn?.setBackgroundResource(R.drawable.btn_whiteboard)
                    }

                    StreamingContent.RTSP -> {
                        binding?.speakingBtn?.setBackgroundResource(R.drawable.btn_speaking)
                        binding?.cameraBtn?.setBackgroundResource(R.mipmap.btn_camera_p)
                        binding?.screenSharingBtn?.setBackgroundResource(R.drawable.btn_screen_sharing)
                        binding?.whiteboardBtn?.setBackgroundResource(R.drawable.btn_whiteboard)
                    }

                    StreamingContent.DESKTOP -> {
                        preWhiteboardMode = isWhiteBoardMode

                        binding?.speakingBtn?.setBackgroundResource(R.drawable.btn_speaking)
                        binding?.cameraBtn?.setBackgroundResource(R.drawable.btn_camera)
                        if (isWhiteBoardMode) {
                            binding?.screenSharingBtn?.setBackgroundResource(R.drawable.btn_screen_sharing)
                            binding?.whiteboardBtn?.setBackgroundResource(R.mipmap.btn_whiteboard_p)
                        } else {
                            binding?.screenSharingBtn?.setBackgroundResource(R.mipmap.btn_display_p)
                            binding?.whiteboardBtn?.setBackgroundResource(R.drawable.btn_whiteboard)
                        }
                    }

                    else -> {}
                }
            } else {
                binding?.speakingBtn?.visibility = View.GONE
                binding?.cameraBtn?.visibility = View.GONE
                binding?.screenSharingBtn?.visibility = View.GONE
                binding?.whiteboardBtn?.visibility = View.GONE
            }
        })

        concallViewModel?.isWhiteboardLiveData?.observe(this, Observer { isWhiteboard ->
            Log.d(tag, "isWhiteboard: $isWhiteboard")

            this.isWhiteBoardMode = isWhiteboard!!
        })

        if (vcManager.userId != null) {
            concallViewModel?.getMyParticipantInfo(vcManager.userId!!)
        }

        concallViewModel?.isLeaveConcallManual = false
    }

    private fun applyCamRotation(rotation: Int) {
        if (concallViewModel?.mAVSession != null) {
            mLastRotation = rotation
            // libYUV
            concallViewModel?.mAVSession?.setRotation(rotation)

            // FFmpeg
            /*switch (rotation) {
				case 0:
				case 90:
					mAVSession.setRotation(rotation);
					mAVSession.setProducerFlipped(false);
					break;
				case 180:
					mAVSession.setRotation(0);
					mAVSession.setProducerFlipped(true);
					break;
				case 270:
					mAVSession.setRotation(90);
					mAVSession.setProducerFlipped(true);
					break;
				}*/
        }
    }

    @Synchronized
    private fun handleMediaEvent(intent: Intent) {
        val action = intent.action

        if (NgnMediaPluginEventArgs.ACTION_MEDIA_PLUGIN_EVENT == action) {
            val args = intent.getParcelableExtra<NgnMediaPluginEventArgs>(NgnMediaPluginEventArgs.EXTRA_EMBEDDED)
            if (args == null) {
                Log.e(tag, "Invalid event args")
                return
            }

            Log.d(tag, "handleMediaEvent, eventType: ${args.eventType}")
            when (args.eventType) {
                NgnMediaPluginEventTypes.STARTED_OK, //started or restarted (e.g. reINVITE)
                NgnMediaPluginEventTypes.VIDEO_INPUT_SIZE_CHANGED -> {
                    // Maybe not needed here
                    loadView()
                    concallViewModel?.mAVSession?.setIsAllowMediaPush(true)
                }

                NgnMediaPluginEventTypes.STOPPED_OK -> {
                    concallViewModel?.mAVSession?.setIsAllowMediaPush(false)
                }

                NgnMediaPluginEventTypes.PREPARED_OK,
                NgnMediaPluginEventTypes.PREPARED_NOK,
                NgnMediaPluginEventTypes.STOPPED_NOK,
                NgnMediaPluginEventTypes.PAUSED_OK,
                NgnMediaPluginEventTypes.PAUSED_NOK,
                NgnMediaPluginEventTypes.STARTED_NOK -> {}
                else -> {}
            }
        }
    }

    @Synchronized
    private fun handleSipEvent(intent: Intent) {
        if (concallViewModel?.mAVSession == null) {
            Log.e(tag, "Invalid session object")
            return
        }

        val state: NgnInviteSession.InviteState ?= concallViewModel?.mAVSession?.state
        Log.d(tag, "handleSipEvent, state: ${state.toString()}")
        val action = intent.action

        if (NgnInviteEventArgs.ACTION_INVITE_EVENT == action) {
            val args = intent.getParcelableExtra<NgnInviteEventArgs>(NgnInviteEventArgs.EXTRA_EMBEDDED)
            if (args == null) {
                Log.e(tag, "Invalid event args")
                return
            }

            Log.d(tag, "handleSipEvent, args.eventType: ${args.eventType}")

            /**
             * about chat session
             */

            if (args.mediaType == NgnMediaType.Chat) {
                // Answer the message call directly
                //val session = NgnMsrpSession.getSession(args.sessionId)

                when (state) {
                    NgnInviteSession.InviteState.NONE,
                    NgnInviteSession.InviteState.INPROGRESS,
                    NgnInviteSession.InviteState.REMOTE_RINGING,
                    NgnInviteSession.InviteState.EARLY_MEDIA,
                    NgnInviteSession.InviteState.TERMINATING,
                    NgnInviteSession.InviteState.TERMINATED,
                    NgnInviteSession.InviteState.INCOMING -> {}

                    NgnInviteSession.InviteState.INCALL -> {
                        when (args.eventType) {
                            NgnInviteEventTypes.INCOMING -> {
                                // Already accept [NgnSipService]
                                //Log.d(tag, "Accept MsrpSession automatically")
                                //session.accept()
                            }

                            NgnInviteEventTypes.REMOTE_DEVICE_INFO_CHANGED,
                            NgnInviteEventTypes.MEDIA_UPDATED,
                            NgnInviteEventTypes.LOCAL_TRANSFER_TRYING,
                            NgnInviteEventTypes.LOCAL_TRANSFER_FAILED,
                            NgnInviteEventTypes.LOCAL_TRANSFER_ACCEPTED,
                            NgnInviteEventTypes.LOCAL_TRANSFER_COMPLETED,
                            NgnInviteEventTypes.LOCAL_TRANSFER_NOTIFY,
                            NgnInviteEventTypes.REMOTE_TRANSFER_NOTIFY,
                            NgnInviteEventTypes.REMOTE_TRANSFER_REQUESTED,
                            NgnInviteEventTypes.REMOTE_TRANSFER_FAILED,
                            NgnInviteEventTypes.REMOTE_TRANSFER_COMPLETED -> {}
                            else -> {
                            }
                        }
                    }
                    else -> {}
                }
            }

            /**
             * about AV session
             */
            if (args.sessionId != concallViewModel?.mAVSession?.id) {
                if (args.eventType == NgnInviteEventTypes.REMOTE_TRANSFER_INPROGESS) {
                    // Native code created new session handle to be used to replace the current one (event = "tsip_i_ect_newcall").
                    mAVTransfSession = NgnAVSession.getSession(args.sessionId)
                }
                return
            }

            when (state) {
                NgnInviteSession.InviteState.NONE -> {
                }

                NgnInviteSession.InviteState.INCOMING,

                NgnInviteSession.InviteState.INPROGRESS, NgnInviteSession.InviteState.REMOTE_RINGING -> {
                    //loadTryingView()
                }

                NgnInviteSession.InviteState.EARLY_MEDIA, NgnInviteSession.InviteState.INCALL -> {
                    if (NgnInviteEventTypes.INCOMING == args.eventType
                        || NgnInviteEventTypes.RESOLUTION_UPDATED == args.eventType) {
                        val extraSession = intent.getStringExtra(NgnInviteEventArgs.EXTRA_SESSION)
                        Log.d(tag, "extraSession: $extraSession")

                        if (extraSession != null) {
                            concallViewModel?.parseExtraSession(this, extraSession)
                        }
                    }

                    // Send blank packets to open NAT pinhole
                    if (concallViewModel?.mAVSession != null) {
                        applyCamRotation(concallViewModel?.mAVSession?.compensCamRotation(true)!!)

                        if (VcManager.DEBUG_QOS) {
                            mTimerBlankPacket?.schedule(mTimerTaskBlankPacket, 0, 250)
                        }
                        mTimerQoS?.schedule(mTimerTaskQoS, 0, 3000)
                    }

                    when (args.eventType) {
                        NgnInviteEventTypes.REMOTE_DEVICE_INFO_CHANGED -> {
                            Log.d(tag, String.format("Remote device info changed: orientation: %s",
                                    concallViewModel?.mAVSession?.remoteDeviceInfo?.orientation))
                        }

                        NgnInviteEventTypes.INCOMING,
                        NgnInviteEventTypes.MEDIA_UPDATED -> {
                            loadInCallVideoView()
                        }
                        NgnInviteEventTypes.RESOLUTION_UPDATED -> {
                            //loadInCallVideoView()
                        }

                        NgnInviteEventTypes.LOCAL_TRANSFER_TRYING,
                        NgnInviteEventTypes.LOCAL_TRANSFER_FAILED,
                        NgnInviteEventTypes.LOCAL_TRANSFER_ACCEPTED,
                        NgnInviteEventTypes.LOCAL_TRANSFER_COMPLETED,
                        NgnInviteEventTypes.LOCAL_TRANSFER_NOTIFY,
                        NgnInviteEventTypes.REMOTE_TRANSFER_NOTIFY,
                        NgnInviteEventTypes.REMOTE_TRANSFER_REQUESTED -> {
                        }

                        NgnInviteEventTypes.REMOTE_TRANSFER_FAILED -> {
                            mAVTransfSession = null
                        }
                        NgnInviteEventTypes.REMOTE_TRANSFER_COMPLETED -> {
                            if (mAVTransfSession != null) {
                                mAVTransfSession?.context = concallViewModel?.mAVSession?.context
                                concallViewModel?.mAVSession = mAVTransfSession
                                mAVTransfSession = null
                                loadInCallVideoView()  //loadInCallView(true)
                            }
                        }
                        else -> {
                        }
                    }
                }

                NgnInviteSession.InviteState.TERMINATING -> {}
                NgnInviteSession.InviteState.TERMINATED -> {
                    if (VcManager.DEBUG_QOS) {
                        mTimerBlankPacket?.cancel()
                    }

                    // Notify user that this concall had been terminated and finish acitivty.
                    if (!concallViewModel?.isLeaveConcallManual!!) {
                        try {
                            this@ConcallActivity.runOnUiThread {
                                concallViewModel?.showTerminateDialog(this)
                            }
                        } catch (e: WindowManager.BadTokenException) {
                            e.printStackTrace()
                        }
                    }
                }
                else -> {
                }
            }
        }
    }

    @Synchronized
    private fun handleRegistrationEvent(intent: Intent) {
        val activity = this
        val args = intent.getParcelableExtra<NgnRegistrationEventArgs>(NgnEventArgs.EXTRA_EMBEDDED)
        if (args == null) {
            Log.d(tag, "Invalid event args")
            return
        }

        Log.d(tag, "NgnRegistrationEventTypes: ${args.eventType}")
        when (args.eventType) {
            NgnRegistrationEventTypes.REGISTRATION_OK -> {
            }

            NgnRegistrationEventTypes.UNREGISTRATION_OK -> {
                // dismiss loading dialog
                binding?.progress?.setVisibleImmediately(false)

                /**
                 * lifecycle is:
                 * ConcallActivity: onBackPressed()
                 * ConcallActivity: handleSipEvent, state: TERMINATING
                 * ConcallActivity: NgnRegistrationEventTypes: UNREGISTRATION_INPROGRESS
                 * ConcallActivity: handleMediaEvent, eventType: STOPPED_OK
                 * ConcallActivity: handleMediaEvent, eventType: STOPPED_OK
                 * ConcallActivity: handleSipEvent, state: TERMINATED
                 * ConcallActivity: NgnRegistrationEventTypes: UNREGISTRATION_OK
                 */
                preDestroy()
                activity.finish()
            }

            NgnRegistrationEventTypes.REGISTRATION_NOK,
            NgnRegistrationEventTypes.REGISTRATION_INPROGRESS,
            NgnRegistrationEventTypes.UNREGISTRATION_INPROGRESS -> {}

            NgnRegistrationEventTypes.UNREGISTRATION_NOK -> {
                binding?.progress?.setVisibleImmediately(false)
                concallViewModel?.isLeaveConcallManual = false
            }

            else -> { Log.d(tag, "Received unknown NgnRegistrationEventTypes") }
        }
    }

    @Synchronized
    private fun handleMessagingEvent(intent: Intent) {
        val args = intent.getParcelableExtra<NgnMessagingEventArgs>(NgnMessagingEventArgs.EXTRA_EMBEDDED)
        if (args == null) {
            Log.d(tag, "Invalid messaging event args")
            return
        }

        Log.d(tag, "NgnMessagingEventTypes: ${args.eventType}")

        when (args.eventType) {
            NgnMessagingEventTypes.INCOMING -> {
                //concallViewModel?.receiveMessage(String(args.payload))
            }

            NgnMessagingEventTypes.OUTGOING,
            NgnMessagingEventTypes.SUCCESS,
            NgnMessagingEventTypes.FAILURE,
            NgnMessagingEventTypes.Inform -> {}

            else -> {}
        }
    }

    @Synchronized
    private fun handleMsrpEvent(intent: Intent) {
        val args = intent.getParcelableExtra<NgnMsrpEventArgs>(NgnMsrpEventArgs.EXTRA_EMBEDDED)
        if (args == null) {
            Log.d(tag, "Invalid MSRP event args")
            return
        }

        Log.d(tag, "NgnMsrpEventTypes: ${args.eventType}")

        when (args.eventType) {
            NgnMsrpEventTypes.CONNECTED,
            NgnMsrpEventTypes.SUCCESS_2XX,
            NgnMsrpEventTypes.SUCCESS_REPORT,
            NgnMsrpEventTypes.ERROR,
            NgnMsrpEventTypes.DISCONNECTED -> {}

            NgnMsrpEventTypes.DATA -> {
                val message = String(intent.getByteArrayExtra(NgnMsrpEventArgs.EXTRA_DATA))
                val remoteUri = intent.getStringExtra(NgnInviteEventArgs.EXTRA_REMOTE_URI)
                Log.d(tag, "MSRP data: $message, remoteUri: $remoteUri, sessionId: ${args.sessionId}")
                //concallViewModel?.receivePrivateMessage(message, remoteUri, args.sessionId)
            }

            else -> {}
        }
    }

    private fun loadView() {
        when (concallViewModel?.mAVSession?.state) {
            NgnInviteSession.InviteState.INCOMING,
            NgnInviteSession.InviteState.INPROGRESS,
            NgnInviteSession.InviteState.REMOTE_RINGING -> {
                //loadTryingView()
            }

            NgnInviteSession.InviteState.INCALL,
            NgnInviteSession.InviteState.EARLY_MEDIA -> {
                //loadInCallVideoView()
            }

            NgnInviteSession.InviteState.NONE,
            NgnInviteSession.InviteState.TERMINATING,
            NgnInviteSession.InviteState.TERMINATED -> {
                //loadTermView()
            }
            else -> {
                //loadTermView()
            }
        }
    }

    @Synchronized
    private fun loadInCallVideoView() {
        Log.d(tag, "loadInCallVideoView()")

        // Video Consumer
        loadVideoPreview()

        // Video Producer
        startStopVideo()
        concallViewModel?.startStopProjection()
    }

    /**
     * Decode main and secondary video streaming data.
     * note: Need to change width with lastUpdateStatus of [concallViewModel] dynamically.
     */
    @Synchronized
    private fun loadVideoPreview() {
        if (concallViewModel?.lastUpdateStatus == null) {
            Log.d(tag, "Haven't received UpdateStatus yet")

            if (VcManager.DEBUG_CONCALL_BY_VIRTUAL_SESSION) {
                concallViewModel?.addPresenterSingle(vcManager.userId!!.toInt())
            }

            return
        }

        mViewRemoteVideoPreview?.removeAllViews()
        val remotePreview = concallViewModel?.mAVSession?.startVideoConsumerPreview()
        if (remotePreview != null) {
            val viewParent = remotePreview.parent
            if (viewParent != null && viewParent is ViewGroup) {
                viewParent.removeView(remotePreview)
            }
            mViewRemoteVideoPreview?.addView(remotePreview)
        }

        mViewRemoteSndVideoPreview?.removeAllViews()
        val remoteSndPreview = concallViewModel?.mAVSession?.startSndVideoConsumerPreview()
        if (remoteSndPreview != null) {
            val viewParent2 = remoteSndPreview.parent
            if (viewParent2 != null && viewParent2 is ViewGroup) {
                viewParent2.removeView(remoteSndPreview)
            }

            // reset size
            remoteSndPreview.layoutParams = ViewGroup.LayoutParams(
                    concallViewModel?.lastUpdateStatus?.participants?.size!! * sndVideoWidthUnit, sndVideoHeightUnit)
            mViewRemoteSndVideoPreview?.addView(remoteSndPreview)
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return Math.round(dp.toFloat() * density)
    }

    private fun cancelBlankPacket() {
        if (mTimerBlankPacket != null) {
            mTimerBlankPacket?.cancel()
            mCountBlankPacket = 0
        }
    }

    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            mMediaProjection = null
            mImageReader = null
//            stopScreenSharing()
        }
    }

    private fun createVirtualDisplay(): VirtualDisplay? {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val mScreenDensity = metrics.densityDpi

//        val display = windowManager.defaultDisplay
//        val s = Point()
//        display.getSize(s)
//        val size = Size(s.x, s.y)
        val size = concallViewModel?.mAVSession?.frameSize

        return if (size != null) {
            mImageReader = ImageReader.newInstance(size.height, size.width, PixelFormat.RGBA_8888, 1)
            concallViewModel?.setImageReader(mImageReader, mMediaProjection)

            if (isWhiteBoardMode) {
                binding?.handler?.startWhiteboardFragment()
            }

            mMediaProjection!!.createVirtualDisplay(tag!!,
                    size.height, size.width, mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader?.surface, null, null)
        } else {
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != REQUEST_CODE) {
            Log.e(tag, "Unknown request code: $requestCode")
            return
        }

        if (resultCode != RESULT_OK) {
            Log.d(tag, "Screen Cast Permission Denied")
            isWhiteBoardMode = preWhiteboardMode
            return
        }

        if (intent != null) {
            mMediaProjection = mMediaProjectionManager?.getMediaProjection(resultCode, intent)
            mMediaProjection.let {
                it?.registerCallback(mMediaProjectionCallback, null)
            }

            createVirtualDisplay()
        }
    }

    private fun startStopVideo() {
        Log.d(tag, "startStopVideo, isCameraEnable: ${preferences.isCameraEnable}")

        concallViewModel?.mAVSession?.isSendingVideo = preferences.isCameraEnable

        // camera view
//            mViewLocalVideoPreview?.removeAllViews()
//
//            val localPreview = concallViewModel?.mAVSession?.startVideoProducerPreview()
//            if (localPreview != null) {
//                val viewParent = localPreview.parent
//                if (viewParent != null && viewParent is ViewGroup) {
//                    viewParent.removeView(localPreview)
//                }
//                if (localPreview is SurfaceView) {
//                    localPreview.setZOrderOnTop(false)
//                }
//
//                if (!preferences.isCameraEnable) {
//                    localPreview.visibility = View.GONE
//                } else {
//                    localPreview.visibility = View.VISIBLE
//                }
//
//                mViewLocalVideoPreview?.addView(localPreview)
//                mViewLocalVideoPreview?.bringChildToFront(localPreview)
//                mViewLocalVideoPreview?.visibility = View.VISIBLE
//            }
        if (preferences.isCameraEnable) {
            val display = windowManager.defaultDisplay
            val s = Point()
            display.getSize(s)
            concallViewModel?.mAVSession?.startCamera2(this)
        } else {
            concallViewModel?.mAVSession?.stopCamera2()
        }
    }

    private val mTimerTaskBlankPacket = object : TimerTask() {
        override fun run() {
            Log.d(tag, "Resending Blank Packet " + mCountBlankPacket.toString())
            if (mCountBlankPacket < 3) {
                if (concallViewModel?.mAVSession != null) {
                    concallViewModel?.mAVSession?.pushBlankPacket()
                }
                mCountBlankPacket++
            } else {
                cancel()
                mCountBlankPacket = 0
            }
        }
    }

    private val mTimerTaskQoS = object : TimerTask() {
        override fun run() {
            if (concallViewModel?.mAVSession != null && binding?.textViewQoS != null) {
                synchronized(binding?.textViewQoS!!) {
                    val qos = concallViewModel?.mAVSession?.qoSVideo
                    if (qos != null) {
                        this@ConcallActivity.runOnUiThread {
                                try {
                                    val qosInfo =
                                            "Quality: 		" + (qos.qavg * 100).toInt() + "%\n" +
                                            "Receiving:		" + qos.bandwidthDownKbps + "Kbps\n" +
                                            "Sending:		" + qos.bandwidthUpKbps + "Kbps\n" +
                                            "Size in:	    " + qos.videoInWidth + "x" + qos.videoInHeight + "\n" +
                                            "Size out:		" + qos.videoOutWidth + "x" + qos.videoOutHeight + "\n" +
                                            "Fps in:        " + qos.videoInAvgFps + "\n" +
                                            "Encode time:   " + qos.videoEncAvgTime + "ms / frame\n" +
                                            "Decode time:   " + qos.videoDecAvgTime + "ms / frame\n" +
                                            "Svn Revision:  " + this@ConcallActivity.getString(R.string.svn_revision)
                                    binding?.textViewQoS?.text = qosInfo
                                } catch (e: Exception) {
                            }
                        }
                    }
                }
            }
        }
    }

    class ViewPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()
        override fun getItem(p0: Int): Fragment = mFragmentList[p0]

        override fun getCount(): Int = mFragmentList.size

        override fun getPageTitle(position: Int): CharSequence? = mFragmentTitleList[position]

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }
    }


    override fun supportFragmentInjector() = dispatchingAndroidInjector
}
