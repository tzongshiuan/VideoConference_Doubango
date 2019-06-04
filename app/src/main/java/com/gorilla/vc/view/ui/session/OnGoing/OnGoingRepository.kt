package com.gorilla.vc.view.ui.session.OnGoing

import android.util.Log
import com.gorilla.vc.api.ApiService
import com.gorilla.vc.model.*
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class OnGoingRepository @Inject constructor(private val apiService: ApiService, private val vcManager: VcManager) {

    // for convenient development
    val isPassSessionType = false

    fun getOnGoingSessionList(): Observable<ArrayList<OnGoingVcSession>> {
        val participants = apiService.getParticipants().map{ response -> response.body() }
                .onErrorReturn {
                    Log.e("OnGoingRepository",it.toString())
                    null
                }
        val sessionList = apiService.getSessionList().map { response -> response.body() }
                .onErrorReturn {
                    Log.e("OnGoingRepository",it.toString())
                    null
                }
        val list = ArrayList<OnGoingVcSession>()
        return Observable.zip<ArrayList<Participant>, ArrayList<BaseVcSession>, ArrayList<OnGoingVcSession>>(
                participants,
                sessionList,
                BiFunction<ArrayList<Participant>, ArrayList<BaseVcSession>, ArrayList<OnGoingVcSession>> { t1, t2 ->
                    Log.d("OnGoingRepository","start")
                    if (vcManager.userId.isNullOrEmpty()) {
                        t1.forEach loop@{ participant ->
                            if (vcManager.mPreferences.account == participant.name) {
                                vcManager.userId = participant.id
                                vcManager.joinSessions = participant.joinSessions!!
                                return@loop
                            }
                        }
                    }

                    if (!vcManager.userId.isNullOrEmpty()) {
                        t2.forEach { session ->
                            val ongoineSession = OnGoingVcSession(session)

                            // filter session status
                            if (ongoineSession.status == SessionStatus.ONGOING || isPassSessionType) {
                                // filter whether current user is belong to this session
                                ongoineSession.participants?.forEach loop@{ participant ->
                                    if (vcManager.joinSessions.contains(participant)) {
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
                    Log.d("OnGoingRepository","end")

                    list
                }
        ).subscribeOn(Schedulers.io())
    }

}