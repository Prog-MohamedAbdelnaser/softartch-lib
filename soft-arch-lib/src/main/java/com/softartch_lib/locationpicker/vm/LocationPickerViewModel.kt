package com.softartch_lib.locationpicker.vm

import android.graphics.Typeface
import android.text.style.StyleSpan
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.SphericalUtil
import com.softartch_lib.component.RequestDataState
import com.softartch_lib.exceptions.ApiKeyRequiredException
import com.softartch_lib.domain.LocationAddress
import com.softartch_lib.domain.LocationAddressUseCase
import com.softartch_lib.domain.LocationAddressUseCases
import com.softartch_lib.locationpicker.PlaceAutoComplete
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class LocationPickerViewModel(private val useCases: LocationAddressUseCases) : ViewModel() {

    private val disposables=CompositeDisposable()

    private  val token  = AutocompleteSessionToken.newInstance()

    var lastSelectedLocation :LatLng?=null

    private var targetAddress: String? = null

    private var targetLocationAddress: LocationAddress?=null

    var locationAddressLiveDataState =MutableLiveData<RequestDataState<LocationAddress>>()

    var placesSearchLiveDataState =MutableLiveData<RequestDataState<ArrayList<PlaceAutoComplete>>>()

    companion object {
        const val ONE_METER = 1
    }

    private val locationAddressSubject = PublishSubject.create<LatLng>()

    init {

        try {
           // placesClient = Places.createClient(context)
        }catch (e:Exception){
            e.printStackTrace()
        }
        disposables.add(locationAddressSubject
            .filter {
                val filter =
                    if (lastSelectedLocation == null) {
                        true
                    } else {
                        SphericalUtil.computeDistanceBetween(
                            LatLng(lastSelectedLocation!!.latitude,
                                lastSelectedLocation!!.longitude),
                            LatLng(it.latitude, it.longitude)) > ONE_METER
                    }
                if (filter) {
                    locationAddressLiveDataState.postValue(RequestDataState.LoadingShow)
                }
                filter
            }
            .debounce(300, TimeUnit.MILLISECONDS)
            .subscribe {
                fetchLocationAddress(it)
            })
    }

    fun onCameraIdle(location: LatLng) {

        Log.i("onCameraIdle","location${location.toString()}")

        if (targetAddress == null) {
            locationAddressSubject.onNext(location)
        } else {
            targetLocationAddress=
                LocationAddress(location!!.latitude,location.longitude,targetAddress.toString())
            locationAddressLiveDataState.postValue(RequestDataState.Success(targetLocationAddress!!))
        }
    }

    fun fetchLocationAddress(latlng: LatLng?) {

        lastSelectedLocation = latlng
        disposables.add(useCases.locationAddressUseCase.execute(latlng)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                targetAddress = it
                targetLocationAddress = LocationAddress(latlng!!.latitude,latlng.longitude,it)
                locationAddressLiveDataState.value =RequestDataState.Success(targetLocationAddress!!)

            }, {
                locationAddressLiveDataState.value = RequestDataState.Error(it)
            })
        )
    }

    fun setTargetAddress(targetAddress: String?) {
        this.targetAddress = targetAddress
    }

    fun setTargetLocationAddress(locationAddress: LocationAddress?) {
        this.targetLocationAddress = locationAddress
        setTargetAddress(targetLocationAddress!!.addressName)
    }

     fun filter(constraint: CharSequence,localizationFillter:String=""){
         disposables.add(getPredictions(constraint,localizationFillter)
             .subscribeOn(Schedulers.io())
             .observeOn(AndroidSchedulers.mainThread())
             .doOnSubscribe { placesSearchLiveDataState.value=RequestDataState.LoadingShow }
             .doFinally {placesSearchLiveDataState.value=RequestDataState.LoadingFinished  }
             .subscribe({
                 placesSearchLiveDataState.value=RequestDataState.Success(it)
             },{
                 it.printStackTrace()
                 placesSearchLiveDataState.value=RequestDataState.Error(it)
             }))
     }

    fun getPredictions(constraint: CharSequence,localizationFillter:String): Single<ArrayList<PlaceAutoComplete>> {

       return Single.create<ArrayList<PlaceAutoComplete>> { emitter->

           if (useCases.placesClient==null){
               emitter.onError(ApiKeyRequiredException(message = "ApiKeyRequiredException"))
               return@create
           }
        val STYLE_NORMAL = StyleSpan(Typeface.NORMAL)
        val STYLE_BOLD = StyleSpan(Typeface.BOLD)

        val resultList = ArrayList<PlaceAutoComplete>()

        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(constraint.toString())
           .setCountry(localizationFillter)
            .setTypeFilter(TypeFilter.ADDRESS)
            .build()


        val autoCompletePredictions = useCases.placesClient?.findAutocompletePredictions(request)

        Tasks.await(autoCompletePredictions!!, 60, TimeUnit.SECONDS)

        autoCompletePredictions.addOnSuccessListener {

            if (it.autocompletePredictions.isNullOrEmpty().not()){

                it.autocompletePredictions.iterator().forEach { it ->
                    Log.i("getPredictions","getPredictions ${it.toString()}")
                    resultList.add(
                        PlaceAutoComplete(
                        it.placeId,
                        it.getPrimaryText(STYLE_NORMAL).toString(),
                        it.getFullText(STYLE_BOLD).toString())
                    )
                }
                emitter.onSuccess(resultList)
            }

        }.addOnFailureListener {
            emitter.onError(it)
        }

       }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()

    }
}