package com.thevenot.placepicker

import android.content.Intent
import android.content.res.ColorStateList
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.thevenot.placepicker.Constants.ADDRESS_REQUIRED_INTENT
import com.thevenot.placepicker.Constants.BOTTOM_VIEW_COLOR_RES_INTENT
import com.thevenot.placepicker.Constants.DEFAULT_ZOOM
import com.thevenot.placepicker.Constants.DISABLE_MARKER_ANIMATION
import com.thevenot.placepicker.Constants.FAB_COLOR_RES_INTENT
import com.thevenot.placepicker.Constants.GOOGLE_API_KEY
import com.thevenot.placepicker.Constants.HIDE_LOCATION_BUTTON
import com.thevenot.placepicker.Constants.HIDE_MARKER_SHADOW_INTENT
import com.thevenot.placepicker.Constants.INITIAL_LATITUDE_INTENT
import com.thevenot.placepicker.Constants.INITIAL_LONGITUDE_INTENT
import com.thevenot.placepicker.Constants.INITIAL_ZOOM_INTENT
import com.thevenot.placepicker.Constants.MAP_RAW_STYLE_RES_INTENT
import com.thevenot.placepicker.Constants.MAP_TYPE_INTENT
import com.thevenot.placepicker.Constants.MARKER_COLOR_RES_INTENT
import com.thevenot.placepicker.Constants.MARKER_DRAWABLE_RES_INTENT
import com.thevenot.placepicker.Constants.MY_LOCATION_BUTTON_POSITION
import com.thevenot.placepicker.Constants.ONLY_COORDINATES_INTENT
import com.thevenot.placepicker.Constants.PRIMARY_TEXT_COLOR_RES_INTENT
import com.thevenot.placepicker.Constants.SEARCH_BAR_ENABLE
import com.thevenot.placepicker.Constants.SECONDARY_TEXT_COLOR_RES_INTENT
import com.thevenot.placepicker.Constants.SHOW_LAT_LONG_INTENT
import com.thevenot.placepicker.PlacePicker.Position.LEFT
import com.thevenot.placepicker.databinding.ActivityPlacePickerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class PlacePickerActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val TAG = "PlacePickerActivity"
        const val DEFAULT_MARGIN_DP = 16
    }

    private lateinit var map: GoogleMap
    private lateinit var placeAutocomplete: AutocompleteSupportFragment
    private var googleApiKey: String? = null
    private var searchBarEnable: Boolean = false

    private var latitude = Constants.DEFAULT_LATITUDE
    private var longitude = Constants.DEFAULT_LONGITUDE
    private var initLatitude = Constants.DEFAULT_LATITUDE
    private var initLongitude = Constants.DEFAULT_LONGITUDE
    private var showLatLong = true
    private var zoom = DEFAULT_ZOOM
    private var addressRequired: Boolean = true
    private var shortAddress = ""
    private var fullAddress = ""
    private var hideMarkerShadow = false
    private var markerDrawableRes: Int = -1
    private var markerColorRes: Int = -1
    private var fabColorRes: Int = -1
    private var primaryTextColorRes: Int = -1
    private var secondaryTextColorRes: Int = -1
    private var bottomViewColorRes: Int = -1
    private var mapRawResourceStyleRes: Int = -1
    private var addresses: List<Address>? = null
    private var mapType: MapType = MapType.NORMAL
    private var onlyCoordinates: Boolean = false
    private var hideLocationButton: Boolean = false
    private var disableMarkerAnimation: Boolean = false
    private var myLocationButtonPosition: PlacePicker.Position? = null
    private lateinit var binding: ActivityPlacePickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getIntentData()

        if (searchBarEnable) {
            showSearchBar()
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        binding.textViewPlaceCoordinates.visibility = if (showLatLong) View.VISIBLE else View.GONE

        binding.placeChosenButton.setOnClickListener {
            if (onlyCoordinates) {
                sendOnlyCoordinates()
            } else {
                addresses?.let {
                    val addressData = AddressData(latitude, longitude, it)
                    val returnIntent = Intent()
                    returnIntent.putExtra(Constants.ADDRESS_INTENT, addressData)
                    setResult(RESULT_OK, returnIntent)
                    finish()
                } ?: run {
                    if (!addressRequired) {
                        sendOnlyCoordinates()
                    } else {
                        Toast.makeText(
                            this@PlacePickerActivity,
                            R.string.place_picker_no_address,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        binding.myLocationButton.setOnClickListener {
            if (this::map.isInitialized) {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(initLatitude, initLongitude),
                        zoom
                    )
                )
            }
        }
        setIntentCustomization()

        iniMyLocationButton()
    }

    private fun iniMyLocationButton() {
        if (myLocationButtonPosition != null && myLocationButtonPosition == LEFT) {
            val constraintLayout = binding.placePickerLayout
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)
            constraintSet.clear(R.id.my_location_button, ConstraintSet.LEFT)
            constraintSet.clear(R.id.my_location_button, ConstraintSet.BOTTOM)
            constraintSet.clear(R.id.my_location_button, ConstraintSet.RIGHT)
            constraintSet.clear(R.id.my_location_button, ConstraintSet.TOP)
            constraintSet.clear(R.id.my_location_button, ConstraintSet.START)
            constraintSet.clear(R.id.my_location_button, ConstraintSet.END)
            val margin = (DEFAULT_MARGIN_DP * this.resources.displayMetrics.density).toInt()
            constraintSet.connect(
                R.id.my_location_button,
                ConstraintSet.START,
                R.id.place_picker_layout,
                ConstraintSet.START,
                margin
            )
            constraintSet.connect(
                R.id.my_location_button,
                ConstraintSet.BOTTOM,
                R.id.info_layout,
                ConstraintSet.TOP,
                margin
            )
            constraintSet.applyTo(constraintLayout)
        }
    }

    private fun moveCamera(location: LatLng) {
        Log.d(TAG, "Moving to $location")
        val cameraPosition =
            CameraPosition(LatLng(location.latitude, location.longitude), zoom, 0f, 0f)
        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun showSearchBar() {
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, googleApiKey!!)
        }

        binding.searchBarCardView.visibility = View.VISIBLE
        placeAutocomplete = supportFragmentManager.findFragmentById(R.id.place_autocomplete)
                as AutocompleteSupportFragment

        placeAutocomplete.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.ADDRESS_COMPONENTS
            )
        )
        placeAutocomplete.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                latitude = place.latLng!!.latitude
                longitude = place.latLng!!.longitude
                setAddress(latitude, longitude)

                map.clear()
                map.setOnMapLoadedCallback {
                    setPlaceDetails(latitude, longitude, shortAddress, fullAddress)
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(latitude, longitude),
                            zoom
                        )
                    )
                }
            }

            override fun onError(error: Status) {
                Log.d(TAG, error.toString())
            }
        })
    }

    private fun sendOnlyCoordinates() {
        val addressData = AddressData(latitude, longitude, listOf())
        val returnIntent = Intent()
        returnIntent.putExtra(Constants.ADDRESS_INTENT, addressData)
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    private fun getIntentData() {
        latitude = intent.getDoubleExtra(INITIAL_LATITUDE_INTENT, Constants.DEFAULT_LATITUDE)
        longitude = intent.getDoubleExtra(INITIAL_LONGITUDE_INTENT, Constants.DEFAULT_LONGITUDE)
        initLatitude = latitude
        initLongitude = longitude
        showLatLong = intent.getBooleanExtra(SHOW_LAT_LONG_INTENT, false)
        addressRequired = intent.getBooleanExtra(ADDRESS_REQUIRED_INTENT, true)
        hideMarkerShadow = intent.getBooleanExtra(HIDE_MARKER_SHADOW_INTENT, false)
        zoom = intent.getFloatExtra(INITIAL_ZOOM_INTENT, DEFAULT_ZOOM)
        markerDrawableRes = intent.getIntExtra(MARKER_DRAWABLE_RES_INTENT, -1)
        markerColorRes = intent.getIntExtra(MARKER_COLOR_RES_INTENT, -1)
        fabColorRes = intent.getIntExtra(FAB_COLOR_RES_INTENT, -1)
        primaryTextColorRes = intent.getIntExtra(PRIMARY_TEXT_COLOR_RES_INTENT, -1)
        secondaryTextColorRes = intent.getIntExtra(SECONDARY_TEXT_COLOR_RES_INTENT, -1)
        bottomViewColorRes = intent.getIntExtra(BOTTOM_VIEW_COLOR_RES_INTENT, -1)
        mapRawResourceStyleRes = intent.getIntExtra(MAP_RAW_STYLE_RES_INTENT, -1)
        mapType = intent.getSerializableExtra(MAP_TYPE_INTENT) as MapType
        onlyCoordinates = intent.getBooleanExtra(ONLY_COORDINATES_INTENT, false)
        googleApiKey = intent.getStringExtra(GOOGLE_API_KEY)
        searchBarEnable = intent.getBooleanExtra(SEARCH_BAR_ENABLE, false)
        hideLocationButton = intent.getBooleanExtra(HIDE_LOCATION_BUTTON, false)
        disableMarkerAnimation = intent.getBooleanExtra(DISABLE_MARKER_ANIMATION, false)
        myLocationButtonPosition = intent.getParcelableExtra(MY_LOCATION_BUTTON_POSITION)
    }

    private fun setIntentCustomization() {
        binding.markerShadowImageView.visibility = if (hideMarkerShadow) View.GONE else View.VISIBLE
        if (markerColorRes != -1) {
            binding.markerImageView.setColorFilter(ContextCompat.getColor(this, markerColorRes))
        }
        if (markerDrawableRes != -1) {
            binding.markerImageView.setImageDrawable(ContextCompat.getDrawable(this, markerDrawableRes))
        }
        if (fabColorRes != -1) {
            binding.placeChosenButton.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, fabColorRes))
            binding.myLocationButton.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, fabColorRes))
        }
        if (primaryTextColorRes != -1) {
            binding.textViewPlaceName.setTextColor(ContextCompat.getColor(this, primaryTextColorRes))
        }
        if (secondaryTextColorRes != -1) {
            binding.textViewPlaceAddress.setTextColor(ContextCompat.getColor(this, secondaryTextColorRes))
        }
        if (bottomViewColorRes != -1) {
            binding.infoLayout.setBackgroundColor(ContextCompat.getColor(this, bottomViewColorRes))
        }
        binding.myLocationButton.visibility = if (hideLocationButton) View.INVISIBLE else View.VISIBLE
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.setOnCameraMoveStartedListener {
            if (binding.markerImageView.translationY == 0f && !disableMarkerAnimation) {
                binding.markerImageView.animate()
                    .translationY(-75f)
                    .setInterpolator(OvershootInterpolator())
                    .setDuration(250)
                    .start()
            }
        }

        map.setOnCameraIdleListener {
            if (!disableMarkerAnimation) {
                binding.markerImageView.animate()
                    .translationY(0f)
                    .setInterpolator(OvershootInterpolator())
                    .setDuration(250)
                    .start()
            }

            showLoadingBottomDetails()
            val latLng = map.cameraPosition.target
            latitude = latLng.latitude
            longitude = latLng.longitude

            lifecycleScope.launch {
                getAddressForLocation()
                setPlaceDetails(latitude, longitude, shortAddress, fullAddress)
            }
        }
        showLoadingBottomDetails()
        if (mapRawResourceStyleRes != -1) {
            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, mapRawResourceStyleRes))
        }
        map.mapType = when (mapType) {
            MapType.NORMAL -> GoogleMap.MAP_TYPE_NORMAL
            MapType.SATELLITE -> GoogleMap.MAP_TYPE_SATELLITE
            MapType.HYBRID -> GoogleMap.MAP_TYPE_HYBRID
            MapType.TERRAIN -> GoogleMap.MAP_TYPE_TERRAIN
            MapType.NONE -> GoogleMap.MAP_TYPE_NONE
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoom))
    }

    private fun showLoadingBottomDetails() {
        binding.textViewPlaceName.text = ""
        binding.textViewPlaceAddress.text = ""
        binding.textViewPlaceCoordinates.text = ""
        binding.progressBarPlace.visibility = View.VISIBLE
    }

    private fun setPlaceDetails(
        latitude: Double,
        longitude: Double,
        shortAddress: String,
        fullAddress: String
    ) {

        if (latitude == -1.0 || longitude == -1.0) {
            binding.textViewPlaceName.text = ""
            binding.textViewPlaceAddress.text = ""
            binding.progressBarPlace.visibility = View.VISIBLE
            return
        }
        binding.progressBarPlace.visibility = View.INVISIBLE

        binding.textViewPlaceName.text = if (shortAddress.isEmpty()) "Dropped Pin" else shortAddress
        binding.textViewPlaceAddress.text = fullAddress
        binding.textViewPlaceCoordinates.text = resources.getString(
            R.string.place_picker_coordinates,
            Location.convert(latitude, Location.FORMAT_DEGREES),
            Location.convert(longitude, Location.FORMAT_DEGREES)
        )
    }

    private suspend fun getAddressForLocation() {
        withContext(Dispatchers.Default) {
            setAddress(latitude, longitude)
        }
    }

    private fun setAddress(
        latitude: Double,
        longitude: Double
    ) {
        val geoCoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geoCoder.getFromLocation(latitude, longitude, 1)
            this.addresses = addresses
            return if (addresses != null && addresses.size != 0) {
                fullAddress = addresses[0].getAddressLine(
                    0
                ) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                shortAddress = generateFinalAddress(fullAddress).trim()
            } else {
                shortAddress = ""
                fullAddress = ""
            }
        } catch (e: Exception) {
            //Time Out in getting address
            Log.e(TAG, "Could not get the address", e)
            shortAddress = ""
            fullAddress = ""
            addresses = null
        }
    }

    private fun generateFinalAddress(
        address: String
    ): String {
        val s = address.split(",")
        return if (s.size >= 3) s[1] + "," + s[2] else if (s.size == 2) s[1] else s[0]
    }
}
