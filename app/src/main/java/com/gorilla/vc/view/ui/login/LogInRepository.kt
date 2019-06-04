package com.gorilla.vc.view.ui.login

import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import com.gorilla.vc.api.ApiService
import com.gorilla.vc.model.*
import retrofit2.Response
import javax.inject.Inject
import io.reactivex.functions.Function

class LogInRepository @Inject constructor(private val apiService: ApiService, private val preferencesHelper: PreferencesHelper) {

    private val tag = LogInRepository::class.simpleName

    /**
     * Send the command to login Portal website
     */
    fun login(username : String, password : String, cardSerial : String): Observable<LoginResponse> {
        return apiService.login(username, password, cardSerial)
                .subscribeOn(Schedulers.io())
                .map(object : Function<Response<LoginResponse>, LoginResponse> {
                    override fun apply(response: Response<LoginResponse>): LoginResponse? {
                        return if(response.isSuccessful){
                            response.body()
                        } else{
                            //val string = response.errorBody()?.string()
                            val error = Gson().fromJson(response.errorBody()?.string(), LoginResponse::class.java)
                            error
                        }
                    }
                })
    }
}