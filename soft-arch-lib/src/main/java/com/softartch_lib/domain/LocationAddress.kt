package com.softartch_lib.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize data class LocationAddress(var latitude:Double, val longitude:Double, var addressName:String?=null):Parcelable