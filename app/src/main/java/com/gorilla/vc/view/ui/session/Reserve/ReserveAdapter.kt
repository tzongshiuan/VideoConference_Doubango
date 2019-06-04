package com.gorilla.vc.view.ui.session.reserve

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.gorilla.vc.databinding.ResvereSessionItemBinding
import com.gorilla.vc.model.ReserveVcSession

class ReserveAdapter:RecyclerView.Adapter<ReserveAdapter.ViewHolder>() {

    var items: ArrayList<ReserveVcSession>?=null

    override fun onBindViewHolder(holder: ReserveAdapter.ViewHolder, position: Int) {
        Log.d(tag, "onBindViewHolder(), position = $position")
        holder.bind(items!![position])
    }

    private val tag = ReserveAdapter::class.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(tag, "onCreateViewHolder()")
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ResvereSessionItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items?.size ?: 0

    inner class ViewHolder(private val binding: ResvereSessionItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReserveVcSession) {
            this.binding.session = item
            this.binding.executePendingBindings()
        }
    }
}
