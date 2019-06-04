package com.gorilla.vc.view.ui.session.OnGoing

import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gorilla.vc.R
import com.gorilla.vc.databinding.OnGoingFragmentBinding
import com.gorilla.vc.di.Injectable
import com.gorilla.vc.model.*
import com.gorilla.vc.utils.VcViewModelFactory
import com.gorilla.vc.utils.apiLiveData.ApiLiveData
import com.gorilla.vc.utils.apiLiveData.ApiObserver
import org.doubango.ngn.events.NgnEventArgs
import org.doubango.ngn.events.NgnRegistrationEventArgs
import org.doubango.ngn.events.NgnRegistrationEventTypes
import javax.inject.Inject

class OnGoingFragment : Fragment(), Injectable {

    val TAG = OnGoingFragment::class.simpleName

    private var mBinding :OnGoingFragmentBinding?=null
    private lateinit var onGoingViewModel: OnGoingViewModel

    @Inject
    lateinit var vcManager: VcManager

    @Inject
    lateinit var factory: VcViewModelFactory

    private var mSipBroadCastRecv : BroadcastReceiver ?= null

    private var adapter: OnGoingAdapter ?= null

    @Suppress("UNCHECKED_CAST")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onGoingViewModel = ViewModelProviders.of(this, factory).get(OnGoingViewModel::class.java)
        onGoingViewModel.apiLiveData.observe(this,object :ApiObserver<ArrayList<OnGoingVcSession>>{
            override fun onApiCallBack(status: Status, data: ArrayList<OnGoingVcSession>?) {
                mBinding?.swipeRefreshLayout?.isRefreshing = (status == Status.RUNNING)
                mBinding?.apiLiveData = onGoingViewModel.apiLiveData as ApiLiveData<Any>
                if (!VcManager.DEBUG_CONCALL_BY_VIRTUAL_SESSION) {
                    if (status != Status.RUNNING)
                        loadList(data)
                }
                else {
                    //////////////////////////////////////////////////////////
                    Log.d(TAG, "Create fake session for test")
                    val sessions: ArrayList<OnGoingVcSession> = ArrayList()
                    val base = BaseVcSession()
                    base.id = "15000"
                    base.name = "VC_ANDROID_TEST"
                    base.password = "gorilla"
                    base.startDate = "2018-12-03T09:30:00+08:00"
                    base.endDate = "2018-12-03T21:30:00+08:00"
                    base.hostId = "1212"
                    base.creatorId = "1212"
                    base.recordDefault = 0
                    base.agenda = "Test Agenda"
                    base.status = 1
                    base.createDate = "2018-12-03T09:30:46.902+08:00"
                    val sipInfo = SessionSipInfo()
                    sipInfo.id = "1001"
                    sipInfo.proxyIp = vcManager.mPreferences.ip
                    sipInfo.proxyPort = "5060"
                    sipInfo.videoCodecs = "H.263"
                    sipInfo.audioCodecs = "mp3"
                    base.sip = sipInfo

                    val s = OnGoingVcSession(base)
                    s.hostName = "Hsuan"
                    sessions.add(s)
                    loadList(sessions)

                    vcManager.userId = "1212"
                    //////////////////////////////////////////////////////////
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = OnGoingFragmentBinding.inflate(inflater, container, false)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mBinding?.recyclerView?.layoutManager = layoutManager

        adapter = OnGoingAdapter(vcManager)
        mBinding?.recyclerView?.adapter = adapter

        mBinding?.swipeRefreshLayout?.setOnRefreshListener {
            vcManager.userId = null
            
            if(!onGoingViewModel.apiLiveData.callApi(activity!!)){
                mBinding?.swipeRefreshLayout?.isRefreshing = false
            }
        }

        return mBinding?.root
    }

    private fun loadList(data: ArrayList<OnGoingVcSession>?){
        (mBinding?.recyclerView?.adapter as OnGoingAdapter).items = data
        mBinding?.recyclerView?.adapter?.notifyDataSetChanged()
        mBinding?.recyclerView?.scheduleLayoutAnimation()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")

        vcManager.stopEngine()

        onGoingViewModel.apiLiveData.callApi(activity!!)

        if (mSipBroadCastRecv == null) {
            mSipBroadCastRecv = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action

                    // Registration Event
                    if (NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT == action) {
                        val args = intent.getParcelableExtra<NgnRegistrationEventArgs>(NgnEventArgs.EXTRA_EMBEDDED)
                        if (args == null) {
                            Log.d(TAG, "Invalid event args")
                            return
                        }

                        Log.d(TAG, "NgnRegistrationEventTypes: ${args.eventType}")
                        when (args.eventType) {
                            NgnRegistrationEventTypes.REGISTRATION_OK -> {
                                adapter?.makeCall(context)
                            }

                            NgnRegistrationEventTypes.UNREGISTRATION_OK -> {
                                // TODO show something about enter concall failed
                                Toast.makeText(context, context.getString(R.string.enter_session_fail), Toast.LENGTH_SHORT).show()
                            }

                            NgnRegistrationEventTypes.REGISTRATION_NOK,
                            NgnRegistrationEventTypes.REGISTRATION_INPROGRESS,
                            NgnRegistrationEventTypes.UNREGISTRATION_INPROGRESS,
                            NgnRegistrationEventTypes.UNREGISTRATION_NOK -> {
                            }

                            else -> {
                                Log.d(TAG, "Received unknown NgnRegistrationEventTypes")
                            }
                        }
                    }
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT)
        activity?.registerReceiver(mSipBroadCastRecv, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause()")

        if (mSipBroadCastRecv != null) {
            activity?.unregisterReceiver(mSipBroadCastRecv)
            mSipBroadCastRecv = null
        }
    }
}