package com.softartch_lib.domain

import com.google.android.gms.maps.model.LatLng
import io.reactivex.Single

class LocationAddressUseCase(private val locationAddressRepository: LocationAddressRepository) :
    UseCase<LatLng, Single<String>> {

    override fun execute(param: LatLng?): Single<String> =
            locationAddressRepository.getAddress(param!!)

}