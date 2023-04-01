[![](https://jitpack.io/v/pault1337/place-picker.svg)](https://jitpack.io/#pault1337/place-picker)

# PlacePicker
Place Picker for Google Maps has been deprecated in Android and we are told to move to paid APIs. Autocomplete, Nearby and Places photos APIs are chargeable after a number of loads. [Check Pricing here](https://cloud.google.com/maps-platform/pricing/sheet/)

<p align="center"><img src="https://github.com/pault1337/place-picker/blob/master/screens/place_picker_deprecated.png"></p>

Thankfully, Static and Dynamic Maps on Mobile and Geocoder is still free. PlacePicker is a Place Picker alternative library that allows you to pick a point in the map and get its coordinates and Address using Geocoder instead of Google APIs

<p align="center"><img src="https://github.com/pault1337/place-picker/blob/master/screens/demo.gif"></p>

## Adding PlacePicker to your project

Include the following dependencies in your app's build.gradle :

Add it in your root build.gradle at the end of repositories:

```
allprojects {
  repositories {
	  ...
		maven { url 'https://jitpack.io' }
	}
}
```

```
dependencies {
  implementation 'com.github.pault1337:place-picker:1.0.2'
}
```
PlacePicker Uses **AndroidX** artifacts, thus to use it without issues, make sure your application has been migrated to AndroidX as well. If you havent done it already, [Here's How](https://developer.android.com/jetpack/androidx/migrate)

## How to use

1. You need a Maps API key and add it to your app. [Here's How](https://developers.google.com/maps/documentation/android-sdk/signup)
2. Create a launcher to get the activity result
``` kotlin
val placePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getParcelableExtra<AddressData>(Constants.ADDRESS_INTENT)?.let { addressData ->
                    findViewById<TextView>(R.id.address_data_text_view).text = addressData.toString()
                }
            }
        }
        
```
3. Then, to start The `PlacePickerActivity`:

``` kotlin
val intent = PlacePicker.IntentBuilder()
                .setLatLong(40.748672, -73.985628)  // Initial Latitude and Longitude the Map will load into
                .showLatLong(true)  // Show Coordinates in the Activity
                .setMapZoom(12.0f)  // Map Zoom Level. Default: 14.0
                .setAddressRequired(true) // Set If return only Coordinates if cannot fetch Address for the coordinates. Default: True
                .hideMarkerShadow(true) // Hides the shadow under the map marker. Default: False
                .setMarkerDrawable(R.drawable.marker) // Change the default Marker Image
                .setMarkerImageImageColor(R.color.colorPrimary)
                .setFabColor(R.color.fabColor)
                .setPrimaryTextColor(R.color.primaryTextColor) // Change text color of Shortened Address
                .setSecondaryTextColor(R.color.secondaryTextColor) // Change text color of full Address
                .setBottomViewColor(R.color.bottomViewColor) // Change Address View Background Color (Default: White)
                .setMapRawResourceStyle(R.raw.map_style)  //Set Map Style (https://mapstyle.withgoogle.com/)
                .setMapType(MapType.NORMAL)
                .setPlaceSearchBar(true, GOOGLE_API_KEY) //Activate GooglePlace Search Bar. Default is false/not activated. SearchBar is a chargeable feature by Google
                .onlyCoordinates(true)  //Get only Coordinates from Place Picker
                .hideLocationButton(true)   //Hide Location Button (Default: false)
                .disableMarkerAnimation(true)   //Disable Marker Animation (Default: false)
                .build(this)
placePickerLauncher.launch(intent)
```

Note: Placepicker DOES NOT access your location. It's your app's responsibility to fet and provide the User's location and send via `setLatLong`. The MyLocation button takes the user in the sent location

Note: `PlacePickerActivity` uses the default theme of your app. If you want to change the theme, declare it in your app's Manifest:
```xml
<activity
    android:name="com.thevenot.placepicker.PlacePickerActivity"
    android:theme="@style/PlacePickerTheme"/> <!-- Included FullScreen Day-Night Theme -->
```

## Changelog

### [1.0.2]
- Do not require activity for IntentBuilder

### [1.0.1]
- Make address non nullable on return data object

### [1.0.0]
- First version


**Note:** This is inspired from [PlacePicker](https://github.com/suchoX/PlacePicker) which was inspired by Mapbox [Android Place Picker plugin](https://docs.mapbox.com/android/plugins/examples/place-picker/). Code and UI has been reused from the open source library hosted on [Github](https://github.com/mapbox/mapbox-plugins-android). Their copyright license has been added [here](https://github.com/pault1337/place-picker/blob/master/LICENSE)