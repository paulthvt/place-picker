package com.thevenot.placepickerexample

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.thevenot.placepicker.AddressData
import com.thevenot.placepicker.Constants
import com.thevenot.placepicker.MapType
import com.thevenot.placepicker.PlacePicker

class MainActivity : AppCompatActivity() {

    private lateinit var placePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val applicationInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)

        placePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getParcelableExtra<AddressData>(Constants.ADDRESS_INTENT)?.let { addressData ->
                    findViewById<TextView>(R.id.address_data_text_view).text = addressData.toString()
                }
            }
        }

        findViewById<Button>(R.id.open_place_picker_button).setOnClickListener {
            val intent = PlacePicker.IntentBuilder()
                .setLatLong(40.748672, -73.985628)
                .showLatLong(true)
                .setMapRawResourceStyle(R.raw.map_style)
                .setMapType(MapType.NORMAL)
                .setMyLocationButtonPosition(PlacePicker.Position.LEFT)
                .setPlaceSearchBar(true, applicationInfo.metaData.getString("com.google.android.geo.API_KEY"))
                .build(this)
            placePickerLauncher.launch(intent)
        }
    }
}
