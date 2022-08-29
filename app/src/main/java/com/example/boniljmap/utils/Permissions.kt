package com.example.boniljmap.utils

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import com.vmadalin.easypermissions.EasyPermissions

object Permissions {

	fun hasLocationPermission(context: Context) =
		EasyPermissions.hasPermissions(
			context,
			Manifest.permission.ACCESS_FINE_LOCATION
		)

	fun requestLocationPermission(fragment: Fragment) {
		EasyPermissions.requestPermissions(
			 fragment,
			"This application cannot work without location permission",
			Constants.PERMISSION_LOCATION_REQUEST_CODE,
			Manifest.permission.ACCESS_FINE_LOCATION
		)
	}

//	fun hasBackgroundPermission(context: Context): Boolean {
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//			return EasyPermissions.hasPermissions(
//				context,
//				Manifest.permission.ACCESS_BACKGROUND_LOCATION
//			)
//		}
//		return true
//	}
//
//	fun requestBackgroundLocationPermission(fragment: Fragment) {
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//			EasyPermissions.requestPermissions(
//				fragment,
//				"Background location permission (allow location to work all the time) is essential for application to work properly.",
//				Constants.PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE,
//				Manifest.permission.ACCESS_BACKGROUND_LOCATION
//			)
//		}
//	}
}