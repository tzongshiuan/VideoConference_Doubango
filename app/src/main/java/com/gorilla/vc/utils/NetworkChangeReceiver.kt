package com.gorilla.vc.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gorilla.vc.model.NetworkState

class NetworkChangeReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        NetworkState.updateNetworkConnected(p0!!)
    }
}