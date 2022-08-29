package com.example.boniljmap.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.boniljmap.R
import com.example.boniljmap.model.Restaurant
import com.example.boniljmap.model.RestaurantTimetable
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.util.*


object Utils {

//	fun computeGeolocations(context: Context) {
//		val geocoder = Geocoder(context, Locale.getDefault())
//		val jsonFileString = getJsonDataFromAsset(context, "restaurants_data_with_geolocations.json")
//		val gson = GsonBuilder().setPrettyPrinting().create()
//		val restaurantList = object : TypeToken<List<Restaurant>>() {}.type
//		val restaurants = gson.fromJson<List<Restaurant>>(jsonFileString, restaurantList)
//
//		for ((index, restaurant) in restaurants.withIndex()) {
//			Log.d("Geolocation", "${index+1} / ${restaurants.size}")
//			val address = restaurant.address
//			val geolocation = geocoder.getFromLocationName(address, 1)[0]
//			if (geolocation.hasLatitude() && geolocation.hasLongitude()) {
//				restaurant.geolocation = Pair(geolocation.latitude, geolocation.longitude)
//			} else {
//				Log.i("Geolocation", "${restaurant.title} geolocation not found")
//			}
//		}
//	}

	fun isRestaurantOpen(restaurant: Restaurant): Boolean? {
		if (restaurant.timetable.isNotEmpty()) {
			val calendar = Calendar.getInstance()
			val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
			lateinit var timetable: String

			if (dayOfWeek == Calendar.SATURDAY) {
				timetable = restaurant.timetable[1]
			} else if (dayOfWeek == Calendar.SUNDAY) {
				timetable = restaurant.timetable[2]
			} else {
				timetable = restaurant.timetable[0]
			}

			if (timetable == "zaprto") {
				return false
			} else {
				val splitted = timetable.split(" ")
				val open = splitted[0].split(":")
				val close = splitted[2].split(":")
				val openInt = (open[0] + open[1]).toInt()
				val closeInt = (close[0] + close[1]).toInt()

				if (openInt + closeInt == 0) { // from midnight to midnight
					return true
				}

				val currentTimeHour = calendar.get(Calendar.HOUR_OF_DAY).toString()
				var currentTimeMinutes = calendar.get(Calendar.MINUTE).toString()
				if (currentTimeMinutes.length == 1) {
					currentTimeMinutes = "0$currentTimeMinutes"
				}
				val currentTime = (currentTimeHour + currentTimeMinutes).toInt()

				return if (closeInt < openInt) { // 14:00 - 02:00
					(currentTime in openInt..2359) || (currentTime in 0..closeInt)
				} else {
					currentTime in openInt until closeInt
				}
			}
		} else {
			return null
		}
	}

	fun getJsonDataFromAsset(context: Context, fileName: String): String? {
		val jsonString: String
		try {
			jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
		} catch (ioException: IOException) {
			ioException.printStackTrace()
			return null
		}
		return jsonString
	}

	fun restaurantMatchesFilter(restaurant: Restaurant, restaurantTag: String, restaurantType: String): Boolean {
		val satisfyType = restaurantType == "vse" || restaurant.tags.contains(restaurantType)
		var satisfyTag = false

		if (restaurantTag == "trenutno odprto") {
			val open = isRestaurantOpen(restaurant)
			satisfyTag = open != null && open
		} else if (restaurantTag == "odprto ob vikendih") {
			if (restaurant.timetable.isNotEmpty()) {
				satisfyTag = restaurant.timetable[1] != "zaprto" || restaurant.timetable[2] != "zaprto"
			}
		} else if (restaurantTag == "brez doplačila") {
			satisfyTag = restaurant.subvention_price == "0,00"
		} else {
			satisfyTag = restaurantTag == "vse" || restaurant.tags.contains(restaurantTag)
		}

		return satisfyTag && satisfyType
	}

//	fun isLocationEnabled(context: Context): Boolean {
//		var locationMode = 0
//
//		try {
//			locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
//		} catch (e: SettingNotFoundException) {
//			e.printStackTrace()
//			return false
//		}
//
//		return locationMode != Settings.Secure.LOCATION_MODE_OFF
//	}
}