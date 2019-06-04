package com.gorilla.vc.view.ui.concall

import com.gorilla.vc.api.ApiService
import com.gorilla.vc.mocks.MockRetrofit
import com.gorilla.vc.model.MockApiServiceAsset
import com.gorilla.vc.testShared.model.VcManagerTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class ConcallRepositoryTest {

    private var vcManager: VcManagerTest? = null

    @Before
    fun setUp() {
        vcManager = Mockito.spy(VcManagerTest::class.java)
        vcManager?.userId = "11751"
    }

    @After
    fun tearDown() {
    }

    @Test
    fun getMyParticipantInfo() {
        val retrofit = MockRetrofit()
        val service = retrofit.create(ApiService::class.java)

        retrofit.path = MockApiServiceAsset.PARTICIPANT_ID_DATA
        service.getParticipant(vcManager?.userId!!)
                .test()
                .assertValue {
                    val participant = it.body()

                    participant?.id == vcManager?.userId
                    && participant?.name == "hsuan"
                }
    }

    @Test
    fun getMyRtspList() {
        val retrofit = MockRetrofit()
        val service = retrofit.create(ApiService::class.java)

        retrofit.path = MockApiServiceAsset.GET_CAMERA_DATA
        service.getRtspList(vcManager?.userId!!)
                .test()
                .assertValue {
                    val list = it.body()

                    list?.size == 2
                    && list[0].channel == 1
                    && list[1].channel == 2
                }
    }
}