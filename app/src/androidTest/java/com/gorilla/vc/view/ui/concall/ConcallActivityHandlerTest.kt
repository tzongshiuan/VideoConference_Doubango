package com.gorilla.vc.view.ui.concall

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Rule
import org.junit.runner.RunWith
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.*
import com.gorilla.vc.R
import android.support.test.rule.ActivityTestRule
import android.view.View
import android.widget.RelativeLayout
import com.gorilla.vc.view.ui.concall.whiteboard.WhiteBoardFragment
import org.hamcrest.Matcher


@RunWith(AndroidJUnit4::class)
@LargeTest
class ConcallActivityHandlerTest {

    @get:Rule
    val mActivityRule = ActivityTestRule<ConcallActivity>(ConcallActivity::class.java)

    val tag = ConcallActivityHandler::class.simpleName
    var sessionId = ""

    var instrumentationCtx: Context? = null

    @Before
    fun setUp() {
        instrumentationCtx = InstrumentationRegistry.getContext()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun getTag() {
    }

    @Test
    fun getSessionId() {
        //assertEquals(tag, "ConcallActivityHandler")
    }

    @Test
    fun setSessionId() {
    }

    @Test
    fun onSettingBtnClick() {
        onView(withId(R.id.settingBtn)).perform()
    }

    @Test
    fun onSpeakingBtnClick() {
        // must set control panel to View.Visible at beginning
        onView(withId(R.id.controlPanel)).perform(setControlPanelVisibility(true))

        waitControlPanelShowing()

        onView(withId(R.id.speakingBtn)).perform(click())
    }

    @Test
    fun onCameraBtnClick() {
        onView(withId(R.id.controlPanel)).perform(setControlPanelVisibility(true))

        waitControlPanelShowing()

        onView(withId(R.id.cameraBtn)).perform(click())
    }

    @Test
    fun onScreenCastBtnClick() {
        onView(withId(R.id.controlPanel)).perform(setControlPanelVisibility(true))

        waitControlPanelShowing()

        onView(withId(R.id.screenSharingBtn)).perform(click())
    }

    @Test
    fun onWhiteboardBtnClick() {
        onView(withId(R.id.controlPanel)).perform(setControlPanelVisibility(true))

        waitControlPanelShowing()

        onView(withId(R.id.whiteboardBtn)).perform(click())
    }

    @Test
    fun startWhiteboardFragment() {
        val fragmentTransaction = mActivityRule.activity.supportFragmentManager.beginTransaction()
        val whiteboardFragment = WhiteBoardFragment()
        fragmentTransaction.add(R.id.concallMainLayout, whiteboardFragment).addToBackStack(null)
        fragmentTransaction.commit()
    }

    @Test
    fun onLeaveBtnClick() {
        onView(withId(R.id.controlPanel)).perform(setControlPanelVisibility(true))

        waitControlPanelShowing()

        onView(withId(R.id.leaveBtn)).perform(click())
    }

    private fun waitControlPanelShowing() {
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun setControlPanelVisibility(value: Boolean): ViewAction {
        return object : ViewAction {

            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(RelativeLayout::class.java)
            }

            override fun perform(uiController: UiController, view: View) {
                view.visibility = if (value) View.VISIBLE else View.GONE
            }

            override fun getDescription(): String {
                return "Show / Hide View"
            }
        }
    }
}