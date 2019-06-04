package com.gorilla.vc.api

import okhttp3.Interceptor
import okhttp3.Response

open class HostSelectionInterceptor(host:String) : Interceptor {

    open var host:String = host

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val host = this.host
        //if (host != null) {
            val newUrl = request.url().newBuilder()
                    .host(host)
                    .build()
            request = request.newBuilder()
                    .url(newUrl)
                    .build()
        //}
        return chain.proceed(request)
    }
}