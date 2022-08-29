package com.example.boniljmap.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.boniljmap.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

class MapsUtils(
	private val googleMap: GoogleMap
) {

//	fun drawCircle(map: GoogleMap, point: LatLng): Circle {
//		val circleOptions = CircleOptions()
//		circleOptions.center(point)
//		circleOptions.radius(50.0)
//		circleOptions.strokeColor(0x15111111)
//		circleOptions.fillColor(0x15111111)
//		circleOptions.strokeWidth(0f)
//		circleOptions.zIndex(-1f)
//		return map.addCircle(circleOptions)
//	}

// fun moveMyLocationButton() {
//		val locationButton: View =
//			(mView.findViewById<View>("1".toInt()).parent as View).findViewById("2".toInt())
//		val rlp = locationButton.layoutParams as RelativeLayout.LayoutParams
//		// position on right bottom
//		rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
//		rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
//		rlp.setMargins(300, 0, 0, 300)
//	}

	fun setMapStyle(context: Context) {
		try {
			val success = googleMap.setMapStyle(
				MapStyleOptions.loadRawResourceStyle(
					context,
					R.raw.map_style_2
				)
			)

			if (!success) {
				Log.i("Maps", "Failed to add style.")
			}
		} catch (e: Exception) {
			Log.d("Maps", e.toString())
		}
	}

	fun zoomCamera(position: LatLng, zoom: Float, animationDuration: Int) {
		googleMap.animateCamera(
			CameraUpdateFactory.newCameraPosition(
				CameraPosition.Builder()
					.target(position)
					.zoom(zoom)
					.build()
			),
			animationDuration,
			null
		)
	}

	fun getBitmapFromVectorDrawable(context: Context?, drawableId: Int): Bitmap? {
		val drawable = ContextCompat.getDrawable(context!!, drawableId)
		val bitmap = Bitmap.createBitmap(
			drawable!!.intrinsicWidth,
			drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
		)
		val canvas = Canvas(bitmap)
		drawable.setBounds(0, 0, canvas.width, canvas.height)
		drawable.draw(canvas)
		return bitmap
	}

//	fun setMapType(item: MenuItem, googleMap: GoogleMap) {
//		when (item.itemId) {
//			R.id.standard -> {
//				googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
//			}
//			R.id.sattelite -> {
//				googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
//			}
//			R.id.hybrid -> {
//				googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
//			}
//			R.id.none -> {
//				googleMap.mapType = GoogleMap.MAP_TYPE_NONE
//			}
//			R.id.terrain -> {
//				googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
//			}
//		}
//	}
}