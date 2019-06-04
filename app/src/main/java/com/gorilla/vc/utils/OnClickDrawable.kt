package com.gorilla.vc.utils

import android.graphics.drawable.StateListDrawable

abstract class OnClickDrawable : StateListDrawable() {
    abstract fun onPress()
    abstract fun onRelease()
    override fun onStateChange(stateSet: IntArray): Boolean {
        if(checkIsPressed(stateSet)){
            onPress()
        }else{
            onRelease()
        }
        return super.onStateChange(stateSet)
    }

    private fun checkIsPressed(stateSet: IntArray): Boolean {
        for (state in stateSet)
            if (state == android.R.attr.state_pressed)
                return true
        return false
    }

}