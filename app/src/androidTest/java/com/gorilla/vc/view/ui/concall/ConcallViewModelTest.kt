package com.gorilla.vc.view.ui.concall

import android.app.AlertDialog
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.MutableLiveData
import android.support.test.espresso.Espresso.onView
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.gorilla.vc.R
import com.gorilla.vc.model.ParticipantStatus
import com.gorilla.vc.model.PresentMode
import com.gorilla.vc.testShared.model.UpdateStatusTest
import com.gorilla.vc.testShared.model.VcManagerTest
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Spy
import org.mockito.junit.MockitoJUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
class ConcallViewModelTest {

    @get:Rule
    val mActivityRule = ActivityTestRule<ConcallActivity>(ConcallActivity::class.java)

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()

    inline fun <reified T> lambdaMock(): T = mock(T::class.java)

    private val isChatEnableLiveData: MutableLiveData<Boolean> = MutableLiveData()

    @Volatile var isLeaveConcallManual: Boolean = false

    @Spy
    private var lastUpdateStatus: UpdateStatusTest?= null

    @Spy
    private var vcManager: VcManagerTest? = null

    private fun initUpdateStatus() {
        lastUpdateStatus?.host = 11751
        lastUpdateStatus?.isChatRoomEnabled = 1
        lastUpdateStatus?.isRecord = false

        val list = ArrayList<ParticipantStatus>()
        val participant1 = ParticipantStatus()
        participant1.id = 11751
        participant1.isMicEnabled = true
        participant1.isCamEnabled = true
        participant1.streamingContent = 0
        participant1.rtspUrl = ""
        participant1.streamingContentStatus = 1
        val participant2 = ParticipantStatus()
        participant2.id = 11902
        participant2.isMicEnabled = true
        participant2.isCamEnabled = true
        participant2.streamingContent = 0
        participant2.rtspUrl = ""
        participant2.streamingContentStatus = 1
        list.add(participant1)
        list.add(participant2)
        lastUpdateStatus?.participants = list
        lastUpdateStatus?.presentMode = PresentMode.DUAL_HOST
        lastUpdateStatus?.presenter1 = 11751
        lastUpdateStatus?.presenter2 = 11902
    }

    @Before
    fun setUp() {
        initUpdateStatus()

        vcManager?.userId = "11751"
    }

    @After
    fun tearDown() {
    }

    ////////////////////////////////////////////
    // Variable Test Start
    @Test
    fun isChatEnableLiveData() {
        //isChatEnableLiveData.postValue(false)
        //assertEquals(false, isChatEnableLiveData.value)
        //isChatEnableLiveData.postValue(true)
        //assertEquals(true, isChatEnableLiveData.value)

        val observer = lambdaMock<(Boolean) -> Unit>()
        val lifecycle = LifecycleRegistry(mock(LifecycleOwner::class.java))
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        isChatEnableLiveData.observe({lifecycle}) { isEnable ->
            isEnable?.let(observer)
        }

        isChatEnableLiveData.postValue(true)

        verify(observer).invoke(true)
    }

    @Test
    fun getJoinChatUserLiveData() {
    }

    @Test
    fun getMessageListLiveData() {
    }

    @Test
    fun isExpandControlView() {
    }

    @Test
    fun setExpandControlView() {
    }

    @Test
    fun getChatUserIndexLiveData() {
    }

    @Test
    fun setChatUserIndexLiveData() {
    }

    @Test
    fun getNewMessageUserLiveData() {
    }

    @Test
    fun setNewMessageUserLiveData() {
    }

    @Test
    fun getMessageUserListLiveData() {
    }

    @Test
    fun setMessageUserListLiveData() {
    }

    @Test
    fun isWaitingLiveData() {
    }

    @Test
    fun getLastUpdateStatusLiveData() {
    }

    @Test
    fun getLastUpdateStatus() {
    }

    @Test
    fun setLastUpdateStatus() {
    }

    @Test
    fun getLastChatStatusLiveDate() {
    }

    @Test
    fun getMAVSession() {
    }

    @Test
    fun setMAVSession() {
    }

    @Test
    fun isLeaveConcallManual() {
    }

    @Test
    fun setLeaveConcallManual() {
    }

    @Test
    fun isVideoActivateLiveData() {
    }

    @Test
    fun getRtspListLiveData() {
    }

    @Test
    fun getRtspIndexLiveData() {
    }

    @Test
    fun isWhiteboardLiveData() {
    }

    @Test
    fun setVideoActivateStatus() {
    }

    @Test
    fun getParticipantStatus() {
    }
    // Variable Test End
    ////////////////////////////////////////////

    private fun blockUI(millis: Long) {
        Thread.sleep(millis)
    }

    @Test
    fun leaveConcall() {
        if (lastUpdateStatus == null) {
            isLeaveConcallManual = true
            assertEquals(isLeaveConcallManual, true)
            return
        }

        if (vcManager?.userId?.toInt() == lastUpdateStatus?.host
                && lastUpdateStatus?.participants?.size!! > 1) {
            val context = mActivityRule.activity
            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.resources.getString(R.string.alert_exit_title))
            builder.setMessage(context.resources.getString(R.string.hint_host_leave))
            builder.setCancelable(true)

            builder.setPositiveButton(context.resources.getString(R.string.ok)) { _, _ ->
                // Nothing need to do, so I just refresh the video view here
            }

            context.runOnUiThread {
                builder.create().show()
            }

            blockUI(2000)
        }
    }

    @Test
    fun showLeaveConcallDialog() {
        val context = mActivityRule.activity
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.alert_exit_title))
        builder.setMessage(context.resources.getString(R.string.alert_exit_message))
        builder.setCancelable(true)

        builder.setNegativeButton(context.resources.getString(R.string.exit)) { _, _ ->
            //show loading dialog here
//            isWaitingLiveData.value = true
//            isLeaveConcallManual = true
//            vcManager.unRegister()
        }

        builder.setPositiveButton(context.resources.getString(R.string.cont)) { _, _ ->
            // Nothing need to do, so I just refresh the video view here
            refreshVideoView()
        }

        context.runOnUiThread {
            builder.create().show()
        }

        blockUI(2000)
    }

    @Test
    fun showTerminateDialog() {
    }

    @Test
    fun initAVSession() {
    }

    @Test
    fun destroyAVSession() {
    }

    @Test
    fun initGlobalChatSession() {
    }

    @Test
    fun reloadMsgUserList() {
    }

    @Test
    fun addMessageUserItem() {
    }

    @Test
    fun changeSelectUserIndex() {
    }

    @Test
    fun toggleControlView() {
    }

    @Test
    fun destoryChatSession() {
    }

    @Test
    fun sendChatMessage() {
    }

    @Test
    fun refreshVideoView() {
    }

    @Test
    fun setPresenterNone() {
    }

    @Test
    fun addPresenterSingle() {
    }

    @Test
    fun addPresenterDual() {
    }

    @Test
    fun changeHost() {
    }

    @Test
    fun setVideoSourceFromCamera() {
    }

    @Test
    fun setVideoSourceFromScreen() {
    }

    @Test
    fun setUserVideoAudio() {
    }

    @Test
    fun setChatRoomEnabled() {
    }

    @Test
    fun parseResolution() {
    }

    @Test
    fun parseExtraSession() {
    }

    @Test
    fun setRtspListIndex() {
    }

    @Test
    fun playSelectedRtspSource() {
    }

    @Test
    fun setImageReader() {
    }

    @Test
    fun startStopProjection() {
    }

    @Test
    fun setIsWhiteboardLiveData() {
    }

    @Test
    fun getMyParticipantInfo() {
    }

    @Test
    fun getMyRtspList() {
    }

    @Test
    fun onCleared() {
    }
}