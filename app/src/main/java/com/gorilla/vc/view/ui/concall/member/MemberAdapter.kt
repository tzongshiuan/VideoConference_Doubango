package com.gorilla.vc.view.ui.concall.member

import android.app.AlertDialog
import android.content.Context
import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import com.gorilla.vc.R
import com.gorilla.vc.databinding.MemberListItemBinding
import com.gorilla.vc.model.*
import com.gorilla.vc.view.ui.concall.ConcallViewModel

class MemberAdapter(private val vcManager: VcManager, private val viewModel: ConcallViewModel) : RecyclerView.Adapter<MemberAdapter.ViewHolder>() {

    val tag = MemberAdapter::class.simpleName

    private var items: ArrayList<ParticipantStatus>? = null

    var videoCount = 0
    var updateStatus: UpdateStatus? = null
        set(value) {
            videoCount = VIDEO_NONE
            items = value?.participants
            field = value
        }

    override fun onBindViewHolder(holder: MemberAdapter.ViewHolder, position: Int) {
        Log.d(MemberAdapter::javaClass.name, "onBindViewHolder(), position = $position")
        holder.bind(items!![position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(MemberAdapter::javaClass.name, "onCreateViewHolder()")
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = MemberListItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items?.size ?: 0

    inner class ViewHolder(private val binding: MemberListItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ParticipantStatus) {
            // To decide video image source
            binding.isVideoActive = (item.id == updateStatus?.presenter1 && updateStatus?.presentMode != PresentMode.NONE_HOST)
                                    || (item.id == updateStatus?.presenter2 && updateStatus?.presentMode == PresentMode.DUAL_HOST)
            if (binding.isVideoActive) {
                videoCount++
            }

            if (item.id == vcManager.userId?.toInt()) {
                viewModel.setVideoActivateStatus(binding.isVideoActive)
            }

            // To decide host image source
            binding.isHostActive = (item.id == updateStatus?.host)

            binding.name = vcManager.getNameFromId(item.id?.toString())
            binding.executePendingBindings()

            val isIamHost = (updateStatus?.host == vcManager.userId?.toInt())

            binding.videoBtn.setOnClickListener {
                if (isIamHost) {
                    // show pop menu to control speaking video View(center, left, right)
                    showChangeVideoPopMenu(it, item.id, binding.isVideoActive)
                } else {
                    showAlertToast(it.context, it.context.getString(R.string.hint_change_speaker))
                }
            }

            binding.hostBtn.setOnClickListener {
                if (isIamHost) {
                    if (!binding.isHostActive) {
                        // show dialog to notify user whether to change host
                        showChangeHostDialog(it.context, item.id)
                    }
                } else {
                    showAlertToast(it.context, it.context.getString(R.string.hint_change_host))
                }
            }

            if (item.id == vcManager.userId?.toInt()) {
                binding.userVideo.visibility = View.INVISIBLE
                binding.userAudio.visibility = View.INVISIBLE
                binding.userChat.visibility = View.INVISIBLE
            } else {
                binding.userVideo.visibility = View.VISIBLE
                binding.userAudio.visibility = View.VISIBLE
                binding.userChat.visibility = View.VISIBLE

                // about controlling user video, user mic and user chat features
                binding.isEnableVideo = item.isCamEnabled!!
                binding.isEnableAudio = item.isMicEnabled!!

                // setOnClickListener
                binding.userVideo.setOnClickListener {
                    if (isIamHost) {
                        // change status to !binding.isEnableVideo
                        viewModel.setUserVideoAudio(item.id!!, !binding.isEnableVideo, binding.isEnableAudio)
                    } else {
                        showAlertToast(it.context, it.context.getString(R.string.hint_change_video_status))
                    }
                }

                binding.userAudio.setOnClickListener {
                    if (isIamHost) {
                        // change status to !binding.isEnableAudio
                        viewModel.setUserVideoAudio(item.id!!, binding.isEnableVideo, !binding.isEnableAudio)
                    } else {
                        showAlertToast(it.context, it.context.getString(R.string.hint_change_audio_status))
                    }
                }

                binding.userChat.setOnClickListener {
                    // add into one-by-one chat list
                    viewModel.addMessageUserItem(PrivateMessageUser(item.id, binding.name))
                }
            }
        }
    }

    private fun showAlertToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showChangeHostDialog(context: Context, id: Int?) {
        val resources = context.resources
        val builder = AlertDialog.Builder(context)
        builder.setTitle(resources.getString(R.string.alert_change_title))
        builder.setCancelable(false)

        val message = String.format(
                resources.getString(R.string.alert_change_host), vcManager.getNameFromId(id.toString()))
        builder.setMessage(message)

        builder.setNegativeButton(resources.getString(R.string.cancel)) { _, _ ->
            // Do nothing
        }

        builder.setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
            // set this user as host
            viewModel.changeHost(id!!)
        }

        builder.create().show()
    }

    private fun showChangeVideoPopMenu(view: View, id: Int?, isVideoActive: Boolean) {
        if (id == null) {
            Log.d(tag, "Id can not be null")
            return
        }

        if (videoCount > VIDEO_DUAL) {
            Log.d(tag, "Invalid videoCount")
            return
        }

        val res = view.context.resources
        val popup = PopupMenu(view.context, view)
        val other = if (updateStatus?.presenter1 == id) updateStatus?.presenter2 else updateStatus?.presenter1

        if (other == null) {
            Log.d(tag, "other id can not be null")
            return
        }

        if (isVideoActive) {
            // delete or change position from speaking video
            when (videoCount) {
                VIDEO_SINGLE -> {
                    popup.menu.add(Menu.NONE, OPTION_INTERRUPT, Menu.NONE, res.getString(R.string.interrupt))
                    popup.setOnMenuItemClickListener {
                        when (it.itemId) {
                            OPTION_INTERRUPT -> viewModel.setPresenterNone()
                            else -> {}
                        }
                        true
                    }
                }
                VIDEO_DUAL -> {
                    popup.menu.add(Menu.NONE, OPTION_INTERRUPT, Menu.NONE, res.getString(R.string.interrupt))
                    popup.menu.add(Menu.NONE, OPTION_LEFT, Menu.NONE, res.getString(R.string.put_left))
                    popup.menu.add(Menu.NONE, OPTION_RIGHT, Menu.NONE, res.getString(R.string.put_right))
                    popup.setOnMenuItemClickListener {
                        when (it.itemId) {
                            OPTION_INTERRUPT -> viewModel.addPresenterSingle(other)
                            OPTION_LEFT -> viewModel.addPresenterDual(id, other, true)
                            OPTION_RIGHT -> viewModel.addPresenterDual(id, other, false)
                            else -> {}
                        }
                        true
                    }
                }
                else -> {
                    // Do nothing
                }
            }
        } else {
            // if still have position, add to speaking video
            when {
                videoCount == VIDEO_NONE -> {
                    popup.menu.add(Menu.NONE, OPTION_CENTER, Menu.NONE, res.getString(R.string.put_center))
                    popup.setOnMenuItemClickListener {
                        when (it.itemId) {
                            OPTION_CENTER -> viewModel.addPresenterSingle(id)
                            else -> {}
                        }
                        true
                    }
                }
                videoCount == VIDEO_SINGLE -> {
                    popup.menu.add(Menu.NONE, OPTION_CENTER, Menu.NONE, res.getString(R.string.put_center))
                    popup.menu.add(Menu.NONE, OPTION_LEFT, Menu.NONE, res.getString(R.string.put_left))
                    popup.menu.add(Menu.NONE, OPTION_RIGHT, Menu.NONE, res.getString(R.string.put_right))
                    popup.setOnMenuItemClickListener {
                        when (it.itemId) {
                            OPTION_CENTER -> viewModel.addPresenterSingle(id)
                            OPTION_LEFT -> viewModel.addPresenterDual(id, other,true)
                            OPTION_RIGHT -> viewModel.addPresenterDual(id, other,false)
                            else -> {}
                        }
                        true
                    }
                }
                videoCount >= VIDEO_DUAL -> showAlertToast(view.context, view.context.getString(R.string.already_two_speakers))
            }
        }

        popup.show()
    }

    companion object {
        const val VIDEO_NONE = 0
        const val VIDEO_SINGLE = 1
        const val VIDEO_DUAL = 2

        const val OPTION_INTERRUPT = 0
        const val OPTION_CENTER    = 1
        const val OPTION_LEFT      = 2
        const val OPTION_RIGHT     = 3

        @JvmStatic
        @BindingAdapter("convertVideo")
        fun convertVideo(view: ImageView, isVideoActive: Boolean) {
            if (isVideoActive) {
                view.setImageResource(R.mipmap.btn_video_n)
            } else {
                view.setImageResource(R.drawable.btn_video)
            }
        }

        @JvmStatic
        @BindingAdapter("convertHost")
        fun convertHost(view: ImageView, isHostActive: Boolean) {
            if (isHostActive) {
                view.setImageResource(R.mipmap.btn_hammer_n)
            } else {
                view.setImageResource(R.drawable.btn_hammer)
            }
        }

        @JvmStatic
        @BindingAdapter("convertUserVideoSrc")
        fun convertUserVideoSrc(view: ImageView, isEnableVideo: Boolean) {
            if (isEnableVideo) {
                view.setImageResource(R.mipmap.ic_video_n)
            } else {
                view.setImageResource(R.mipmap.ic_video_off_n)
            }
        }

        @JvmStatic
        @BindingAdapter("convertUserAudioSrc")
        fun convertUserAudioSrc(view: ImageView, isEnableAudio: Boolean) {
            if (isEnableAudio) {
                view.setImageResource(R.mipmap.ic_mic_n)
            } else {
                view.setImageResource(R.mipmap.ic_mic_off_n)
            }
        }
    }
}
