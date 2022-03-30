package com.locationpicker.sample.features.splash.fragmentes

import android.content.Intent
import android.view.View
import com.softartch_lib.component.fragment.BaseFragment
import com.locationpicker.sample.R
import com.locationpicker.sample.features.location.activities.LocationAddress

// by moahmed abdelnaser4-7-2019

class FragmentSplash : BaseFragment() {

    override fun layoutResource(): Int =R.layout.fragment_splash

    override fun onViewInflated(parentView: View, childView: View) {
        super.onViewInflated(parentView, childView)

      //  moveToHome()

    }
    fun moveToHome(){
        val  intent=Intent(requireContext(),LocationAddress::class.java)
        requireActivity().overridePendingTransition(0,0)
        startActivity(intent)
        requireActivity().overridePendingTransition(0,0)
        requireActivity().finish()
    }
}
