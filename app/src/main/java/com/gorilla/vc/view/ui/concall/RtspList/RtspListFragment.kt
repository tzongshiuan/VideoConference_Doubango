package com.gorilla.vc.view.ui.concall.RtspList

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gorilla.vc.R
import com.gorilla.vc.databinding.ConcallRtspListFragmentBinding
import com.gorilla.vc.di.Injectable
import com.gorilla.vc.model.ParticipantStatus
import com.gorilla.vc.model.StreamingContent
import com.gorilla.vc.model.StreamingContentStatus
import com.gorilla.vc.model.VcManager
import com.gorilla.vc.utils.VcViewModelFactory
import com.gorilla.vc.view.ui.concall.ConcallViewModel
import javax.inject.Inject

class RtspListFragment : Fragment(), Injectable {

    private val TAG = RtspListFragment::class.simpleName

    private var mBinding: ConcallRtspListFragmentBinding? = null

    @Inject
    lateinit var concallViewModel: ConcallViewModel

    @Inject
    lateinit var vcManager: VcManager

    @Inject
    lateinit var factory: VcViewModelFactory

    private var isExamining = false

    private var lastParticipantStatus: ParticipantStatus? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        concallViewModel = ViewModelProviders.of(this, factory)
                                             .get(ConcallViewModel::class.java)

        // Observe rtsp list
        concallViewModel.rtspListLiveData.observe(this, Observer { rtspList ->
            (mBinding?.rtspList?.adapter as RtspListAdapter).rtspList = rtspList
            mBinding?.rtspList?.adapter?.notifyDataSetChanged()

            mBinding?.swipeRefreshLayout?.isRefreshing = false
        })

        concallViewModel.rtspIndexLiveData.observe(this, Observer { index ->
            (mBinding?.rtspList?.adapter as RtspListAdapter).rowIndex = index!!
            mBinding?.rtspList?.adapter?.notifyDataSetChanged()
        })

        concallViewModel.lastUpdateStatusLiveData.observe(this, Observer {
            if (lastParticipantStatus == null) {
                lastParticipantStatus = concallViewModel.getParticipantStatus()
            } else {
                val status = concallViewModel.getParticipantStatus()

                //if (status.streamingContent == StreamingContent.RTSP)
                if (isExamining) {
                    // Dismiss dialog
                    mBinding?.progress?.setVisibleImmediately(false)
                    isExamining = false

                    Log.d(TAG, "StreamingContentStatus: ${status?.streamingContentStatus}")
                    if (status?.streamingContentStatus == StreamingContentStatus.SUCCESS) {
                        Log.d(TAG, "RTSP is valid")
                        // rtsp is valid, close RTSP fragment
                        this.fragmentManager?.popBackStack()
                    } else {
                        Log.d(TAG, "RTSP is invalid")
                        // Else: Show dialog to notify user that this monitor is invalid
                        showRtspInvalidDialog()
                    }
                } else {
                    // continue to check RTSP status if playing RTSP
                    if (lastParticipantStatus?.streamingContent == StreamingContent.RTSP
                        && lastParticipantStatus?.streamingContentStatus == StreamingContentStatus.SUCCESS
                        && status?.streamingContent == StreamingContent.RTSP
                        && status.streamingContentStatus == StreamingContentStatus.FAILED) {
                        Log.d(TAG, "RTSP has disconnected")
                        showRtspDisconnectDialog()
                    }
                }

                lastParticipantStatus = status
            }
        })

        mBinding?.swipeRefreshLayout?.isRefreshing = true
        concallViewModel.getMyRtspList(vcManager.userId!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = ConcallRtspListFragmentBinding.inflate(inflater, container, false)

        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mBinding?.rtspList?.layoutManager = layoutManager

        val adapter = RtspListAdapter(vcManager, concallViewModel)
        mBinding?.rtspList?.adapter = adapter

        // Simple add listeners here
        mBinding?.cancelBtn?.setOnClickListener {
            resetAdapterSetting()
            this.fragmentManager?.popBackStack()
        }

        mBinding?.confirmBtn?.setOnClickListener {
            val listAdapter = (mBinding?.rtspList?.adapter as RtspListAdapter)

            if (listAdapter.rtspList.isNullOrEmpty() || listAdapter.rowIndex == -1) {
                resetAdapterSetting()
                this.fragmentManager?.popBackStack()
            } else {
                // Show examining RTSP url waiting dialog
                mBinding?.progress?.setVisibleWithAnimate(true)
                isExamining = true

                if (!concallViewModel.playSelectedRtspSource()) {
                    mBinding?.progress?.setVisibleImmediately(false)
                    isExamining = false
                }

                resetAdapterSetting()
            }
        }

        mBinding?.swipeRefreshLayout?.setOnRefreshListener {
            if (mBinding?.swipeRefreshLayout?.isRefreshing!!) {
                resetAdapterSetting()
                (mBinding?.rtspList?.adapter as RtspListAdapter).rtspList?.clear()
                mBinding?.rtspList?.adapter?.notifyDataSetChanged()

                concallViewModel.getMyRtspList(vcManager.userId!!)
            }
        }

        return mBinding?.root
    }

    private fun resetAdapterSetting() {
        //val adapter = (mBinding?.rtspList?.adapter as RtspListAdapter)
        //adapter.rowIndex = -1
        concallViewModel.setRtspListIndex(-1)
    }

    private fun showRtspInvalidDialog() {
        // Show hint dialog to check whether user want to leave conall
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context?.resources?.getString(R.string.alert_rtsp_title))
        builder.setMessage(context?.resources?.getString(R.string.alert_rtsp_invalid_message))
        builder.setCancelable(true)

        builder.setPositiveButton(context?.resources?.getString(R.string.confirm)) { _, _ ->
        }

        builder.create().show()
    }

    private fun showRtspDisconnectDialog() {
        // Show hint dialog to check whether user want to leave conall
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context?.resources?.getString(R.string.alert_rtsp_title))
        builder.setMessage(context?.resources?.getString(R.string.alert_rtsp_disconnect_message))
        builder.setCancelable(true)

        builder.setPositiveButton(context?.resources?.getString(R.string.confirm)) { _, _ ->
        }

        builder.create().show()
    }
}