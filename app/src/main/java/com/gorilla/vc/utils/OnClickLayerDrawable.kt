package com.gorilla.vc.utils

import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable

abstract class OnClickLayerDrawable(background: Drawable) : LayerDrawable(arrayOf(background, background)) {

    companion object {
        private const val ID_BACKGROUND = 0
        private const val ID_CLICK_CHECK = 1
    }

    init {
        setId(0, ID_BACKGROUND)
        setId(1, ID_CLICK_CHECK)
        setDrawableByLayerId(ID_CLICK_CHECK, object : OnClickDrawable(){
            override fun onRelease() {
                this@OnClickLayerDrawable.onRelease()
            }

            override fun onPress() {
                this@OnClickLayerDrawable.onPress()
            }
        })
    }

    fun setBackground(drawable: Drawable) {
        setDrawableByLayerId(ID_BACKGROUND, drawable)
    }

    abstract fun onPress()
    abstract fun onRelease()

}