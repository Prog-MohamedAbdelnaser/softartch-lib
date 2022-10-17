# locationPicker&placesSearch library
An Android library that provides a location picker and auto complete search about locations

# Usage
For a working implementation of this project see the sample/ folder.

### Step 1

Include the library as a local library project or add the dependency in your build.gradle.

```groovy
dependencies {

      implementation 'com.github.Prog-MohamedAbdelnaser:location-picker-places-search-lib:Tag'

}
```
Or

Import the library, then add it to your /settings.gradle and /app/build.gradle. 

### Step 2
Make you owen map fragment child of LocationPickerFragmentWithSearchBar.


class AddressFragmentWithSearchBar : LocationPickerFragmentWithSearchBar(){

    //todo set your api key here 
    override fun setGoogleAPIKEY(): String = ""

    override fun mapViewResource(): MapView =mapView

    override fun layoutResource(): Int = R.layout.fragment_location

    override fun onViewInflated(parentView: View, childView: View) {
        super.onViewInflated(parentView, childView)

        //to fillter auto complete  search result using country code
        setSearchLocalizationFilter("EG")
        // to initialize map location pin
        setMapPickLoctionIcon(R.drawable.ic_location)
        setSearchViewAutoComplete(searchViewAuto)
        btnSave.setOnClickListener {
            requireActivity().onBackPressed()
        }

    }

    override fun onGetLocationAddress(locationAddress: LocationAddress) {

        // todo handle as you need the pick location result or location selected from search

        Log.i("onGetLocationAddress","$locationAddress")
    }
}
   
### Step 3

Add com.softartch_lib.component.widget.AutoCompleteSearchView  and map view to map fragment xm file .
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:orientation="vertical">


    <com.google.android.gms.maps.MapView
        android:id="@+id/mapView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:cameraTargetLat="23.91895493226023"
        app:cameraTargetLng="45.43327666819095"
        app:layout_constraintBottom_toTopOf="@id/btnSave"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/searchViewAuto" />
    <com.softartch_lib.component.widget.AutoCompleteSearchView
        android:id="@+id/searchViewAuto"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:background="@color/colorWhite"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        />
    <Button

        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintBottom_toBottomOf="parent"
        android:background="@color/colorAccent"
        android:id="@+id/btnSave"
        android:textColor="@color/colorWhite"
        android:textStyle="bold"
        android:text="save"
        />
</androidx.constraintlayout.widget.ConstraintLayout>

### Full explanation for all features
    * https://youtu.be/T7DUjyEYwBE

