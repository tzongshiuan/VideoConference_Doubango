package com.gorilla.vc.view.ui.concall.message

import android.databinding.BindingAdapter
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import com.gorilla.vc.databinding.MessageParticipantListItemBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.gorilla.vc.R
import com.gorilla.vc.model.PrivateMessageUser
import com.gorilla.vc.model.VcManager
import com.gorilla.vc.view.ui.concall.ConcallViewModel

class ControlAdapter(private val vaManager: VcManager, private val viewModel: ConcallViewModel) : RecyclerView.Adapter<ControlAdapter.UserViewHolder>() {

    private val tag = ControlAdapter::class.simpleName

    var participantList : ArrayList<PrivateMessageUser>? = null

    var rowIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UserViewHolder(MessageParticipantListItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun getItemCount() = participantList?.size ?: 0

    override fun onBindViewHolder(viewHolder : UserViewHolder, position: Int) {
        viewHolder.bind(position)
    }

    inner class UserViewHolder(private val binding: MessageParticipantListItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val user = participantList!![position]
            binding.name = user.name

            when {
                (position == 0) -> binding.onlineStatus = STATUS_SYSTEM
                user.isOnline -> binding.onlineStatus = STATUS_ONLINE
                else -> binding.onlineStatus = STATUS_OFFLINE
            }

            when {
                (rowIndex == position) -> binding.backgroundStatus = BACK_SELECTED
                user.isHaveUnreadMessage -> binding.backgroundStatus = BACK_UNREAD
                else -> binding.backgroundStatus = BACK_DEFAULT
            }
            binding.executePendingBindings()

            binding.rowLayout.setOnClickListener {
                viewModel.changeSelectUserIndex(position)
            }
        }
    }

    companion object {
        val BACK_DEFAULT  = 0
        val BACK_SELECTED = 1
        val BACK_UNREAD   = 2

        val STATUS_SYSTEM  = 0
        val STATUS_ONLINE  = 1
        val STATUS_OFFLINE = 2

        @JvmStatic
        @BindingAdapter("convertLayoutBackground")
        fun convertLayoutBackground(view: ConstraintLayout, backgroundStatus: Int) {
            when (backgroundStatus) {
                BACK_DEFAULT -> {
                    view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.activity_background))
                }

                BACK_SELECTED -> {
                    view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.deep_sky_blue))
                }

                BACK_UNREAD -> {
                    view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.message_unread))
                }
            }
        }

        @JvmStatic
        @BindingAdapter("convertOnlineStatus")
        fun convertOnlineStatus(view: ImageView, onlineStatus: Int) {
            when (onlineStatus) {
                STATUS_SYSTEM -> {
                    view.visibility = View.GONE
                }

                STATUS_ONLINE -> {
                    view.setImageResource(R.mipmap.ic_online)
                }

                STATUS_OFFLINE -> {
                    view.setImageResource(R.mipmap.ic_offline)
                }
            }
        }
    }
}