package com.gorilla.vc.view.ui.session.OnGoing

import com.gorilla.vc.api.ApiService
import com.gorilla.vc.mocks.MockRetrofit
import com.gorilla.vc.model.*
import com.gorilla.vc.testShared.model.VcManagerTest
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class OnGoingRepositoryTest {

    private var vcManager: VcManagerTest? = null

    @Before
    fun setUp() {
        vcManager = Mockito.spy(VcManagerTest::class.java)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun getOnGoingSessionList() {

        val retrofit1 = MockRetrofit()
        val service1 = retrofit1.create(ApiService::class.java)
        retrofit1.path = MockApiServiceAsset.PARTICIPANTS_DATA
        val participants =
                service1.getParticipants().map { response -> response.body() }

        val retrofit2 = MockRetrofit()
        val service2 = retrofit2.create(ApiService::class.java)
        retrofit2.path = MockApiServiceAsset.MEETING_ROOMS_DATA
        val sessionList =
                service2.getSessionList().map { response -> response.body() }

        val list = ArrayList<OnGoingVcSession>()
        Observable.zip<ArrayList<Participant>, ArrayList<BaseVcSession>, ArrayList<OnGoingVcSession>>(
                participants,
                sessionList,
                BiFunction<ArrayList<Participant>, ArrayList<BaseVcSession>, ArrayList<OnGoingVcSession>> { t1, t2 ->
                    if (vcManager?.userId.isNullOrEmpty()) {
                        t1.forEach loop@{ participant ->
                            if (vcManager?.mPreferences?.account == participant.name) {
                                vcManager?.userId = participant.id
                                vcManager?.joinSessions = participant.joinSessions!!
                                return@loop
                            }
                        }
                    }

                    if (!vcManager?.userId.isNullOrEmpty()) {
                        t2.forEach { session ->
                            val ongoineSession = OnGoingVcSession(session)

                            // filter session status
                            if (ongoineSession.status == SessionStatus.ONGOING) {
                                // filter whether current user is belong to this session
                                ongoineSession.participants?.forEach loop@{ participant ->
                                    if (vcManager?.joinSessions?.contains(participant)!!) {
                                        list.add(ongoineSession)
                                        return@loop
                                    }
                                }

                                // find host name to show on UI
                                t1.forEach loop@{ participant ->
                                    if (ongoineSession.hostId == participant.id) {
                                        ongoineSession.hostName = participant.name
                                        return@loop
                                    }
                                }
                            }
                        }
                    }
                    list
                }
        ).test()
         .assertValue {
             list.size == 2
         }
    }
}