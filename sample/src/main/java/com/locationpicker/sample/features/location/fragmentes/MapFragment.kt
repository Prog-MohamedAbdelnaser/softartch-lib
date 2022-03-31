package com.locationpicker.sample.features.location.fragmentes

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.softartch_lib.domain.LocationAddress
import com.google.android.gms.maps.MapView
import com.locationpicker.sample.R
import com.softartch_lib.locationpicker.BaseMapFragment
import kotlinx.android.synthetic.main.fragment_location.*

class MapFragment : BaseMapFragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }
    //todo set your api key here
    override fun setGoogleAPIKEY(): String =""

    override fun mapViewResource(): MapView =mapView

    override fun layoutResource(): Int = R.layout.fragment_location


    override fun onMapSetup(map: GoogleMap?) {
        super.onMapSetup(map)
        map?.setOnMarkerClickListener {

         false}
        map?.uiSettings?.isMapToolbarEnabled=false
        map?.uiSettings?.isRotateGesturesEnabled=false
        map?.uiSettings?.isCompassEnabled=false
        map?.uiSettings?.isScrollGesturesEnabled=false
        map?.uiSettings?.isTiltGesturesEnabled=false
        map?.uiSettings?.isZoomControlsEnabled=false
        map?.uiSettings?.setAllGesturesEnabled(false)
    }

    override fun onViewInflated(parentView: View, childView: View) {
        super.onViewInflated(parentView, childView)

        //to fillter auto complete  search result
        // setSearchLocalizationFilter(SAUDIA_FILTER)


        // to initialize map location pin
        setMapPickLoctionIcon(R.drawable.ic_location)

    }

    override fun onGetLocationAddress(locationAddress: LocationAddress) {

        // todo handle as you need the pick location result or location selected from search

        Log.i("onGetLocationAddress","$locationAddress")
    }



}