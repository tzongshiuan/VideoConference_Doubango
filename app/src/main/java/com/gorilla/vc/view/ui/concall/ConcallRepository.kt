package com.gorilla.vc.view.ui.concall

import com.gorilla.vc.api.ApiService
import com.gorilla.vc.model.*
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ConcallRepository @Inject constructor(private val apiService: ApiService, private val vcManager: VcManager) {

    fun getMyParticipantInfo(id: String): Observable<Participant> {
        return apiService.getParticipant(id)
                .subscribeOn(Schedulers.io())
                .map { response -> response.body()
                }
    }

    fun getMyRtspList(id: String): Observable<ArrayList<RtspInfo>> {
        return apiService.getRtspList(id)
                .subscribeOn(Schedulers.io())
                .map { response -> response.body()
                }
    }
}