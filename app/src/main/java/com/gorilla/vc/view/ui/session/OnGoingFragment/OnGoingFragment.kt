package com.gorilla.vc.view.ui.session.OnGoingFragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gorilla.vc.databinding.OnGoingFragmentBinding

class OnGoingFragment : Fragment(){

    private var mBinding :OnGoingFragmentBinding?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = OnGoingFragmentBinding.inflate(inflater, container, false)
        return mBinding?.root
    }
}