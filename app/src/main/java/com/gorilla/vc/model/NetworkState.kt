package com.gorilla.vc.model

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.gorilla.vc.R

enum class Status {
    RUNNING,
    SUCCESS,
    FAILED
}

@Suppress("DataClassPrivateConstructor")
data class NetworkState private constructor(
        val status: Status,
        val msgRes: Int = R.string.network_no_network) {
    companion object {
        val LOADED = NetworkState(Status.SUCCESS)
        val LOADING = NetworkState(Status.RUNNING)
        fun error(msgRes: Int) = NetworkState(Status.FAILED, msgRes)
        var isNetworkConnected = MutableLiveData<Boolean>()


        @SuppressLint("WrongConstant", "ObsoleteSdkInt")
        private fun isNetworkAvailable(context: Context): Boolean {
            val localPackageManager = context.packageManager
            if (localPackageManager.checkPermission("android.permission.ACCESS_NETWORK_STATE", context.packageName) != 0) {
                return false
            }

            val localConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            try {
                // always >= 21 now, current is 22
                if (Build.VERSION.SDK_INT >= 21) {
                    val networks = localConnectivityManager.allNetworks
                    for (network in networks) {
                        val capabilities = localConnectivityManager.getNetworkCapabilities(network)
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                            return true
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val info = localConnectivityManager.allNetworkInfo

                    if (info != null) {
                        for (i in info.indices) {
                            if (info[i] == null)
                                continue
                            if (info[i].isConnected) {
                                return true
                            }
                        }
                    }
                }

            } catch (e: Exception) {

            }

            return false
        }

        fun updateNetworkConnected(context: Context) : Boolean{
            val available = isNetworkAvailable(context)
            if(isNetworkConnected.value!=available) {
                isNetworkConnected.value = available
                return true
            }
            return false
        }
    }


}