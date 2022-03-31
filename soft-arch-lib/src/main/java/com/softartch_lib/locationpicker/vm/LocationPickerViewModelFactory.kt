package com.softartch_lib.locationpicker.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.softartch_lib.domain.LocationAddressUseCase
import com.softartch_lib.domain.LocationAddressUseCases

class LocationPickerViewModelFactory( private val  usesCases: LocationAddressUseCases
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationPickerViewModel::class.java)) {
            return LocationPickerViewModel(usesCases) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
class LocationTrackViewModelFactory( private val  usesCases: LocationAddressUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationTrackViewModel::class.java)) {
            return LocationTrackViewModel(usesCases) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}