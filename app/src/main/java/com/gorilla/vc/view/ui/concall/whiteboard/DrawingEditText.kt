package com.gorilla.vc.view.ui.concall.whiteboard

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText

class DrawingEditText: EditText {

    var fragment: WhiteBoardFragment? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setRootFragment(f: WhiteBoardFragment) {
        this.fragment = f
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            fragment?.onBackPressed()
        }

        return super.onKeyPreIme(keyCode, event)
    }
}