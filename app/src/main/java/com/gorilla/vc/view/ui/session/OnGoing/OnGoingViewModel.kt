package com.gorilla.vc.view.ui.session.OnGoing

import android.arch.lifecycle.ViewModel
import android.util.Log
import com.gorilla.vc.R
import com.gorilla.vc.model.OnGoingVcSession
import com.gorilla.vc.utils.apiLiveData.ApiLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import javax.inject.Inject

class OnGoingViewModel @Inject constructor(private val onGoingRepository: OnGoingRepository, private val compositeDisposable: CompositeDisposable) : ViewModel() {

    private val tag = OnGoingViewModel::class.simpleName

    val apiLiveData = object : ApiLiveData<ArrayList<OnGoingVcSession>>() {
        private var disposableObserver :DisposableObserver<ArrayList<OnGoingVcSession>>? = null

        override fun apiImpl() {
            Log.d(tag, "loadSessions()")
            disposableObserver = object : DisposableObserver<ArrayList<OnGoingVcSession>>() {
                override fun onComplete() {
                }

                override fun onNext(list: ArrayList<OnGoingVcSession>) {
                    Log.d(tag, "onNext()")
                    complete(list)
                }

                override fun onError(e: Throwable) {
                    Log.d(tag, "trackingNotificationDetail onError e = $e")
                    error(R.string.network_server_error)
                }
            }
            compositeDisposable.add(onGoingRepository.getOnGoingSessionList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(disposableObserver!!))
        }

        override fun cancelApi() {
            if(disposableObserver!=null)
                compositeDisposable.remove(disposableObserver!!)
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.d("OnGoingViewModel", "onCleared - start" )
        compositeDisposable.clear()
    }
}