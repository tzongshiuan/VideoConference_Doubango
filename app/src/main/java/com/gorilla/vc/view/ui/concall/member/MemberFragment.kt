package com.gorilla.vc.view.ui.concall.member

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gorilla.vc.databinding.ConcallMemberFragmentBinding
import com.gorilla.vc.di.Injectable
import com.gorilla.vc.model.VcManager
import com.gorilla.vc.utils.VcViewModelFactory
import com.gorilla.vc.view.ui.concall.ConcallViewModel
import javax.inject.Inject

class MemberFragment : Fragment(), Injectable {

    private var mBinding:ConcallMemberFragmentBinding? = null

    @Inject
    lateinit var concallViewModel: ConcallViewModel

    @Inject
    lateinit var vcManager: VcManager

    @Inject
    lateinit var factory: VcViewModelFactory

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        concallViewModel = ViewModelProviders.of(this, factory)
                                             .get(ConcallViewModel::class.java)

        concallViewModel.lastUpdateStatusLiveData.observe(this, Observer { updateStatus ->
            (mBinding?.memberList?.adapter as MemberAdapter).updateStatus = updateStatus
            mBinding?.memberList?.adapter?.notifyDataSetChanged()
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = ConcallMemberFragmentBinding.inflate(inflater, container, false)

        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mBinding?.memberList?.layoutManager = layoutManager

        val adapter = MemberAdapter(vcManager, concallViewModel)
        mBinding?.memberList?.adapter = adapter

        return mBinding?.root
    }
}