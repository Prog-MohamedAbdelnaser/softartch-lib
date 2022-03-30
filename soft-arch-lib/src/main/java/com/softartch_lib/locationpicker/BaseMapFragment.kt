package com.softartch_lib.locationpicker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.Observer
import com.softartch_lib.R
import com.softartch_lib.component.RequestDataState
import com.softartch_lib.utility.hideKeyboard
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*

import com.google.android.gms.maps.*
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.softartch_lib.component.extension.hide
import com.softartch_lib.component.extension.show


import com.softartch_lib.exceptions.LocationServiceRequestException
import com.softartch_lib.exceptions.PermissionDeniedException
import com.softartch_lib.component.fragment.BaseFragment
import com.softartch_lib.component.widget.AutoCompleteSearchView
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.SingleSource
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.ArrayList


@Suppress("RedundantLambdaArrow", "MissingPermission")
abstract class BaseMapFragment : BaseFragment(), OnMapReadyCallback{

    companion object {
        const val SAUDIA_FILTER="SA"

        const val LOCATION_SERVICE_REQUEST_CODE = 1000

        const val providerChanges = "android.location.PROVIDERS_CHANGED"

        private const val defaultZoom: Float = 15f

        const val deviceInfoKey: String = "deviceInfoKey"

    }

    @DrawableRes
    var resLocationIcon:Int?=R.drawable.ic_location

    private var loadingTextPlaceHolder:String = "Loading...";

    abstract fun mapViewResource(): MapView

    abstract fun setGoogleAPIKEY(): String

     private var GOOGLE_API_KEY :String?=null

    protected val locationPermissionRequestCode = 1500

    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    private var permissionEmitter: SingleEmitter<Boolean>? = null

    private var locationServiceEmitter: SingleEmitter<Boolean>? = null

    private var disposable: Disposable? = null

    private var googleMap: GoogleMap? = null

    private var zoom: Float? = null

    private var fusedLocationClient: FusedLocationProviderClient? = null

    private val locationViewModel: LocationPickerViewModel by viewModel()

    private var savedInstanceState: Bundle? = null

    private var locationRequest: LocationRequest? = null

    private var targetMarker: Marker? = null

    private var targetAccuracy: Float = 0f

    private var userLocation: Location? = null

    private var locationCallback: LocationCallback? = null

    private var locationChangedListener: OnLocationChangedListener? = null

    private var locationUpdateStarted: Boolean = false

    private var gpsReceiver: GpsLocationReceiver? = null

    private var shouldMarkerFollowUserLocation: Boolean = false

    private var bitmap: Bitmap? = null

    private var tvAddress: AppCompatTextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.savedInstanceState = savedInstanceState

    }

    override fun onViewInflated(parentView: View, childView: View) {
        mapViewResource().onCreate(savedInstanceState)
        mapViewResource().getMapAsync(this)

        GOOGLE_API_KEY=setGoogleAPIKEY()

        try {
            if (!Places.isInitialized()) {
                Places.initialize(requireContext(), GOOGLE_API_KEY!!)
            }

        }catch (e:Exception){
            e.printStackTrace()
        }

        initViewModelObservers()


       // placesSearchResultAdapter?.setClickListener(this)

    }



    private fun initViewModelObservers() {

        locationViewModel.apply {
            locationAddressLiveDataState.observe(this@BaseMapFragment, Observer {
                Handler(Looper.getMainLooper()).post {
                    handleLocationAddressState(it)
                }

            })
        }
    }
    private fun handleLocationAddressState(state: RequestDataState<LocationAddress>?) {
        when(state){
            is RequestDataState.LoadingShow->{setMarkerTitle(loadingTextPlaceHolder)}

            is RequestDataState.Success->{
                state.data.addressName?.let { setMarkerTitle(it) }
                onGetLocationAddress(state.data)

            }
            is RequestDataState.Error->{
                state.exception.printStackTrace()
                onGetLocationAddressFailure(state.exception)
            }
        }
    }

    open fun onGetLocationAddressFailure(exception: Throwable) {

    }
    fun setMapPickLoctionIcon(@DrawableRes iconRes:Int){
        resLocationIcon=iconRes
    }


    fun setMarkerTitle(title:String){
        tvAddress?.text = title
        targetMarker?.showInfoWindow()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
        googleMap?.apply {

            enableMapTypeControls(this)

            setupGoogleMap(this)
            disposable = checkPermission()
                    .flatMap(requestLocationPermissionFunction())
                    .doOnSuccess {
                        listenToGPSChanges()
                    }
                    .flatMap(requestLocationServiceSettingFunction())
                    .subscribe({
                        startLocationTracking(googleMap)
                    }, {
                        if (it is PermissionDeniedException) {

                            // todo if you need to show user popup with permission need description
                        } else {
                       // showErrorSnackbar(requireView(), getString(R.string.wont_detect_location))
                        }
                    })

            setHasOptionsMenu(true)
        }
    }

    private fun startLocationTracking(googleMap: GoogleMap?) {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        }
        if (locationCallback == null) {
            locationCallback = locationCallback()
        }
        enableGoogleMapMyLocation(googleMap, locationSource())
    }

    private fun listenToGPSChanges() {
        context?.apply {
            gpsReceiver = GpsLocationReceiver()
            registerReceiver(gpsReceiver, IntentFilter(providerChanges))
        }
    }

    private fun locationSource() = object : LocationSource {
        override fun deactivate() {
            stopLocationUpdate()
        }

        override fun activate(locationChangedListener: OnLocationChangedListener?) {
            this@BaseMapFragment.locationChangedListener = locationChangedListener
            locationUpdateStarted = true
//            getLastKnownLocation()
            startLocationUpdate()
        }
    }

    private fun stopLocationUpdate() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    private fun startLocationUpdate() {
        if (locationUpdateStarted) {
            fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }


    private fun locationCallback() =
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult.locations.isNullOrEmpty().not()) {
                        val location = locationResult.locations.last()
                        if (targetMarker == null || shouldMarkerFollowUserLocation) {
                            shouldMarkerFollowUserLocation = true
                            addMarkerAndMoveToSelectedLocation(googleMap, LatLng(location.latitude, location.longitude))
                            targetAccuracy = location.accuracy

                            locationViewModel.onCameraIdle(LatLng(location.latitude,location.longitude))
                        }
                        locationChangedListener?.onLocationChanged(location)
                        userLocation = location
                    }
                }
            }

    private fun enableMapTypeControls(googleMap: GoogleMap) {

        val currentMapType =  GoogleMap.MAP_TYPE_NORMAL
         googleMap.mapType = currentMapType

        }

    private fun setupGoogleMap(googleMap: GoogleMap) {
        googleMap.apply {

            setupGoogleMapZoomSettings(this)

            setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                @SuppressLint("InflateParams")
                override fun getInfoContents(p0: Marker?): View? {
                    if (tvAddress == null) {
                        tvAddress = LayoutInflater
                                .from(context)
                                .inflate(R.layout.location_address, null, false)
                                as AppCompatTextView?
                    }
                    return tvAddress
                }

                override fun getInfoWindow(p0: Marker?): View? = null

            })

            setOnMapClickListener {
            }

        }
    }

    private fun setupGoogleMapZoomSettings(googleMap: GoogleMap) {
        googleMap.uiSettings?.apply {
            isZoomControlsEnabled = true
        }
    }

    private fun enableGoogleMapMyLocation(googleMap: GoogleMap?, locationSource: LocationSource) {
        googleMap?.apply {
            setLocationSource(locationSource)
            isMyLocationEnabled = true

            setOnMyLocationButtonClickListener {
                userLocation?.apply {
                    val latLng = LatLng(latitude, longitude)
                    if (shouldMarkerFollowUserLocation.not()) {
                        targetAccuracy = accuracy
                        shouldMarkerFollowUserLocation = true
                        addMarkerAndMoveToSelectedLocation(googleMap, latLng)
                    } else {
                        moveCamera(googleMap, latLng)
                    }
                }
                true
            }

            setOnMyLocationClickListener {
                Log.i("Location","setOnMyLocationClickListener")
                if (shouldMarkerFollowUserLocation.not()) {
                    targetAccuracy = it.accuracy
                    shouldMarkerFollowUserLocation = true
                    addMarkerAndMoveToSelectedLocation(googleMap, LatLng(it.latitude, it.longitude))
                }
            }
        }
    }

    private fun checkPermission(): Single<Boolean> =
            if (isMarshmallowOrLater()) {
                Single.just(isLocationPermissionGranted())
            } else {
                Single.just(true)
            }

    private fun isMarshmallowOrLater(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    private fun isLocationPermissionGranted() =
            checkSelfPermission() == PermissionChecker.PERMISSION_GRANTED

    private fun checkSelfPermission() =
            ContextCompat.checkSelfPermission(context!!, locationPermission)

    private fun requestLocationPermissionFunction() =
            Function<Boolean, SingleSource<Boolean>> { granted ->
                if (!granted) {
                    requestLocationPermissionSingle()
                } else {
                    Single.just(true)
                }
            }

    private fun requestLocationPermissionSingle(): Single<Boolean> {
        return Single.create<Boolean> {
            permissionEmitter = it
            requestPermissions(arrayOf(locationPermission), locationPermissionRequestCode)
        }
    }

    private fun requestLocationServiceSettingFunction() =
            Function<Boolean, SingleSource<Boolean>> { granted ->
                if (granted) {
                    requestLocationServiceSettingSingle()
                } else {
                    Single.error(IllegalArgumentException("Permissions must be granted first"))
                }
            }

    private fun requestLocationServiceSettingSingle(): Single<Boolean>? {
        return Single.create { emitter ->

            if (locationRequest == null) {
                locationRequest = createLocationRequest()
            }

            val locationSettingRequest =
                    locationSettingsRequest(locationRequest!!)

            val settingsClient =
                    LocationServices.getSettingsClient(context!!)

            val task: Task<LocationSettingsResponse> =
                    settingsClient.checkLocationSettings(locationSettingRequest)

            task.addOnSuccessListener {
                emitter.onSuccess(true)
            }

            task.addOnFailureListener {
                if (it is ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult()
                        val resolvable: ResolvableApiException = it
                        locationServiceEmitter = emitter
                        resolvable.startResolutionForResult(activity!!,
                                LOCATION_SERVICE_REQUEST_CODE
                        )
                    } catch (sendEx: IntentSender.SendIntentException) {
                        emitter.onError(sendEx)
                    }
                } else {
                    emitter.onError(it)
                }
            }

            task.addOnCanceledListener {
                emitter.onError(LocationServiceRequestException())
            }
        }
    }

    private fun locationSettingsRequest(locationRequest: LocationRequest) =
            LocationSettingsRequest
                    .Builder()
                    .addLocationRequest(locationRequest)
                    .build()

    private fun createLocationRequest() =
            LocationRequest()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setFastestInterval(5000)
                    .setInterval(8000)
                    .setSmallestDisplacement(5f)

    private fun addMarkerAndMoveToSelectedLocation(googleMap: GoogleMap?, latLng: LatLng, address: String? = null) {
        googleMap?.apply {
            targetMarker?.remove()
            targetMarker = addMarker(createMarkerOption(latLng))
            moveCamera(this, latLng)
            locationViewModel.setTargetAddress(address)
        }
    }

    private fun createMarkerOption(latLng: LatLng): MarkerOptions {
        if (bitmap == null) {
            bitmap = getBitmapFromVectorDrawable()
        }
        return MarkerOptions().position(latLng).icon(
                BitmapDescriptorFactory.fromBitmap(bitmap)
        )
    }

    private fun getBitmapFromVectorDrawable(): Bitmap {
        val drawable = ContextCompat.getDrawable(context!!, resLocationIcon?:R.drawable.ic_location)

        val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    private fun moveCamera(googleMap: GoogleMap, latLng: LatLng) {
        googleMap.animateCamera(createCameraUpdate(googleMap, latLng), 800, null)
    }

    private fun createCameraUpdate(googleMap: GoogleMap, latLng: LatLng): CameraUpdate {
        zoom = if (zoom == null || zoom!! < defaultZoom) {
            defaultZoom
        } else {
            googleMap.cameraPosition.zoom
        }
        return CameraUpdateFactory.newLatLngZoom(latLng, zoom!!)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == locationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
                permissionEmitter?.apply {
                    if (!isDisposed) {
                        onSuccess(true)
                        permissionEmitter = null
                    }
                }
            } else {
                permissionEmitter?.apply {
                    if (!isDisposed) {
                        onError(PermissionDeniedException())
                        permissionEmitter = null
                    }
                }
            }
        }
    }

    fun setPickedPlace(place: Place){
        shouldMarkerFollowUserLocation = false
        targetAccuracy = 0f
        addMarkerAndMoveToSelectedLocation(googleMap, place.latLng!!, place.address.toString())
        onGetLocationAddress(LocationAddress(place.latLng!!.latitude,place.latLng!!.longitude,place.address.toString()))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.w("onActivityResult", "onReceive ${data?.toString()}")

        if (requestCode == LOCATION_SERVICE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                locationServiceEmitter?.apply {
                    if (!isDisposed) {
                        onSuccess(true)
                        locationServiceEmitter = null
                    }
                }
            } else {
                locationServiceEmitter?.apply {
                    if (!isDisposed) {
                        onError(LocationServiceRequestException())
                        locationServiceEmitter = null
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        mapViewResource().onResume()
    }

    override fun onStart() {
        super.onStart()
        mapViewResource().onStart()
        startLocationUpdate()
    }

    override fun onPause() {
        super.onPause()
        mapViewResource().onPause()
    }

    override fun onStop() {
        super.onStop()
        mapViewResource().onStop()
        stopLocationUpdate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapViewResource().onDestroy()
        stopLocationUpdate()
        fusedLocationClient = null
        activity?.apply {
            hideKeyboard(window)
        }
        disposable?.apply {
            if (!isDisposed) {
                dispose()
            }
        }
        if (gpsReceiver != null) {
            context?.apply {
                unregisterReceiver(gpsReceiver)
                gpsReceiver = null
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapViewResource().onLowMemory()
    }

    inner class GpsLocationReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action?.equals(providerChanges) == true) {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    googleMap?.apply {
                        if (isMyLocationEnabled.not()) {
                            startLocationTracking(this)
                        } else {
                            this@BaseMapFragment.startLocationUpdate()
                        }
                    }
                } else {
                    this@BaseMapFragment.stopLocationUpdate()
                }
            }
        }
    }

    open fun onGetLocationAddress(locationAddress: LocationAddress) {}

    private fun showErrorAlertToUser(msg: String) {

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setMessage(msg)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.cancel()
            }

        val alert = alertDialogBuilder.create()
        alert.show()
    }

    fun setMarkerLoadingText(text:String){
        loadingTextPlaceHolder = text
    }
}