package com.example.boniljmap.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.TextView
import com.example.boniljmap.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoAdapter(context: Context) : GoogleMap.InfoWindowAdapter {

	private val contentView =
		(context as Activity).layoutInflater.inflate(R.layout.marker_custom_info_window, null)

	override fun getInfoContents(marker: Marker): View? {
		renderViews(marker, contentView)
		return contentView
	}

	override fun getInfoWindow(marker: Marker): View? {
		renderViews(marker, contentView)
		return contentView
	}

	private fun renderViews(marker: Marker?, contentView: View) {
		val title = marker?.title
		val description = marker?.snippet

		val tvTitle = contentView.findViewById<TextView>(R.id.tvTitle)
		tvTitle.text = title

		val tvDesc = contentView.findViewById<TextView>(R.id.tvDescription)
		tvDesc.text = description
	}
}