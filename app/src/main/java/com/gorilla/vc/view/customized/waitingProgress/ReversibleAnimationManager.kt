package com.example.shawnwang.searchtest

import android.view.View
import android.view.animation.*

class ReversibleAnimationManager(view: View, anim:Animation, duration: Long){

    private val view = view
    private val animation = anim
    private val duration = duration

    private val STATUS_INIT = 100001
    private val STATUS_FADE_IN = 100002
    private val STATUS_FADE_OUT = 100003

    private var currentPos:Float = -1.0F
    private var currentStatus = STATUS_INIT

    init {
        currentPos = if(view.visibility== View.INVISIBLE || view.visibility== View.GONE){
            0.0F
        }else{
            1.0F
        }
    }
    private fun getAnimate(anim:Animation):Animation{
        anim.interpolator = object : AccelerateDecelerateInterpolator (){
            private var lastInput = 0.0F
            override fun getInterpolation(input: Float): Float {
                if(currentStatus == STATUS_INIT)
                    return super.getInterpolation(currentPos)

                val dist:Float = Math.abs(lastInput-input)
                lastInput = input

                val pos:Float = if(currentStatus == STATUS_FADE_IN) currentPos + dist else currentPos - dist

                currentPos = when {
                    pos>1.0F -> 1.0F
                    pos<0.0F -> 0.0F
                    else -> pos
                }

//                if(currentStatus==STATUS_FADE_IN){
//                    if(currentPos>0.75){
//                        setVisibleWithAnimate(false)
//                    }
//                }else{
//                    if(currentPos<0.25){
//                        setVisibleWithAnimate(true)
//                    }
//                }

//                Log.i("aaaaaaaa","STATUS = $currentStatus  currentPos = $currentPos  input = $input" )
                checkFinished()
                return super.getInterpolation(currentPos)
            }
        }
        anim.duration = duration
        anim.fillAfter = true
        anim.isFillEnabled = true
        anim.setAnimationListener(object :Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
//                Log.i("bbbbbb","finish")
                checkFinished()
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
        return anim
    }

    private fun addAnimate(){
        //val anim = AnimationUtils.loadAnimation(context,R.anim.progress_aim)
//        Log.i(javaClass.name,"addAnimate")
        val progressAnim = getAnimate(animation)
        view.startAnimation(progressAnim)
    }

    private fun isFloatEqual(a:Float, b:Float):Boolean = Math.abs(a-b)<0.000001

    private fun isFinished():Boolean{
        if(currentStatus == STATUS_INIT)
            return true
        if((view.visibility == View.GONE||view.visibility== View.INVISIBLE) && currentStatus == STATUS_FADE_OUT)
            return true
        if(currentStatus == STATUS_FADE_IN && isFloatEqual(currentPos,1.0F))
            return true
        if(currentStatus == STATUS_FADE_OUT && isFloatEqual(currentPos,0.0F))
            return true
        return false
    }

    private fun checkFinished(){
        if(isFinished()){
//            Log.i(javaClass.name,"isFinished - true")
            if(currentStatus == STATUS_FADE_IN){
                view.visibility = View.VISIBLE
            }else if(currentStatus == STATUS_FADE_OUT){
                view.visibility = View.GONE
            }
            view.clearAnimation()
            currentStatus = STATUS_INIT
        }else {
//            Log.i(javaClass.name,"isFinished - false")
            if(view.animation==null || (view.animation!=null && view.animation.hasEnded())){
                addAnimate()
            }
        }
    }

    fun setVisibleWithAnimate(isVisible:Boolean){
        if(isVisible){
            currentStatus = STATUS_FADE_IN
            view.visibility = View.VISIBLE
        }else{
            currentStatus = STATUS_FADE_OUT
        }
        checkFinished()
    }

    fun setVisibleImmediately(isVisible:Boolean){
        currentStatus = STATUS_INIT
        currentPos = if(isVisible) 1.0F else 0.0F
        view.clearAnimation()
        view.visibility = if(isVisible) View.VISIBLE else View.GONE
    }
}