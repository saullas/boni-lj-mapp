package com.example.boniljmap.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class MarkerItem(
	lat: Double,
	lng: Double,
	private val title: String,
	private val snippet: String,
	private val tags: List<String>
): ClusterItem {

	private val position: LatLng = LatLng(lat, lng)

	override fun getPosition(): LatLng {
		return position
	}

	override fun getTitle(): String {
		return title
	}

	override fun getSnippet(): String {
		return snippet
	}

	fun getTags(): List<String> {
		return tags
	}
}