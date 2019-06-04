package com.gorilla.vc.view.ui.session.reserve

import android.arch.lifecycle.ViewModel
import android.util.Log
import com.gorilla.vc.R
import com.gorilla.vc.model.ReserveVcSession
import com.gorilla.vc.utils.apiLiveData.ApiLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject

class ReserveViewModel @Inject constructor(private val reserveRepository: ReserveRepository, private val compositeDisposable: CompositeDisposable) : ViewModel() {

    private val tag = ReserveViewModel::class.simpleName
    
    val apiLiveData = object :ApiLiveData<ArrayList<ReserveVcSession>>(){

        private var disposableObserver :DisposableObserver<ArrayList<ReserveVcSession>>? = null
        override fun cancelApi() {
            if(disposableObserver!=null)
                compositeDisposable.remove(disposableObserver!!)
        }

        override fun apiImpl() {
            Log.d(tag, "loadSessions()")
            disposableObserver = object : DisposableObserver<ArrayList<ReserveVcSession>>() {
                override fun onComplete() {
                }

                override fun onNext(list: ArrayList<ReserveVcSession>) {
                    Log.d(tag, "onNext()")
                    complete(list)
                }

                override fun onError(e: Throwable) {
                    error(R.string.network_server_error)
                    Log.d(tag, "trackingNotificationDetail onError e = $e")
                }
            }
            compositeDisposable.add(reserveRepository.getReserveSessionList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(disposableObserver!!))
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}