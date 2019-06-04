package com.gorilla.vc.view.ui.concall

import android.app.Activity
import android.app.AlertDialog
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.util.Log
import android.util.Size
import com.google.gson.Gson
import com.gorilla.vc.R
import com.gorilla.vc.di.Injectable
import com.gorilla.vc.model.*
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import org.doubango.ngn.media.NgnMediaType
import org.doubango.ngn.sip.NgnAVSession
import org.doubango.ngn.utils.NgnContentType
import org.doubango.ngn.utils.NgnStringUtils
import org.doubango.ngn.utils.NgnUriUtils
import org.doubango.tinyWRAP.MediaSessionMgr
import org.doubango.tinyWRAP.tmedia_pref_video_size_t
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConcallViewModel @Inject constructor(private val concallRepository: ConcallRepository,
                                           private val vcManager: VcManager) : Injectable, ViewModel() {

    private val tag = ConcallViewModel::class.simpleName
    private val compositeDisposable = CompositeDisposable()

    companion object {
        private const val isDynamicAdjustResolution = false

        private const val LISTENER_INFO = "ListenesrInfo:"
        private const val UPDATE_STATUS = "UpdateStatus:"
        private const val RESOLUTION_UPDATE = "104 recv"

        private const val CHAT_JOIN_TO_ROOM = "JoinToRoom"
        private const val CHAT_LEAVE_FROM_ROOM = "LeaveFromRoom"
        private const val CHAT_SEND_MESSAGE_TO_ROOM = "SendMessageToRoom"
        private const val CHAT_SEND_MESSAGE_TO_USER = "SendMessageToUser"
        private const val CHAT_RECEIVE_MESSAGE = "ReceiveMessage"
        private const val CHAT_RECEIVE_PRIVATE_MESSAGE = "ReceivePrivateMessage"

        private const val CHAT_USER_JOIN = "has joined the room"
        private const val CHAT_USER_LEAVE = "has left the room"
        //private const val CHAT_CREATE_ROOM = "room was created"
        //private const val CHAT_DESTROY_ROOM = "The room has been destroyed"

        // Set ratio = 1.2f to instead 1.775(default setting) to view the better resolution
        private const val FRAME_RATIO = 1.2f
    }

    val isChatEnableLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val joinChatUserLiveData: MutableLiveData<String> = MutableLiveData()

    val messageListLiveData: MutableLiveData<ArrayList<ChatMessage>> = MutableLiveData()
    private val messageListList: ArrayList<MessageList> = ArrayList()

    var isExpandControlView: MutableLiveData<Boolean> = MutableLiveData()
    private var msgUserList: ArrayList<PrivateMessageUser> = ArrayList()
    var chatUserIndexLiveData: MutableLiveData<Int> = MutableLiveData()
    var newMessageUserLiveData: MutableLiveData<PrivateMessageUser> = MutableLiveData()
    var messageUserListLiveData: MutableLiveData<ArrayList<PrivateMessageUser>> = MutableLiveData()

    val isWaitingLiveData: MutableLiveData<Boolean> = MutableLiveData()

    private val mSipService = vcManager.mEngine.sipService

    val lastUpdateStatusLiveData: MutableLiveData<UpdateStatus> = MutableLiveData()
    var lastUpdateStatus: UpdateStatus?= null

    val lastChatStatusLiveDate: MutableLiveData<Int> = MutableLiveData()
    private var lastChatStatus: Int ?= ChatStatus.DEFAULT

    var mAVSession: NgnAVSession? = null

    private var isInitUpdateStatus = false

    // use to check whether session is terminated by user
    @Volatile var isLeaveConcallManual: Boolean = false

    // about control video source
    val isVideoActivateLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val rtspListLiveData: MutableLiveData<ArrayList<RtspInfo>> = MutableLiveData()

    val rtspIndexLiveData: MutableLiveData<Int> = MutableLiveData()

    val isWhiteboardLiveData: MutableLiveData<Boolean> = MutableLiveData()

    private var mMediaProjection: MediaProjection? = null

    private var mHubConnection: HubConnection? = null

    private var terminalDialog: AlertDialog? = null

    fun setVideoActivateStatus(isActivate: Boolean) {
        Log.d(tag, "setVideoActivateStatus: $isActivate")

        isVideoActivateLiveData.value = isActivate
        if (!isActivate) {
            mAVSession?.stopSndVideoProducerProjection()
        }
    }

    fun getParticipantStatus(): ParticipantStatus? {
        if (lastUpdateStatus == null) {
            return null
        }

        return lastUpdateStatus?.participants?.filter {
            participantStatus -> participantStatus.id == vcManager.userId?.toInt()
        }!!.single()
    }

    fun leaveConcall(context: Context, isForce: Boolean = false) {
        if (lastUpdateStatus == null || isForce) {
            isLeaveConcallManual = true
            vcManager.unRegister()
            return
        }

        // Host must transit host permission to another participant before leave concall
        if (vcManager.userId?.toInt() == lastUpdateStatus?.host
                && lastUpdateStatus?.participants?.size!! > 1) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.resources.getString(R.string.alert_exit_title))
            builder.setMessage(context.resources.getString(R.string.hint_host_leave))
            builder.setCancelable(true)

            builder.setPositiveButton(context.resources.getString(R.string.ok)) { _, _ ->
                // Nothing need to do, so I just refresh the video view here
            }

            builder.create().show()
            return
        }

        showLeaveConcallDialog(context)
    }

    private fun showLeaveConcallDialog(context: Context) {
        // Show hint dialog to check whether user want to leave conall
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.alert_exit_title))
        builder.setMessage(context.resources.getString(R.string.alert_exit_message))
        builder.setCancelable(true)

        builder.setNegativeButton(context.resources.getString(R.string.exit)) { _, _ ->
            //show loading dialog here
            isWaitingLiveData.value = true
            isLeaveConcallManual = true
            vcManager.unRegister()
        }

        builder.setPositiveButton(context.resources.getString(R.string.cont)) { _, _ ->
            // Nothing need to do, so I just refresh the video view here
            refreshVideoView()
        }

        builder.create().show()
    }

    fun showTerminateDialog(activity: Activity) {
        Log.d(tag, "showTerminateDialog()")

        if (terminalDialog == null) {
            // Unknown reason to shut down concall page
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(activity.resources.getString(R.string.alert_exit_title))
            builder.setMessage(activity.resources.getString(R.string.hint_terminate))
            builder.setCancelable(false)

            builder.setPositiveButton(activity.resources.getString(R.string.ok)) { _, _ ->
                // Nothing need to do, so I just refresh the video view here
                leaveConcall(activity, true)
            }
            terminalDialog = builder.create()
        }

        terminalDialog?.show()
    }

    fun initAVSession(sessionId: String, context: Context) {
        mAVSession = NgnAVSession.getSession(NgnStringUtils.parseLong(sessionId, -1))
        if (mAVSession == null) {
            Log.d(tag, String.format("Cannot find audio/video session with id=%s", sessionId))
            return
        }

        mAVSession?.incRef()
        mAVSession?.context = context

        val preferences = vcManager.mPreferences
        // volume
        mAVSession?.setSpeakerphoneOn(preferences.isVolumeEnable)
        // mic
        mAVSession?.isOnMute = !preferences.isMicEnable
        // camera
        mAVSession?.isSendingVideo = preferences.isCameraEnable

        //mAVSession?.enableConsumerRecord()
    }

    fun destroyAVSession(keep: Boolean = false) {
        if (mAVSession != null) {
            //mAVSession?.disableConsumerRecord()
            mAVSession?.stopCamera2()
            mAVSession?.stopSndVideoProducerProjection()
            mAVSession?.context = null
            mAVSession?.decRef()
            mAVSession = null
        }

        if (!keep) {
            isInitUpdateStatus = false
            lastUpdateStatus = null
        }
    }

    fun initGlobalChatSession() {
        messageListList.add(MessageList(-1))
        messageListLiveData.value = messageListList[0].messages
        chatUserIndexLiveData.value = 0
    }

    fun reloadMsgUserList() {
        messageUserListLiveData.value = msgUserList
    }

    private fun initSend2EveryoneItem(context: Context) {
        Log.d(tag, "initSend2EveryoneItem()")

        if (msgUserList.size != 0 && msgUserList[0].id == -1) {
            return
        }

        // Add default user as sending message to everyone
        val global = PrivateMessageUser(-1, context.getString(R.string.send_to_everyone))
        msgUserList.add(global)
        messageUserListLiveData.value = msgUserList

        isExpandControlView.value = false
    }

    fun addMessageUserItem(user: PrivateMessageUser, isKeepIndex: Boolean = false) {
        Log.d(tag, "addMessageUserItem()")

        if (msgUserList.contains(user)) {
            Log.d(tag, "Duplicate message user !!")
        } else {
            // default is online
            user.isOnline = true

            msgUserList.add(user)
            messageListList.add(MessageList(user.id))

            if (!isKeepIndex) {
                changeSelectUserIndex(msgUserList.indexOf(user))
            }
        }

        val disposable = Observable.just(user).subscribeOn(AndroidSchedulers.mainThread()).subscribe {
            newMessageUserLiveData.value = it
            isExpandControlView.value = true
        }
        compositeDisposable.add(disposable)
    }

    fun changeSelectUserIndex(index: Int) {
        Log.d(tag, "changeSelectUserIndex: $index")

        msgUserList[index].isHaveUnreadMessage = false
        chatUserIndexLiveData.value = index
        refreshMessageListWithIndex()
    }

    fun toggleControlView() {
        isExpandControlView.value = !isExpandControlView.value!!
    }

    fun destoryChatSession() {
        leaveMessageSession()

        lastChatStatus = ChatStatus.DEFAULT
        isExpandControlView.value = false
        msgUserList.clear()
        messageListList.clear()
    }

    private fun getSendingDateTimeStr(): String {
        val date = Calendar.getInstance().time
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        return sdf.format(date)
    }

    /**
     * Join message session
     */
    @Synchronized
    private fun joinMessageSession(context: Context) {
        if (mHubConnection != null) {
            return
        }

//        val chatUrl = "http://192.168.11.70:53353/Chatroom"
        val chatUrl = "http://${vcManager.sipInfo?.proxyIp}:53353/Chatroom"
        Log.d(tag, "joinMessageSession(), url: $chatUrl")
        mHubConnection = HubConnectionBuilder.create(chatUrl).build()

        mHubConnection?.on(CHAT_RECEIVE_MESSAGE, { dateTime, userId, message ->
            receiveGlobalMessage(dateTime, userId, message)
        }, String::class.java, String::class.java, String::class.java)

        mHubConnection?.on(CHAT_RECEIVE_PRIVATE_MESSAGE, { dateTime, userId, message ->
            receivePrivateMessage(dateTime, userId, message)
        }, String::class.java, String::class.java, String::class.java)

        // This is a blocking call
        mHubConnection?.start()?.blockingAwait()

        mHubConnection?.send(CHAT_JOIN_TO_ROOM, vcManager.sessionInfo?.id, getSendingDateTimeStr(), vcManager.userId)

        isChatEnableLiveData.value = true
        initSend2EveryoneItem(context)
    }

    /**
     * Leave message session
     */
    @Synchronized
    private fun leaveMessageSession() {
        if (mHubConnection == null) {
            return
        }

        isChatEnableLiveData.value = false
        mHubConnection?.send(CHAT_LEAVE_FROM_ROOM, vcManager.sessionInfo?.id, getSendingDateTimeStr(), vcManager.userId)
        mHubConnection?.stop()
        mHubConnection = null
        newMessageUserLiveData.value = null
    }

    private fun checkConnectState(): Boolean {
        if (!mSipService.isRegistered) {
            Log.e(tag, "Not registered")
            return false
        }

        if (mHubConnection?.connectionState != HubConnectionState.CONNECTED) {
            mHubConnection?.start()?.blockingAwait()
        }

        return true
    }

    private fun getMessageList(id: Int): MessageList? {
        var list: MessageList ?= null

        messageListList.forEach loop@{ messageList ->
            if (id == messageList.id) {
                list = messageList
                return@loop
            }
        }

        return list
    }

    private fun refreshMessageListWithIndex() {
        val messageList = messageListList[chatUserIndexLiveData.value!!]
        messageListLiveData.value = messageList.messages
    }

    /**
     * Send message to everybody in the chat room
     */
    private fun sendGlobalMessage(message: String) {
        if (!checkConnectState()) {
            Log.d(tag, "[Send Global Message] failed")
            return
        }

        Log.d(tag, "[Send Global Message] $message")
        mHubConnection?.send(CHAT_SEND_MESSAGE_TO_ROOM, vcManager.sessionInfo?.id, getSendingDateTimeStr(), vcManager.userId, message)
    }

    /**
     * Send private message to specific person in the chat room
     */
    private fun sendPrivateMessage(message: String) {
        if (!checkConnectState()) {
            Log.d(tag, "[Send Private Message] failed")
            return
        }

        Log.d(tag, "[Send Private Message] $message")
        val toUserId = msgUserList[chatUserIndexLiveData.value!!].id
        mHubConnection?.send(CHAT_SEND_MESSAGE_TO_USER, toUserId, getSendingDateTimeStr(), vcManager.userId, message)

        val chatMessage = ChatMessage(getSendingDateTimeStr(), vcManager.userId!!, message, false)
        chatMessage.id = vcManager.userId
        chatMessage.name = vcManager.getNameFromId(vcManager.userId)

        val disposable = Observable.just(chatMessage).subscribeOn(AndroidSchedulers.mainThread()).subscribe {
            // add into corresponding message list
            val messageList = getMessageList(toUserId!!)
            messageList?.messages?.add(chatMessage)

            refreshMessageListWithIndex()
            reloadMsgUserList()
        }
        compositeDisposable.add(disposable)
    }

    fun sendChatMessage(message: String) {
        if (chatUserIndexLiveData.value == 0) {
            sendGlobalMessage(message)
        } else {
            sendPrivateMessage(message)
        }
    }

    private fun isDuplicateChatMessage(chatMessage: ChatMessage): Boolean {
        return messageListLiveData.value?.contains(chatMessage)!!
    }

    private fun receiveGlobalMessage(dateTime: String, userId: String, message: String) {
        Log.d(tag, "receive global chat: dateTime: $dateTime, user: $userId, message: $message")

        if (message.contains(CHAT_USER_JOIN)) {
            // Ex: 999 has joined the room 1404"
            joinChatUserLiveData.postValue(vcManager.getNameFromId(userId))
            return
        }

        if (message.contains(CHAT_USER_LEAVE)) {
            // Ex: The user [11902] has left the room
            return
        }

        val chatMessage = ChatMessage(dateTime, userId, message)
        chatMessage.name = vcManager.getNameFromId(chatMessage.id)

        if (isDuplicateChatMessage(chatMessage)) {
            Log.d(tag, "Duplicate Message !!")
            return
        }

        Log.d(tag,"[Receive Global Message] id: ${chatMessage.id}, name: ${chatMessage.name}, time: ${chatMessage.time}, message: ${chatMessage.message}")
        val disposable = Observable.just(chatMessage).subscribeOn(AndroidSchedulers.mainThread()).subscribe {
            val messageList = getMessageList(-1)
            messageList?.messages?.add(it)

            if (chatUserIndexLiveData.value != 0) {
                msgUserList[0].isHaveUnreadMessage = true
            }
            refreshMessageListWithIndex()
            reloadMsgUserList()
        }
        compositeDisposable.add(disposable)
    }

    /**
     * Handle private message
     */
    private fun receivePrivateMessage(dateTime: String, userId: String, message: String) {
        Log.d(tag, "receive private chat: dateTime: $dateTime, user: $userId, message: $message")

        val name = vcManager.getNameFromId(userId)
        val messageUser = PrivateMessageUser(userId.toInt(), name)

        // set unread status
        if (msgUserList.contains(messageUser)) {
            val index = msgUserList.indexOf(messageUser)

            if (index != chatUserIndexLiveData.value) {
                msgUserList[index].isHaveUnreadMessage = true
            }
        } else {
            messageUser.isHaveUnreadMessage = true
            addMessageUserItem(messageUser, true)
        }

        val chatMessage = ChatMessage(dateTime, userId, message, false)
        chatMessage.id = userId
        chatMessage.name = name

        Log.d(tag,"[Receive Private Message] id: ${chatMessage.id}, name: ${chatMessage.name}, time: ${chatMessage.time}, message: ${chatMessage.message}")
        val disposable = Observable.just(chatMessage).subscribeOn(AndroidSchedulers.mainThread()).subscribe {
            // add into corresponding message list
            val messageList = getMessageList(userId.toInt())
            messageList?.messages?.add(chatMessage)

            refreshMessageListWithIndex()
            reloadMsgUserList()
        }
        compositeDisposable.add(disposable)
    }

    fun refreshVideoView() {
        val jStatus = lastUpdateStatus?.getHostChangeViewObject(lastUpdateStatus!!, 1, lastUpdateStatus?.host!!, -1)
        sendUpdateStatusInfo(jStatus)
    }

    fun setPresenterNone() {
        // Stop screen sharing mode to camera mode
        mMediaProjection?.stop()
        if (isVideoActivateLiveData.value == true) {
            lastUpdateStatus?.participants?.filter {
                participantStatus -> participantStatus.id == vcManager.userId?.toInt() }!!.single()
                    .streamingContent = StreamingContent.CAMERA
        }

        val jStatus = lastUpdateStatus?.getHostChangeViewObject(lastUpdateStatus!!, PresentMode.NONE_HOST, -1, -1)
        sendUpdateStatusInfo(jStatus)
    }

    fun addPresenterSingle(id: Int) {
        val jStatus = lastUpdateStatus?.getHostChangeViewObject(lastUpdateStatus!!, PresentMode.SINGLE_HOST, id, -1)
        sendUpdateStatusInfo(jStatus)
    }

    fun addPresenterDual(id: Int, otherId: Int, isLeft: Boolean) {
        val jStatus = if (isLeft) {
            lastUpdateStatus?.getHostChangeViewObject(lastUpdateStatus!!, PresentMode.DUAL_HOST, id, otherId)
        } else {
            lastUpdateStatus?.getHostChangeViewObject(lastUpdateStatus!!, PresentMode.DUAL_HOST, otherId, id)
        }

        sendUpdateStatusInfo(jStatus)
    }

    fun changeHost(id: Int) {
        val jStatus = lastUpdateStatus?.getChangeHostObject(lastUpdateStatus!!, id)
        sendUpdateStatusInfo(jStatus)
    }

    fun setVideoSourceFromCamera(id: Int) {
//        mAVSession?.setIsScreenSource(false)
        mMediaProjection?.stop()

        val jStatus = lastUpdateStatus?.getControlStreamObject(lastUpdateStatus!!, id, StreamingContent.CAMERA, null)
        sendUpdateStatusInfo(jStatus)
    }

    private fun setVideoSourceFromRTSP(id: Int, info: RtspInfo) {
//        mAVSession?.setIsScreenSource(false)
        mMediaProjection?.stop()

        val jStatus = lastUpdateStatus?.getControlStreamObject(lastUpdateStatus!!, id, StreamingContent.RTSP, info.url)
        sendUpdateStatusInfo(jStatus)
    }

    fun setVideoSourceFromScreen() {
        val jStatus = lastUpdateStatus?.getControlStreamObject(
                lastUpdateStatus!!, vcManager.userId?.toInt()!!, StreamingContent.DESKTOP, null)
        sendUpdateStatusInfo(jStatus)
    }

    private fun setVideoSourceFromScreen(reader: ImageReader?) {
        // Send updateStatus to server to notify video source have been changed to screen of android device
        setVideoSourceFromScreen()

        reader?.setOnImageAvailableListener({
            val image = it?.acquireLatestImage()
            val width = image?.width
            val height = image?.height
            val planes = image?.planes
            val buffer = planes!![0].buffer

            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * width!!
            val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height!!, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(buffer)
            image.close()

            //Log.d(tag, "On Screen Image Available, Width: ${bitmap.width}, Height: ${bitmap.height}")

            // Rotate image
//            val mtx = Matrix()
//            mtx.postRotate(90.0f)
//            val rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, mtx, true)
//            Log.d(tag, "rotateBitmap, Width: ${rotateBitmap.width}, Height: ${rotateBitmap.height}")


            // Put it on the black background and fit the ratio
            val size = mAVSession?.frameSize
            val bkWidth = (bitmap.height * FRAME_RATIO).toInt()
            val bkHeight = bitmap.height
            val bkBitmap = Bitmap.createBitmap(bkWidth, bkHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bkBitmap)

            if (size?.height != null) {
                canvas.drawBitmap(bitmap, (bkWidth - size.height) / 2f, 0f, null)
                //Log.d(tag, "bkBitmap, Width: ${bkBitmap.width}, Height: ${bkBitmap.height}")
            }

            mAVSession?.injectScreenImage(bkBitmap)

            //  Use below code to check image reader content
            /*
            if (true) {
                val root = Environment.getExternalStorageDirectory().toString()
                val myDir = File("$root/req_images")
                myDir.mkdirs()
                val generator = Random()
                var n = 10000
                n = generator.nextInt(n)
                val fname = "Image-$n.jpg"
                val file = File(myDir, fname)

                try {
                    val out = FileOutputStream(file)
                    bkBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    out.flush()
                    out.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            */
        }, null)
    }

    fun setUserVideoAudio(id: Int, isVideoEnable: Boolean, isAudioEnable: Boolean) {
        val jStatus = lastUpdateStatus?.getControlParticipantObject(lastUpdateStatus!!, id, isVideoEnable, isAudioEnable)
        sendUpdateStatusInfo(jStatus)
    }

    fun setChatRoomEnabled(isEnable: Boolean) {
        val status = if (isEnable) 1 else 0
        if (lastUpdateStatus?.isChatRoomEnabled == status) {
            return
        }

        val jStatus = lastUpdateStatus?.getControlChatRoomObject(lastUpdateStatus!!, isEnable)
        sendUpdateStatusInfo(jStatus)
    }

    private fun parseStatus(context: Context, extraSession: String) {
        // update message user status(online/offline message)
        val curStatus = Gson().fromJson(extraSession.substringAfter(UPDATE_STATUS), UpdateStatus::class.java)
        val participants = curStatus.participants
        msgUserList.forEach { user ->
            val p1 = ParticipantStatus()
            p1.id = user.id

            if (participants != null) {
                val index = msgUserList.indexOf(user)
                var systemMsgFormat = ""
                var isStatusChange = false

                if (user.isOnline && !participants.contains(p1)) {
                    Log.d(tag, "id: ${user.id}, name: ${user.name} has been offline")
                    user.isOnline = false
                    isStatusChange = true

                    systemMsgFormat = context.getString(R.string.user_offline)
                }

                if (!user.isOnline && participants.contains(p1)) {
                    Log.d(tag, "id: ${user.id}, name: ${user.name} has been online")
                    user.isOnline = true
                    isStatusChange = true

                    systemMsgFormat = context.getString(R.string.user_online)
                }

                if (isStatusChange) {
                    val chatMessage = ChatMessage()
                    chatMessage.isSystemMessage = true
                    chatMessage.message = String.format(systemMsgFormat, user.name)
                    messageListList[index].messages?.add(chatMessage)

                    reloadMsgUserList()
                    refreshMessageListWithIndex()
                }
            }
        }

        lastUpdateStatus = curStatus

        // First receive UpdateStatus
        if (!isInitUpdateStatus) {
            // If this user is not host, no need to send initial UpdateStatus
            isInitUpdateStatus = (vcManager.sessionInfo?.hostId != vcManager.userId)
        }

        if (!isInitUpdateStatus) {
            val host = vcManager.userId?.toInt()
            lastUpdateStatus.let { it?.host = host }
            addPresenterSingle(host!!)
            isInitUpdateStatus = true
        }

        lastUpdateStatusLiveData.value = lastUpdateStatus
        setChatRoomStatus(context, lastUpdateStatus?.isChatRoomEnabled)
    }

    /**
     * 104 recv [x=[128:16:852],y=[96:16:480]] send [x=[128:16:852],y=[96:16:480]]
     */
    //var curPrefSize = Size(852, 480)
    private var curPrefSize = Size(352, 288)
    fun parseResolution(context: Context, extraSession: String) {
        if (!isDynamicAdjustResolution) {
            Log.d(tag, "Not enable isDynamicAdjustResolution")
            return
        }

        // Not speakers
        if (!isVideoActivateLiveData.value!!) {
            Log.d(tag, "Video is not activated now")
            return
        }

        // Parse size first
        val send = extraSession.substringAfter("send")
        val x = send.substring(send.indexOf("128:16:") + 7, send.indexOf("],y")).toInt()
        val y = send.substring(send.indexOf("96:16:") + 6, send.indexOf("]]")).toInt()
        var size = Size(x, y)
        Log.d(tag, "Server prefer size: ${size.width}, ${size.height}")

        // Dual mode
        if (lastUpdateStatus?.presentMode == PresentMode.DUAL_HOST) {
            size = Size(x*2, y*2)
        }

        if (curPrefSize == size) {
            Log.d(tag, "Same prefer size with last setting")
            return
        }

        curPrefSize = size
        val prefSize = PrefSizeMap.getPrefSize(curPrefSize)
        MediaSessionMgr.defaultsSetPrefVideoSize(tmedia_pref_video_size_t.valueOf(prefSize.toString()))
//        MediaSessionMgr.defaultsSetPrefVideoSize(tmedia_pref_video_size_t.valueOf(
//                tmedia_pref_video_size_t.tmedia_pref_video_size_480p.toString()))

        val remoteUri = NgnUriUtils.makeValidSipUri("*${vcManager.sessionInfo?.id}")
        val avSession = NgnAVSession.createOutgoingSession(vcManager.mEngine.sipService.sipStack, NgnMediaType.AudioVideoSecondary)
        avSession.remotePartyUri = remoteUri

        val activeCall = NgnAVSession.getFirstActiveCallAndNot(avSession.id)
        activeCall?.holdCall()

        avSession.makeCall(remoteUri)

        destroyAVSession(true)
        mAVSession = avSession
        initAVSession(avSession.id.toString(), context)
    }

    fun parseExtraSession(context: Context, extraSession: String) {
        when {
            extraSession.contains(UPDATE_STATUS) -> parseStatus(context, extraSession)
            extraSession.contains(RESOLUTION_UPDATE) -> parseResolution(context, extraSession)
            else -> Log.d(tag, "Receive unknown extra sessions.")
        }
    }

    private fun setChatRoomStatus(context: Context, chatStatus: Int?) {
        when (chatStatus) {
            ChatStatus.DEFAULT -> return

            ChatStatus.DISABLE -> {
                if (lastChatStatus == ChatStatus.ENABLE) {
                    // leave message session
                    leaveMessageSession()
                }
            }

            ChatStatus.ENABLE -> {
                if (lastChatStatus != ChatStatus.ENABLE) {
                    // join message session
                    joinMessageSession(context)
                }
            }

            else -> return
        }

        lastChatStatus = chatStatus
        lastChatStatusLiveDate.value = lastChatStatus
    }

    private fun sendListenersInfo(participant: Participant) {
        // Because we already register to SIP server before enter the ConcallAcitivty,
        // so just send ListenersInfo to server when get Participant information.
        val listenerInfo = ParticipantListener(participant.id?.toInt(), participant.unit, participant.duties, participant.name)
        val info = "$LISTENER_INFO${listenerInfo.jsonContent}"
        Log.d(tag, "[SEND INFO] $info")
        mAVSession?.sendInfo(info, NgnContentType.JSON)
    }

    private fun sendUpdateStatusInfo(jStatus: JSONObject?) {
        val info = "$UPDATE_STATUS$jStatus"
        Log.d(tag, "[SEND INFO] $info")

        if (info.isEmpty()) {
            Log.d(tag, "Empty info")
            return
        }
        mAVSession?.sendInfo(info, NgnContentType.JSON)
    }

    ////////////////////////////////////
    ////// About RTSP Fragment /////////
    ////////////////////////////////////
    fun setRtspListIndex(index: Int) {
        rtspIndexLiveData.value = index
    }

    fun playSelectedRtspSource(): Boolean {
        if (rtspListLiveData.value == null || rtspIndexLiveData.value == null) {
            Log.e(tag, "Null live data")
            return false
        }

        if (rtspListLiveData.value!!.size < (rtspIndexLiveData.value!! - 1)) {
            Log.e(tag, "Index out of bound of rtspListLiveData, index: ${rtspIndexLiveData.value}")
            return false
        }

        val rtspInfo = rtspListLiveData.value!![rtspIndexLiveData.value!!]
        setVideoSourceFromRTSP(vcManager.userId?.toInt()!!, rtspInfo)

        return true
    }

    /////////////////////////////////////
    ////// About Screen Sharing /////////
    /////////////////////////////////////
    fun setImageReader(reader: ImageReader?, projection: MediaProjection?) {
        if (reader == null || projection == null) {
            Log.e(tag, "Null reader or null projection")
            return
        }

        mMediaProjection = projection
        setVideoSourceFromScreen(reader)
    }

    fun startStopProjection() {
        val me = getParticipantStatus()
        if (me?.streamingContent == StreamingContent.DESKTOP) {
            mAVSession?.startSndVideoProducerProjection()
        } else {
            mAVSession?.stopSndVideoProducerProjection()
        }
    }

    fun setIsWhiteboardLiveData(isEnable: Boolean) {
        isWhiteboardLiveData.value = isEnable
    }

    ////////////////////////////////////
    ////// About API Service ///////////
    ////////////////////////////////////
    fun getMyParticipantInfo(id: String) {
        Log.d(tag, "getMyParticipantInfo(), id: $id")

        if (VcManager.DEBUG_CONCALL_BY_VIRTUAL_SESSION) {
            // do nothing now
//            val participant = Participant()
//            participant.id = "1212"
//            participant.unit = "hsuan"
//            participant.duties = "hsuan"
//            participant.name = "hsuan"
//            sendListenersInfo(participant)
        }
        else {
            val disposableObserver = object : DisposableObserver<Participant>() {
                override fun onComplete() {
                }

                override fun onNext(participant: Participant) {
                    Log.d(tag, "onNext()")

                    sendListenersInfo(participant)
                }

                override fun onError(e: Throwable) {
                    Log.d(tag, "getMyParticipantInfo() failed")
                }
            }

            compositeDisposable.add(concallRepository.getMyParticipantInfo(id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(disposableObserver))
        }
    }

    fun getMyRtspList(id: String) {
        Log.d(tag, "getMyRtspList(), id: $id")

        rtspListLiveData.value?.clear()
//        isWaitingLiveData.value = true

        val disposableObserver = object : DisposableObserver<ArrayList<RtspInfo>>() {
            override fun onComplete() {
            }

            override fun onNext(list: ArrayList<RtspInfo>) {
                Log.d(tag, "onNext(), list size: ${list.size}")

                rtspListLiveData.value = list
//                isWaitingLiveData.value = false
            }

            override fun onError(e: Throwable) {
                Log.d(tag, "getMyRtspList() failed")
//                isWaitingLiveData.value = false
            }
        }

        compositeDisposable.add(concallRepository.getMyRtspList(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(disposableObserver))
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()

        isWaitingLiveData.value = false
    }
}
