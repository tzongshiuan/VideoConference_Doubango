package com.gorilla.vc.view.ui.login

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gorilla.vc.di.Injectable
import com.gorilla.vc.model.LoginResponse
import com.gorilla.vc.model.VcManager
import com.gorilla.vc.view.ui.session.SessionActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject

// Because we want to keep persistence of data, so I use application to instead of context here
class LogInViewModel @Inject constructor(private val context: Context, private val vcManager: VcManager
                     , private val loginRepository: LogInRepository, private val compositeDisposable: CompositeDisposable)
    : Injectable, ILogInViewModelCallback, ViewModel() {

    private val tag = LogInViewModel::class.simpleName

    /**
     * Use to skip login phase, we are no need to do authentication now
     */
    private val isPassLogin = true

    val isLoginLiveData: MutableLiveData<Boolean> = MutableLiveData()

    var isLogging = false

    /**
     * Expose the LiveData [isLoginLiveData] query so the UI can observe it.
     */
    fun getIsLoginObservable() : MutableLiveData<Boolean> {
        return isLoginLiveData
    }

    fun startLogIn() {
        Log.d(tag, "startLogIn()")

        // make sure logging is atomic procedure
        if (isLogging) {
            return
        }
        isLogging = true

        if (isPassLogin) {
            isLoginLiveData.value = true
            return
        }

        val userName = vcManager.mPreferences.account
        val password = vcManager.mPreferences.password

        val disposableObserver = object : DisposableObserver<LoginResponse>() {
            override fun onComplete() {
                Log.d(tag, "onComplete()")
            }

            override fun onNext(value: LoginResponse) {
                Log.d(tag, "onNext()")
                isLogging = false

                //check code
                isLoginLiveData.value = true
            }

            override fun onError(e: Throwable) {
                isLogging = false
                Log.d(tag, "onError e = $e")
                Log.d(tag, "onError message = $e.message")
            }
        }

        compositeDisposable.add(loginRepository.login(userName, password, "")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(disposableObserver))
    }

    override fun onLogInFailed() {
        Log.d(tag, "onLogInFailed()")
    }

    fun startLogOut() {
        Log.d(tag, "startLogOut()")
    }

    override fun onLogOutFailed() {
        Log.d(tag, "onLogOutFailed()")
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}