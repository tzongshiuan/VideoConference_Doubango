package com.gorilla.vc.view.ui.session.reserve

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gorilla.vc.R
import com.gorilla.vc.databinding.ResvereFragmentBinding
import com.gorilla.vc.di.Injectable
import com.gorilla.vc.model.ReserveVcSession
import com.gorilla.vc.model.Status
import com.gorilla.vc.model.VcManager
import com.gorilla.vc.utils.VcViewModelFactory
import com.gorilla.vc.utils.apiLiveData.ApiLiveData
import com.gorilla.vc.utils.apiLiveData.ApiObserver
import com.gorilla.vc.view.customized.customSearchView.SearchMethod
import java.io.UncheckedIOException
import javax.inject.Inject

class ReserveFragment : Fragment(), Injectable {

    private var mBinding : ResvereFragmentBinding? = null
    private lateinit var reserveViewModel: ReserveViewModel

    @Inject
    lateinit var vcManager: VcManager

    @Inject
    lateinit var factory: VcViewModelFactory

    @Suppress("UNCHECKED_CAST")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        reserveViewModel = ViewModelProviders.of(this, factory)
                .get(ReserveViewModel::class.java)

        mBinding?.apiLiveData = reserveViewModel.apiLiveData as ApiLiveData<Any>

        reserveViewModel.apiLiveData.observe(this,object : ApiObserver<ArrayList<ReserveVcSession>>{
            override fun onApiCallBack(status: Status, data: ArrayList<ReserveVcSession>?) {
                mBinding?.swipeRefreshLayout?.isRefreshing = (status == Status.RUNNING)
                mBinding?.apiLiveData = reserveViewModel.apiLiveData as ApiLiveData<Any>
                if(status!=Status.RUNNING)
                    loadList(data)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = ResvereFragmentBinding.inflate(inflater, container, false)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mBinding?.recyclerView?.layoutManager = layoutManager
        mBinding?.recyclerView?.adapter = ReserveAdapter()

        mBinding?.searchView?.queryHint = getString(R.string.hint_search)
        mBinding?.searchView?.isSubmitButtonEnabled = true
        mBinding?.searchView?.onActionViewExpanded()
        mBinding?.searchView?.clearFocus()

        mBinding?.swipeRefreshLayout?.setOnRefreshListener {
            vcManager.userId = null

            if(!reserveViewModel.apiLiveData.callApi(activity!!)){
                mBinding?.swipeRefreshLayout?.isRefreshing = false
            }
        }
        return mBinding?.root
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadList(data: ArrayList<ReserveVcSession>?){
        (mBinding?.recyclerView?.adapter as ReserveAdapter).items = data
        mBinding?.recyclerView?.adapter?.notifyDataSetChanged()
        mBinding?.recyclerView?.scheduleLayoutAnimation()
        if(data!=null) {
            mBinding?.searchView?.setSearchRecourse(data.clone() as ArrayList<ReserveVcSession>, mSearchMethod)
        }
        mBinding?.searchView?.setQuery("",false)
    }

    override fun onResume() {
        super.onResume()
        reserveViewModel.apiLiveData.callApi(activity!!)
    }

    private val mSearchMethod = object :SearchMethod<ReserveVcSession>{
        override fun isMatchForSummit(keyWord: String, data: ReserveVcSession)
                = isMatchForSuggestion(keyWord, data)

        override fun isMatchForSuggestion(keyWord: String, data: ReserveVcSession)
                = data.name?.toLowerCase()?.contains(keyWord.toLowerCase()) ?: false

        override fun getSearchSuggestionText(keyword: String, data: ReserveVcSession): SpannableString {
            val spannableString = SpannableString(data.name)
            val colorSpan = ForegroundColorSpan(Color.RED)
            val index = data.name?.toLowerCase()?.indexOf(keyword.toLowerCase(), 0) ?: 0
            spannableString.setSpan(colorSpan, index, index + keyword.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            return spannableString
        }

        override fun onSummit() {

        }

        override fun onSearchResult(keyword: String, list: ArrayList<ReserveVcSession>) {
            (mBinding?.recyclerView?.adapter as ReserveAdapter).items?.clear()
            (mBinding?.recyclerView?.adapter as ReserveAdapter).items?.addAll(list)
            mBinding?.recyclerView?.adapter?.notifyDataSetChanged()
        }
    }
}