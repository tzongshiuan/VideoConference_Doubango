package com.gorilla.vc.view.ui.session

import android.app.AlertDialog
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.app.AppCompatActivity
import android.util.Log

import com.gorilla.vc.R
import com.gorilla.vc.databinding.SessionActivityBinding
import com.gorilla.vc.di.Injectable
import com.gorilla.vc.view.ui.session.OnGoing.OnGoingFragment
import com.gorilla.vc.view.ui.session.reserve.ReserveFragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat


class SessionActivity : AppCompatActivity(), HasSupportFragmentInjector, Injectable {

    val tag = SessionActivity::class.simpleName

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    var mBinding: SessionActivityBinding? = null

    private val PERMISSION_ALL = 1
    private val mPermissions = arrayOf(
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.session_activity)
        setupViewPager()
        mBinding?.tabs?.setupWithViewPager(mBinding?.viewpager)

        if(!hasPermissions()){
            ActivityCompat.requestPermissions(this, mPermissions, PERMISSION_ALL)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onBackPressed() {
        Log.d(tag, "onBackPressed()")

        showLogoutDialog()
    }

    private fun hasPermissions(): Boolean {
        for (permission in mPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_ALL -> {
                var isAllGrant = true
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        isAllGrant = false
                        break
                    }
                }

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && isAllGrant) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    showPermissionDialog()
                }
                return
            }
        }

        // other 'case' lines to check for other
        // permissions this app might request
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert_logout_title))
        builder.setMessage(resources.getString(R.string.alert_logout_message))
        builder.setCancelable(true)

        builder.setNegativeButton(resources.getString(R.string.exit)) { _, _ ->
            this.finish()
        }

        builder.setPositiveButton(resources.getString(R.string.cont)) { _, _ ->
            // Nothing need to do
        }

        builder.create().show()
    }

    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert_permission_title))
        builder.setMessage(resources.getString(R.string.alert_permission_message))
        builder.setCancelable(false)

        builder.setPositiveButton(resources.getString(R.string.confirm)) { _, _ ->
            this.finish()
        }

        builder.create().show()
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(ReserveFragment(), getString(R.string.reserve_session))
        adapter.addFragment(OnGoingFragment(), getString(R.string.ongoing_session))
        mBinding?.viewpager?.adapter = adapter
    }

    class ViewPagerAdapter(fm: FragmentManager?) : FragmentPagerAdapter(fm) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()
        override fun getItem(p0: Int): Fragment = mFragmentList[p0]

        override fun getCount(): Int = mFragmentList.size

        override fun getPageTitle(position: Int): CharSequence? = mFragmentTitleList[position]

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector
}