package com.gorilla.vc.utils.apiLiveData

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import com.gorilla.vc.R
import com.gorilla.vc.model.NetworkState
import com.gorilla.vc.model.Status

abstract class ApiLiveData<T> {
    private val mutableLiveData = MutableLiveData<ApiItem<T>>()

    var currentStatus : NetworkState? = null

    protected abstract fun apiImpl()
    abstract fun cancelApi()

    fun complete(data:T){
        mutableLiveData.value = ApiItem(NetworkState.LOADED,data)
    }

    fun error(errorMsgSrc : Int){
        mutableLiveData.value = ApiItem(NetworkState.error(errorMsgSrc),null)
    }

    fun callApi(context: Context) : Boolean{
        if (mutableLiveData.value == null || mutableLiveData.value!!.networkState.status == Status.FAILED) {
            val isChange = NetworkState.updateNetworkConnected(context)
            if( NetworkState.isNetworkConnected.value == false )
                return false
            if (isChange) {
                return true
            }
        }
        mutableLiveData.value = ApiItem(NetworkState.LOADING, null)
        return true
    }

    fun observe(owner: LifecycleOwner, apiObserver: ApiObserver<T>) {
        NetworkState.isNetworkConnected.observe(owner, Observer<Boolean> {
            if (it!!) {
                mutableLiveData.value = ApiItem(NetworkState.LOADING, null)
            } else {
                mutableLiveData.value = ApiItem(NetworkState.error(R.string.network_no_network), null)
            }
        })
        mutableLiveData.observe(owner, Observer {
            currentStatus = it!!.networkState
            apiObserver.onApiCallBack(it.networkState.status,it.data)
            when (it.networkState.status) {
                Status.RUNNING -> {
                    cancelApi()
                    apiImpl()
                }
                else -> {
                }
            }
        })
    }

    class ApiItem<T>(val networkState: NetworkState, val data: T?)
}