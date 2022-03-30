package com.locationpicker.sample.data.sources.resources

import android.app.Application

class AppResources(private val application: Application) {

    fun getString(resId: Int): String = application.getString(resId)

    fun getInteger(resId: Int): Int = application.resources.getInteger(resId)

}