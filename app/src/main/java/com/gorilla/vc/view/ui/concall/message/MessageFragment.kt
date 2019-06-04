package com.gorilla.vc.view.ui.concall.message

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gorilla.vc.databinding.ConcallMessageFragmentBinding
import com.gorilla.vc.di.Injectable
import com.gorilla.vc.model.ChatStatus
import com.gorilla.vc.model.PrivateMessageUser
import com.gorilla.vc.model.VcManager
import com.gorilla.vc.utils.VcViewModelFactory
import com.gorilla.vc.view.ui.concall.ConcallViewModel
import javax.inject.Inject

class MessageFragment : Fragment(), Injectable {

    companion object {
        private val TAG = MessageFragment::class.simpleName
    }

    private var mBinding :ConcallMessageFragmentBinding? = null
    @Inject
    lateinit var vcManager: VcManager
    @Inject
    lateinit var factory: VcViewModelFactory

    @Inject
    lateinit var concallViewModel: ConcallViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        concallViewModel = ViewModelProviders.of(this, factory).get(ConcallViewModel::class.java)

        concallViewModel.lastUpdateStatusLiveData.observe(this, Observer { updateStatus ->
            if (updateStatus?.host == vcManager.userId?.toInt()) {
                mBinding?.messageLayout?.visibility = View.VISIBLE
            } else {
                mBinding?.messageLayout?.visibility = View.GONE
            }
        })

        concallViewModel.messageListLiveData.observe(this, Observer {
            (mBinding?.recyclerView?.adapter as MessageAdapter).chatList = it
            mBinding?.recyclerView?.adapter?.notifyDataSetChanged()
            mBinding?.recyclerView?.scrollToPosition(it!!.size-1)
        })

        concallViewModel.lastChatStatusLiveDate.observe(this, Observer<Int> { chatStatus ->
            when (chatStatus) {
                ChatStatus.ENABLE, ChatStatus.DEFAULT -> {
                    mBinding?.isMessageEnable = true
                    mBinding?.isChatSwitchChecked = true
                }
                ChatStatus.DISABLE -> {
                    mBinding?.isMessageEnable = false
                    mBinding?.isChatSwitchChecked  = false
                }
            }
        })

        concallViewModel.messageUserListLiveData.observe(this, Observer<ArrayList<PrivateMessageUser>> {
            Log.d(TAG, "Reload message user list")
            (mBinding?.participantList?.adapter as ControlAdapter).participantList = it
            mBinding?.participantList?.adapter?.notifyDataSetChanged()
        })
        concallViewModel.reloadMsgUserList()

        concallViewModel.newMessageUserLiveData.observe(this, Observer<PrivateMessageUser> {
            Log.d(TAG, "Observe new private chat user")
            if (it != null) {
                // Have already add item, because participantList is call by reference
                //(mBinding?.participantList?.adapter as ControlAdapter).participantList?.add(it)
                mBinding?.participantList?.adapter?.notifyDataSetChanged()
            }
        })

        concallViewModel.isExpandControlView.observe(this, Observer<Boolean> { isExpand ->
            isExpand?.let { mBinding?.isExpandControlView = it}
        })

        concallViewModel.chatUserIndexLiveData.observe(this, Observer<Int> {
            (mBinding?.participantList?.adapter as ControlAdapter).rowIndex = it!!
            mBinding?.participantList?.adapter?.notifyDataSetChanged()
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = ConcallMessageFragmentBinding.inflate(inflater, container, false)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mBinding?.recyclerView?.layoutManager = layoutManager
        mBinding?.recyclerView?.adapter = MessageAdapter(vcManager)
        mBinding?.send?.setOnClickListener {
            concallViewModel.sendChatMessage(mBinding?.text?.text.toString())
            mBinding?.text?.setText("")
        }

        // participantList RecyclerView
        val layoutManager2 = LinearLayoutManager(activity)
        mBinding?.participantList?.layoutManager = layoutManager2
        mBinding?.participantList?.adapter = ControlAdapter(vcManager, concallViewModel)

        // default with hiding control layout
        mBinding?.isExpandControlView = false

        mBinding?.showControlImg?.setOnClickListener{
            concallViewModel.toggleControlView()
        }

        mBinding?.chatSwitch?.setOnClickListener {
            concallViewModel.setChatRoomEnabled(mBinding?.isChatSwitchChecked!!)
        }

        return mBinding?.root
    }
}