package com.gorilla.vc.view.ui.login

import android.content.Context
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.gorilla.vc.R
import com.gorilla.vc.databinding.ActivityLogInBinding

class LogInActivityHandler(private val context: LogInActivity,
                           private val binding: ActivityLogInBinding?,
                           private val loginViewModel: LogInViewModel?,
                           private val tag: String?) {

    @Suppress("UNUSED_PARAMETER")
    fun onLogInBtnClick(view: View) {
        Log.d(tag, "onLogInBtnClick()")

        // to check whether any items are blank
        if (!checkItemsContent()) {
            return
        }

        loginViewModel?.startLogIn()
    }

    private fun showSoftKeyboard(view: View?) {
        view?.requestFocus()
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun checkItemsContent(): Boolean {
        if (binding?.accountText?.text.isNullOrEmpty()) {
            showEmptyMessage(context.getString(R.string.account_empty))
            showSoftKeyboard(binding?.accountText)
            return false
        }

        if (binding?.passwordText?.text.isNullOrEmpty()) {
            showEmptyMessage(context.getString(R.string.password_empty))
            showSoftKeyboard(binding?.passwordText)
            return false
        }

        if (binding?.addressText?.text.isNullOrEmpty()) {
            showEmptyMessage(context.getString(R.string.address_empty))
            showSoftKeyboard(binding?.addressText)
            return false
        }

        return true
    }

    private fun showEmptyMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}