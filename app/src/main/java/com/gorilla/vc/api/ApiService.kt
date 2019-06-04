package com.gorilla.vc.api

import com.gorilla.vc.model.BaseVcSession
import com.gorilla.vc.model.LoginResponse
import com.gorilla.vc.model.Participant
import com.gorilla.vc.model.RtspInfo
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("Portal/login")
    fun login(@Query("username") username: String, @Query("password") password: String,
              @Query("cardserial") cardSerial: String): Observable<Response<LoginResponse>>

    @GET("vc/api/participants")
    fun getParticipants(): Observable<Response<ArrayList<Participant>>>

    @GET("vc/api/participants/{id}")
    fun getParticipant(@Path("id") id: String): Observable<Response<Participant>>

    @GET("vc/api/meeting-rooms")
    fun getSessionList(): Observable<Response<ArrayList<BaseVcSession>>>

    @GET("vc/api/getCamera/{id}")
    fun getRtspList(@Path("id") id: String): Observable<Response<ArrayList<RtspInfo>>>

}