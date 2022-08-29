package com.example.boniljmap.utils

import android.content.Context
import com.example.boniljmap.R
import com.example.boniljmap.model.MarkerItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer


class MyClusterRenderer(
	private val context: Context,
	private val googleMap: GoogleMap,
	private val clusterManager: ClusterManager<MarkerItem>
): DefaultClusterRenderer<MarkerItem>(context, googleMap, clusterManager) {

	private val mapUtils = MapsUtils(googleMap)

	override fun onBeforeClusterItemRendered(item: MarkerItem, markerOptions: MarkerOptions) {
		markerOptions
			.title(item.title)
			.snippet(item.snippet)
			.icon(mapUtils.getBitmapFromVectorDrawable(context, R.drawable.ic_marker_default)?.let {
				BitmapDescriptorFactory.fromBitmap(
					it
				)
			})
			.alpha(0.95f)
	}
}