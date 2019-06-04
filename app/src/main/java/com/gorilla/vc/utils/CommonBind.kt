package com.gorilla.vc.utils

import android.databinding.BindingAdapter
import android.graphics.drawable.Drawable
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.gorilla.vc.R
import com.gorilla.vc.model.Status
import com.gorilla.vc.utils.apiLiveData.ApiLiveData
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object CommonBind {

    /**
     * Get the custom start date by original start date,
     * start date example: "2018-10-15T13:00:00+08:00"
     */
    @JvmStatic
    @BindingAdapter("convertDateOnly")
    fun convertDateOnly(view: TextView, date: String) {
        try {
            val inSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
            val d = inSdf.parse(date)

            val outSdf = SimpleDateFormat(view.context.getString(R.string.session_date_format), Locale.getDefault())
            view.text = outSdf.format(d)
        } catch (e: ParseException) {
            System.out.println(e.toString())
        }
    }

    @JvmStatic
    @BindingAdapter("pressDrawable","pressTextColor",requireAll = true)
    fun setButtonBackground(button: Button,pressDrawable:Drawable,pressTextColor:Int){
        if(button.background is OnClickLayerDrawable)
            return
        val releaseBg = button.background
        val releaseTextColor = button.currentTextColor
        button.background = object : OnClickLayerDrawable(releaseBg){
            override fun onPress() {
                setBackground(pressDrawable)
                button.setTextColor(pressTextColor)
                invalidateSelf()
                button.invalidate()
            }

            override fun onRelease() {
                setBackground(releaseBg)
                button.setTextColor(releaseTextColor)
                invalidateSelf()
                button.invalidate()
            }
        }
    }

    @JvmStatic
    @BindingAdapter("pressColor",requireAll = true)
    fun setButtonBackground(imageView:ImageView,pressColor:Int){
        if(imageView.background is OnClickDrawable)
            return
        imageView.background = object :OnClickDrawable(){
            override fun onPress() {
                imageView.setColorFilter(pressColor)
            }

            override fun onRelease() {
                imageView.clearColorFilter()
            }
        }
    }

    @JvmStatic
    @BindingAdapter("retryStatus",requireAll = true)
    fun setButtonRetry(button: Button,apiLiveData:ApiLiveData<Any>){
        button.setOnClickListener {
            apiLiveData.callApi(button.context)
        }
    }
}