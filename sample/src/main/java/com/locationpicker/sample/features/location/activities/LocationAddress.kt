package com.locationpicker.sample.features.location.activities

import android.os.Bundle
import com.locationpicker.sample.R
import com.locationpicker.sample.base.activity.BaseActivity

class LocationAddress: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
/*
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            fragmentTransaction.remove(prev)
        }
        fragmentTransaction.addToBackStack(null)

        val dialogFragment = LocationPicker() //here MyDialog is my custom dialog
        dialogFragment.show(supportFragmentManager, "dialog")*/
    }

/*
    class LocationPicker : LocationPickerDialog() {
        override fun layoutResource(): Int = R.layout.location_dialog

        override fun mapViewResource(): MapView = mapView

        override fun setGoogleAPIKEY(): String = "AIzaSyAU6Pf-8uWRgWcDyfaCdKgw-uINqGIsi3E"

        override fun onStart() {
            super.onStart()
            requireDialog().window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )

        }


        override fun onViewInflated(parentView: View, childView: View) {
            super.onViewInflated(parentView, childView)
            requireDialog().window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }

        override fun onGetLocationAddress(locationAddress: LocationAddress) {
            super.onGetLocationAddress(locationAddress)
        }
    }
*/
}