package com.softartch_lib.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.softartch_lib.R
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

object MapUtility {

     fun addMarkerAndMoveToSelectedLocation(context: Context, googleMap: GoogleMap?, latLng: LatLng, @DrawableRes icon:Int= R.drawable.ic_location, zoom: Float): Marker? {
        moveCamera(googleMap!!, latLng,zoom)
        return googleMap.addMarker(createMarkerOption(latLng,context))
    }



    private fun createMarkerOption(latLng: LatLng,context: Context): MarkerOptions {
           val bitmap = getBitmapFromVectorDrawable(context)
        return MarkerOptions().position(latLng).icon(
            BitmapDescriptorFactory.fromBitmap(bitmap)
        )
    }

    private fun getBitmapFromVectorDrawable(context:Context,@DrawableRes icon: Int= R.drawable.ic_location): Bitmap {
        val drawable = ContextCompat.getDrawable(context,icon)

        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    fun moveCamera(googleMap: GoogleMap, latLng: LatLng,zoom: Float=15f) {
        googleMap.animateCamera(createCameraUpdate(googleMap, latLng,zoom), 800, null)
    }


     fun createCameraUpdate(googleMap: GoogleMap, latLng: LatLng,zoom:Float=googleMap.cameraPosition.zoom): CameraUpdate {
        return CameraUpdateFactory.newLatLngZoom(latLng, zoom!!)
    }

}