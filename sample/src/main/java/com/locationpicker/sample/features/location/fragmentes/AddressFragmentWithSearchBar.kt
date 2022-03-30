package com.locationpicker.sample.features.location.fragmentes

import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.softartch_lib.locationpicker.LocationAddress
import com.google.android.gms.maps.MapView
import com.softartch_lib.component.extension.hide
import com.softartch_lib.component.extension.show
import com.softartch_lib.locationpicker.LocationPickerFragmentWithSearchBar
import com.locationpicker.sample.R
import kotlinx.android.synthetic.main.fragment_location.*

class AddressFragmentWithSearchBar : LocationPickerFragmentWithSearchBar(){

    //todo set your api key here
    override fun setGoogleAPIKEY(): String =""

    override fun mapViewResource(): MapView =mapView

    override fun layoutResource(): Int = R.layout.fragment_location



    override fun onViewInflated(parentView: View, childView: View) {
        super.onViewInflated(parentView, childView)

        //to fillter auto complete  search result
        // setSearchLocalizationFilter(SAUDIA_FILTER)



        // to initialize map location pin
        setMapPickLoctionIcon(R.drawable.ic_location)
        setSearchViewAutoComplete(searchViewAuto)
        //initAutoSearchQuery()

    }

    private fun initAutoSearchQuery() {
/*
        searchViewAuto.getSearchView()?.setOnQueryTextListener(object :SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty().not()) {
                    searchQueryListener(newText.toString())
                }else{
                    searchViewAuto.getRecycleViewResults()!!.hide()
                }
                return false
            }
        })

        searchViewAuto.getSearchView()?.setOnCloseListener {
            searchViewAuto.getRecycleViewResults()!!.hide()
            tvSearchPlaceHolderMessage.hide()
            false
        }*/
    }

    override fun onGetLocationAddress(locationAddress: LocationAddress) {

        // todo handle as you need the pick location result or location selected from search

        Log.i("onGetLocationAddress","$locationAddress")
    }



}