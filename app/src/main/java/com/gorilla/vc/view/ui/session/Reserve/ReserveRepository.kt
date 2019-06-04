package com.gorilla.vc.view.ui.session.reserve

import com.gorilla.vc.api.ApiService
import com.gorilla.vc.model.*
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ReserveRepository @Inject constructor(private val apiService: ApiService, private val vcManager: VcManager) {
    fun getReserveSessionList(): Observable<ArrayList<ReserveVcSession>> {
        val participants = apiService.getParticipants().map{ response -> response.body() }
                .onErrorReturn { null }
        val sessionList = apiService.getSessionList().map { response -> response.body() }
                .onErrorReturn { null }
        // for convenient development
        val isPassSessionType = false

        val list = ArrayList<ReserveVcSession>()
        return Observable.zip<ArrayList<Participant>, ArrayList<BaseVcSession>, ArrayList<ReserveVcSession>>(
                participants,
                sessionList,
                BiFunction<ArrayList<Participant>, ArrayList<BaseVcSession>, ArrayList<ReserveVcSession>> { t1, t2 ->
                    if (vcManager.userId.isNullOrEmpty()) {
                        t1.forEach loop@{ participant ->
                            if (vcManager.mPreferences.account == participant.name) {
                                vcManager.userId = participant.id
                                vcManager.joinSessions = participant.joinSessions!!
                                return@loop
                            }
                        }
                    }

                    vcManager.initIdToNameMap(t1)

                    if (!vcManager.userId.isNullOrEmpty()) {
                        t2.forEach { session ->
                            val reserveSession = ReserveVcSession(session)

                            // filter session status
                            if (reserveSession.status == SessionStatus.RESERVED || isPassSessionType) {
                                // filter whether current user is belong to this session
                                reserveSession.participants?.forEach loop@{ participant ->
                                    if (vcManager.joinSessions.contains(participant)) {
                                        list.add(reserveSession)
                                        return@loop
                                    }
                                }

                                // find host name to show on UI
                                t1.forEach loop@{ participant ->
                                    if (reserveSession.hostId == participant.id) {
                                        reserveSession.hostName = participant.name
                                        return@loop
                                    }
                                }
                            }
                        }
                    }
                    list
                }
        ).subscribeOn(Schedulers.io())
    }
}