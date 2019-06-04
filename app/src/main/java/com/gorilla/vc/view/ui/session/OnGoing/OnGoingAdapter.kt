package com.gorilla.vc.view.ui.session.OnGoing

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gorilla.vc.R
import com.gorilla.vc.databinding.OnGoingSessionItemBinding
import com.gorilla.vc.model.OnGoingVcSession
import com.gorilla.vc.model.VcManager
import com.gorilla.vc.view.ui.concall.ConcallActivity
import org.doubango.ngn.media.NgnMediaType
import org.doubango.ngn.sip.NgnAVSession
import org.doubango.ngn.utils.NgnStringUtils
import org.doubango.ngn.utils.NgnUriUtils
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class OnGoingAdapter(private val vcManager: VcManager) : RecyclerView.Adapter<OnGoingAdapter.ViewHolder>() {

    val tag = OnGoingAdapter::class.simpleName

    private var lastSession: OnGoingVcSession ?= null

    private var mCurrentTime: Date = Calendar.getInstance().time
    var items: ArrayList<OnGoingVcSession>? = null
        set(value) {
            mCurrentTime = Calendar.getInstance().time
            field = value
        }

    override fun onBindViewHolder(holder: OnGoingAdapter.ViewHolder, position: Int) {
        Log.d(OnGoingAdapter::javaClass.name, "onBindViewHolder(), position = $position")
        holder.bind(items!![position], mCurrentTime)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(OnGoingAdapter::javaClass.name, "onCreateViewHolder()")
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = OnGoingSessionItemBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items?.size ?: 0

    inner class ViewHolder(private val binding: OnGoingSessionItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OnGoingVcSession, currentTime: Date) {
            val times = getRestTime(item.startDate, currentTime, binding.root.context)
            binding.session = item
            binding.time = times[0]
            binding.timeUnit = times[1]
            binding.people = getOnlinePeople(item)
            binding.executePendingBindings()

            binding.enterBtn.setOnClickListener {
                register(itemView, item)
            }
        }

        private fun getOnlinePeople(item: OnGoingVcSession): String {
            val list = item.participants ?: return 0.toString()
            var count = 0
            for (p in list) {
                count += (if (p.isOnline == 1) 1 else 0)
            }
            return if (count > 99) "99+" else count.toString()
        }

        private fun diffTime(date1: Date, date2: Date, type: Int): Int {
            val c1 = Calendar.getInstance()
            val c2 = Calendar.getInstance()
            c1.time = date1
            c2.time = date2
            return c1.get(type) - c2.get(type)
        }

        private fun getRestTime(time: String?, currentTime: Date, context: Context): Array<String> {
            val inSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
            val date: Date?
            try {
                date = inSdf.parse(time)
            } catch (ex: Exception) {
                return arrayOf(0.toString(), context.getString(R.string.minutes))
            }

            if (currentTime.time < date.time) {
                return arrayOf(0.toString(), context.getString(R.string.minutes))
            }

            val year = diffTime(currentTime, date!!, Calendar.YEAR)
            if (year != 0)
                return arrayOf(year.toString(), context.getString(R.string.years))

            val month = diffTime(currentTime, date, Calendar.MONTH)
            if (month != 0)
                return arrayOf(month.toString(), context.getString(R.string.months))

            val day = diffTime(currentTime, date, Calendar.DAY_OF_MONTH)
            if (day != 0)
                return arrayOf(day.toString(), context.getString(R.string.days))

            val hours = diffTime(currentTime, date, Calendar.HOUR_OF_DAY)
            if (hours != 0)
                return arrayOf(hours.toString(), context.getString(R.string.hours))

            val minute = diffTime(currentTime, date, Calendar.MINUTE)
            return arrayOf(minute.toString(), context.getString(R.string.minutes))

        }
    }

    fun register(view: View, session: OnGoingVcSession) {
        Log.d(tag, "register()")

        val context = view.context
        lastSession = session

        // do SIP register
        vcManager.sipInfo = session.sip
        vcManager.sessionInfo = session
        vcManager.startEngine()

        val isRegisterSuccess = vcManager.register()

        if (isRegisterSuccess) {
            // TODO do something?
        } else {
            showErrorMessage(context, context.getString(R.string.wrong_server_info))
        }

        //if (!vcManager.mEngine.sipService.isRegistered) {
        //    makeCall(context)
        //}
    }

    fun makeCall(context: Context) {
        if (!vcManager.mEngine.sipService.isRegistered) {
            Log.d(tag, "Haven't registered")
            showErrorMessage(context, context.getString(R.string.have_not_register))
            return
        }

        if (NgnStringUtils.isNullOrEmpty(lastSession?.id)) {
            Log.d(tag, "Have empty session ID")
            showErrorMessage(context, context.getString(R.string.have_empty_sid))
            return
        }

        val remoteUri = NgnUriUtils.makeValidSipUri("*${lastSession?.id}")
        if (remoteUri == null) {
            Log.d(tag, "Failed to normalize sip uri")
            showErrorMessage(context, context.getString(R.string.normal_url_fail))
            return
        }

        Log.d(tag, "Make call to session, uri: $remoteUri")

        val avSession = NgnAVSession.createOutgoingSession(vcManager.mEngine.sipService.sipStack, NgnMediaType.AudioVideoSecondary)
        avSession.remotePartyUri = remoteUri

        // go to ConcallActivity
        val intent = Intent(context, ConcallActivity::class.java)
        intent.putExtra("id", avSession.id.toString())
        context.startActivity(intent)

        val activeCall = NgnAVSession.getFirstActiveCallAndNot(avSession.id)
        activeCall?.holdCall()

        avSession.makeCall(remoteUri)
    }

    private fun showErrorMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}