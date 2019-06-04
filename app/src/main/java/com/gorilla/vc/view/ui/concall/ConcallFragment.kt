package com.gorilla.vc.view.ui.concall

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gorilla.vc.R

class ConcallFragment : Fragment() {

    companion object {
        fun newInstance() = ConcallFragment()
    }

    private lateinit var viewModel: ConcallViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_concall, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ConcallViewModel::class.java)
    }

}
