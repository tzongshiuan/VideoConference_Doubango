package com.gorilla.vc.view.customized

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.gorilla.vc.R


class LoadingDialog(val context: Context) {

    private var dialog:Dialog ?= null

    fun showDialog() {
        if (dialog == null) {
            dialog = Dialog(this.context)
            dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog?.setCancelable(false)
            dialog?.setContentView(R.layout.loading_dialog)

            //...initialize the imageView form layout
            val gifImageView = dialog?.findViewById<ImageView>(R.id.custom_loading_imageView)

            Glide.with(context)
                    .load(R.drawable.loading)
                    .into(gifImageView)
        }

        dialog?.show()
    }

    fun dismissDialog() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }
}