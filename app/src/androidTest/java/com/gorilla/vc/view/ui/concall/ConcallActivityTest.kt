package com.gorilla.vc.view.ui.concall

import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.gorilla.vc.testShared.model.PreferencesHelperTest
import com.gorilla.vc.testShared.model.VcManagerTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.junit.MockitoJUnit


@RunWith(AndroidJUnit4::class)
@LargeTest
class ConcallActivityTest {

//    @get:Rule
//    val mActivityRule = ActivityTestRule<ConcallActivity>(ConcallActivity::class.java)

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()   // same as MockitoAnnotations.initMocks(this)

    @Spy
    var preferences: PreferencesHelperTest? = null

    @Spy
    var vcManager: VcManagerTest? = null

    @Before
    fun setUp() {
        //MockitoAnnotations.initMocks(this)
        vcManager?.userId = "11751"
    }

    @After
    fun tearDown() {
    }

    @Test
    fun getTag() {
    }

    @Test
    fun getPreferences() {
        `when`(preferences?.readPreferences("input")).thenReturn("output")
        assertEquals("output", preferences?.readPreferences("input"))
        Mockito.verify(preferences, Mockito.times(1))?.readPreferences(Mockito.anyString())

        assertEquals(preferences?.account, "hsuan")
    }

    @Test
    fun setPreferences() {
        `when`(preferences?.savePreferences("input")).thenReturn("output")
        assertEquals("output", preferences?.savePreferences("input"))
        Mockito.verify(preferences, Mockito.times(1))?.savePreferences(Mockito.anyString())

        preferences?.account = "hsuan2"
        assertEquals(preferences?.account, "hsuan2")
    }

    @Test
    fun getVcManager() {
        assertEquals(vcManager?.userId, "11751")
    }

    @Test
    fun setVcManager() {
        vcManager?.userId = "11902"
        assertEquals(vcManager?.userId, "11902")
    }

    @Test
    fun getFactory() {
    }

    @Test
    fun setFactory() {
    }

    @Test
    fun getDispatchingAndroidInjector() {
    }

    @Test
    fun setDispatchingAndroidInjector() {
    }

    @Test
    fun onCreate() {
    }

    @Test
    fun onResume() {
    }

    @Test
    fun onPause() {
    }

    @Test
    fun onStop() {
    }

    @Test
    fun onDestroy() {
    }

    @Test
    fun onBackPressed() {
    }

    @Test
    fun onActivityResult() {
    }

    @Test
    fun supportFragmentInjector() {
    }
}