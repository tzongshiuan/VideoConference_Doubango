package com.gorilla.vc.view.ui.login

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import com.gorilla.vc.BuildConfig
import com.gorilla.vc.R
import com.gorilla.vc.databinding.ActivityLogInBinding
import com.gorilla.vc.di.Injectable
import com.gorilla.vc.model.PreferencesHelper
import com.gorilla.vc.model.VcManager
import com.gorilla.vc.utils.VcViewModelFactory
import com.gorilla.vc.view.ui.session.SessionActivity
import kotlinx.android.synthetic.main.activity_log_in.*
import org.doubango.ngn.services.impl.NgnSipService
import javax.inject.Inject

class LogInActivity : AppCompatActivity() , Injectable {
    val tag = LogInActivity::class.simpleName

    private var binding: ActivityLogInBinding ?= null

    @Inject
    lateinit var preference: PreferencesHelper

    @Inject
    lateinit var vcManager: VcManager

    @Inject
    lateinit var factory: VcViewModelFactory

    /**
     * For log in
     */
    private var loginViewModel: LogInViewModel?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate()")

        binding = DataBindingUtil.setContentView(this, R.layout.activity_log_in)
        setContentView(binding?.root)

        // For convenient development
        VcManager.DEBUG_OPTIONS_ENABLE = BuildConfig.DEBUG
        if (VcManager.DEBUG_OPTIONS_ENABLE) {
            binding?.stunCheck?.visibility = View.VISIBLE
            NgnSipService.IS_ENABLE_STUN = VcManager.DEBUG_IS_ENABLE_STUN
            binding?.stunCheck?.isChecked = VcManager.DEBUG_IS_ENABLE_STUN
            binding?.stunCheck?.setOnCheckedChangeListener { _, isChecked ->
                VcManager.DEBUG_IS_ENABLE_STUN = isChecked
            }
            
            binding?.avChatApartCheck?.visibility = View.VISIBLE
            NgnSipService.IS_AV_CHAT_PORT_APART = VcManager.DEBUG_AV_CHAT_PORT_APART
            binding?.avChatApartCheck?.isChecked = VcManager.DEBUG_AV_CHAT_PORT_APART
            binding?.avChatApartCheck?.setOnCheckedChangeListener { _, isChecked ->
                VcManager.DEBUG_AV_CHAT_PORT_APART = isChecked
            }

            binding?.virtualSessionCheck?.visibility = View.VISIBLE
            binding?.virtualSessionCheck?.isChecked = VcManager.DEBUG_CONCALL_BY_VIRTUAL_SESSION
            binding?.virtualSessionCheck?.setOnCheckedChangeListener { _, isChecked ->
                VcManager.DEBUG_CONCALL_BY_VIRTUAL_SESSION = isChecked
            }

            binding?.qosInfoCheck?.visibility = View.VISIBLE
            binding?.qosInfoCheck?.isChecked = VcManager.DEBUG_QOS
            binding?.qosInfoCheck?.setOnCheckedChangeListener { _, isChecked ->
                VcManager.DEBUG_QOS = isChecked
            }
        } else {
            NgnSipService.IS_ENABLE_STUN = VcManager.DEBUG_IS_ENABLE_STUN
            NgnSipService.IS_AV_CHAT_PORT_APART = VcManager.DEBUG_AV_CHAT_PORT_APART
        }

        preference.readPreferences()

        initUI()
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume()")

        loginViewModel?.isLogging = false

        // clear here
        vcManager.userId = null
        vcManager.joinSessions.clear()
        vcManager.sipInfo = null
    }

    override fun onPause() {
        super.onPause()
        Log.d(tag, "onPause()")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "onStop()")

        loginViewModel?.startLogOut()
        preference.savePreferences()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy()")
    }

    private fun initUI() {
        initInputViews()
        initLogInViewModel()

        binding?.handler = LogInActivityHandler(this, binding, loginViewModel, tag)
    }

    /**
     * Initialize all input views
     */
    private fun initInputViews() {
        accountText.setText(preference.account)
        accountText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                preference.account = s.toString()
            }
        })

        passwordText.setText(preference.password)
        passwordText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                preference.password = s.toString()
            }
        })

        addressText.setText(preference.ip)
        addressText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                preference.ip = s.toString()
            }
        })
    }

    /**
     * Initialize log in ViewModel
     */
    private fun initLogInViewModel() {
        loginViewModel = ViewModelProviders.of(this, factory)
                .get(LogInViewModel::class.java)

        // Update the list when the data have changed
        loginViewModel?.getIsLoginObservable()?.observe(this, Observer<Boolean> { isLogin ->
            if (isLogin == true) {
                val intent = Intent(this, SessionActivity::class.java)
                this.startActivity(intent)
            } else {
                Toast.makeText(applicationContext, applicationContext.resources.getString(R.string.log_in_fail), Toast.LENGTH_SHORT).show()
            }
        })
    }
}
