package com.example.shawnwang.searchtest

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.*
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.gorilla.vc.R

class WaitingProgress(context: Context, attrs: AttributeSet? = null) : RelativeLayout(context, attrs){

    companion object {
        val SHOW_WITH_ANIMATE = 1001
        val HIDE_WITH_ANIMATE = 1002
        val SHOW_WITHOUT_ANIMATE = 1003
        val HIDE_WITHOUT_ANIMATE = 1004
    }

    private val DURATION:Long = 500
    private var progressLayout:LinearLayout?=null
    private val animationManager : ReversibleAnimationManager = ReversibleAnimationManager(this,AlphaAnimation(0.0F,1.0F),DURATION)
    private var progressAnimationManager :ReversibleAnimationManager? = null

    init {
        val mInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mInflater.inflate(R.layout.waiting_progress_view, this)
        progressLayout = findViewById(R.id.progressLayout)
        progressLayout?.visibility = visibility
//        val scaleAnimation = ScaleAnimation(0.6F,1.0F,0.6F,1.0F,Animation.RELATIVE_TO_SELF,0.5F,Animation.RELATIVE_TO_SELF,0.5F)
        val translateAnimation = TranslateAnimation(0.0F,0.0F,150.0F,0.0F)
//        val animatorSet = AnimationSet(true)
//        animatorSet.addAnimation(scaleAnimation)
//        animatorSet.addAnimation(translateAnimation)
        progressAnimationManager = ReversibleAnimationManager(progressLayout!!,translateAnimation,DURATION)
        setOnClickListener {  }
    }

    fun setProgressVisible(state:Int){
        when(state){
            SHOW_WITH_ANIMATE -> setVisibleWithAnimate(true)
            HIDE_WITH_ANIMATE -> setVisibleWithAnimate(false)
            SHOW_WITHOUT_ANIMATE -> setVisibleImmediately(true)
            HIDE_WITHOUT_ANIMATE -> setVisibleImmediately(false)
        }
    }

    fun setVisibleWithAnimate(isVisible:Boolean){
        if(!isAttachedToWindow){
            setVisibleImmediately(isVisible)
            return
        }
        progressAnimationManager!!.setVisibleWithAnimate(isVisible)
        animationManager.setVisibleWithAnimate(isVisible)
    }

    fun setVisibleImmediately(isVisible:Boolean){
        progressAnimationManager!!.setVisibleImmediately(isVisible)
        animationManager.setVisibleImmediately(isVisible)
    }
}