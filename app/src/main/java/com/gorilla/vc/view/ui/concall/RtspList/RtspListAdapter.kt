package com.gorilla.vc.view.ui.concall.RtspList

import android.content.Context
import android.databinding.BindingAdapter
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.gorilla.vc.R
import com.gorilla.vc.databinding.RtspListItemBinding
import com.gorilla.vc.model.RtspInfo
import com.gorilla.vc.model.VcManager
import com.gorilla.vc.view.ui.concall.ConcallViewModel

class RtspListAdapter(private val vcManager: VcManager, private val viewModel: ConcallViewModel) : RecyclerView.Adapter<RtspListAdapter.ViewHolder>() {

    val tag = RtspListAdapter::class.simpleName

    var rtspList: ArrayList<RtspInfo>? = null

    var rowIndex = -1

    override fun onBindViewHolder(holder: RtspListAdapter.ViewHolder, position: Int) {
        Log.d(RtspListAdapter::javaClass.name, "onBindViewHolder(), position = $position")
        holder.bind(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(RtspListAdapter::javaClass.name, "onCreateViewHolder()")
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = RtspListItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = rtspList?.size ?: 0

    inner class ViewHolder(private val binding: RtspListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val rtspInfo = rtspList!![position]
            when {
                rowIndex == position -> binding.backgroundStatus = BACK_SELECTED
                position % 2 == 0 -> binding.backgroundStatus = BACK_ODD
                else -> binding.backgroundStatus = BACK_EVEN
            }

            binding.rtspName = rtspInfo.name
            binding.rtspLocation = rtspInfo.location
            binding.rtspIp = rtspInfo.cameraIp
            binding.rtspDescription = rtspInfo.description
            binding.executePendingBindings()

            binding.rowLayout.setOnClickListener {
                viewModel.setRtspListIndex(position)
            }
        }
    }

    private fun showAlertToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        val BACK_ODD = 0
        val BACK_EVEN = 1
        val BACK_SELECTED = 2

        @JvmStatic
        @BindingAdapter("convertLayoutBackground")
        fun convertLayoutBackground(view: LinearLayout, backgroundStatus: Int) {
            when (backgroundStatus) {
                BACK_ODD -> {
                    view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.activity_background))
                }

                BACK_EVEN -> {
                    view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.gray))
                }

                BACK_SELECTED -> {
                    view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.deep_sky_blue))
                }
            }
        }
    }
}
