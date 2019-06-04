package com.gorilla.vc.utils.apiLiveData

import com.gorilla.vc.model.Status

interface ApiObserver<T>{
    abstract fun onApiCallBack(status:Status,data : T?)
}