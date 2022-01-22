package com.thevenot.placepicker

import android.location.Address
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddressData(
  var latitude: Double,
  var longitude: Double,
  var addressList: List<Address>
): Parcelable {
  override fun toString(): String {
    return latitude.toString()+"\n" +
            longitude.toString()+"\n"+
            getAddressString()
  }
  fun getAddressString():String {
    return addressList.firstOrNull()?.getAddressLine(0) ?: ""
  }
}