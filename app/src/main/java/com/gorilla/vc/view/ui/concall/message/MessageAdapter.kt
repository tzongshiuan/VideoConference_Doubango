package com.gorilla.vc.view.ui.concall.message

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.gorilla.vc.R
import com.gorilla.vc.databinding.ConcallMessageItemMineBinding
import com.gorilla.vc.databinding.ConcallMessageItemOthersBinding
import com.gorilla.vc.databinding.ConcallMessageItemSystemBinding
import com.gorilla.vc.model.ChatMessage
import com.gorilla.vc.model.VcManager

class MessageAdapter(private val vaManager: VcManager) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    var chatList: ArrayList<ChatMessage>? = null

    companion object {
        private const val TYPE_MINE = 0
        private const val TYPE_OTHERS = 1
        private const val TYPE_SYSTEM = 2

        @JvmStatic
        @BindingAdapter("showControlImgSrc")
        fun showControlImgSrc(view: ImageView, isExpandControlView: Boolean) {
            if (isExpandControlView) {
                view.setImageResource(R.mipmap.ic_arrow_left)
            } else {
                view.setImageResource(R.mipmap.ic_arrow_right)
            }
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, viewType: Int): MessageAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(p0.context)
        return when (viewType) {
            TYPE_SYSTEM -> SystemViewHolder(ConcallMessageItemSystemBinding.inflate(layoutInflater, p0, false))
            TYPE_MINE -> MineViewHolder(ConcallMessageItemMineBinding.inflate(layoutInflater, p0, false))
            else -> OthersViewHolder(ConcallMessageItemOthersBinding.inflate(layoutInflater, p0, false))
        }
    }

    override fun getItemCount() = chatList?.size ?: 0

    override fun onBindViewHolder(viewHolder: MessageAdapter.ViewHolder, p1: Int) = viewHolder.bind(chatList!![p1])

    override fun getItemViewType(position: Int): Int {
        return when {
            chatList!![position].isSystemMessage -> TYPE_SYSTEM
            chatList!![position].id!! == vaManager.userId!! -> TYPE_MINE
            else -> TYPE_OTHERS
        }
    }

    abstract inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        abstract fun bind(chatMessage: ChatMessage)
    }

    inner class SystemViewHolder(private val binding: ConcallMessageItemSystemBinding): ViewHolder(binding.root) {
        override fun bind(chatMessage: ChatMessage) {
            binding.message = chatMessage
            binding.executePendingBindings()
        }

    }

    inner class MineViewHolder(private val binding: ConcallMessageItemMineBinding): ViewHolder(binding.root) {
        override fun bind(chatMessage: ChatMessage) {
            binding.message = chatMessage
            binding.executePendingBindings()
        }

    }

    inner class OthersViewHolder(private val binding: ConcallMessageItemOthersBinding): ViewHolder(binding.root) {
        override fun bind(chatMessage: ChatMessage) {
            binding.message = chatMessage
            binding.executePendingBindings()
        }

    }

}