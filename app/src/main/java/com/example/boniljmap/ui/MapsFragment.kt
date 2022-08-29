package com.example.boniljmap.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.boniljmap.R
import com.example.boniljmap.databinding.FragmentMapsBinding
import com.example.boniljmap.model.MarkerItem
import com.example.boniljmap.model.Restaurant
import com.example.boniljmap.utils.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.max

// TODO animate markers on click
// TODO change language
// TODO night mode
// TODO search bar bug fixes
// TODO filter (price, diet, ...) multiple choices
// TODO on double back pressed exit

class MapsFragment :
	Fragment(),
	OnMapReadyCallback,
	ClusterManager.OnClusterClickListener<MarkerItem>,
	GoogleMap.OnMarkerClickListener,
	GoogleMap.OnInfoWindowCloseListener {

	private var _binding: FragmentMapsBinding? = null
	private val binding get() = _binding!!
	private lateinit var mView: View

	private val args by navArgs<MapsFragmentArgs>()

	private lateinit var map: GoogleMap
	private lateinit var mapUtils: MapsUtils

	private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
	private lateinit var clusterManager: ClusterManager<MarkerItem>

//	private lateinit var markerCircle: Circle

	private var restaurantTag = Constants.DEFAULT_TAG
	private var restaurantTagId = 0
	private var restaurantType = Constants.DEFAULT_RESTAURANT_TYPE
	private var restaurantTypeId = 0

	private lateinit var restaurants: List<Restaurant>
	private var allMarkers: ArrayList<MarkerItem> = ArrayList()
	private lateinit var searchBarAdapter: ArrayAdapter<String>

	@SuppressLint("ResourceType")
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentMapsBinding.inflate(inflater, container, false)

		fusedLocationProviderClient =
			LocationServices.getFusedLocationProviderClient(requireActivity())

		if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
			val param = binding.clUi.layoutParams as ViewGroup.MarginLayoutParams
			param.setMargins(70, 100, 70, 100)
		}

		if (args.backFromBottomSheet) {
			restaurantTag = args.restaurantTag
			restaurantTagId = args.restaurantTagId
			restaurantType = args.restaurantType
			restaurantTypeId = args.restaurantTypeId
		}

		binding.fabFilter.setOnClickListener {
			navigateToBottomSheet()
		}

		binding.fabMyLocation.setOnClickListener {
			hideRestaurantInfoWindow()
			moveToLastKnownLocation()
		}

		binding.clInfoWindow.setOnClickListener {
			val lat = binding.tvLatitude.text
			val lng = binding.tvLongitude.text
			val title = binding.tvTitle.text
			val geoUri = "http://maps.google.com/maps?q=loc:$lat,$lng ($title)"

			val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
			requireContext().startActivity(intent)
		}

		mView = binding.root
		return binding.root
	}

	@SuppressLint("MissingPermission")
	private fun moveToLastKnownLocation() {
		fusedLocationProviderClient.lastLocation
			.addOnCompleteListener { loc ->
				if (args.backFromBottomSheet && loc.result != null) {
					map.moveCamera(
						CameraUpdateFactory.newLatLngZoom(
							LatLng(loc.result.latitude, loc.result.longitude),
							16f
						)
					)
				}
				if (loc.result != null) {
					mapUtils.zoomCamera(
						LatLng(loc.result.latitude, loc.result.longitude),
						16f,
						1000
					)
				} else { // set camera view to top of Ljubljana
					Toast.makeText(activity, "Please enable location", Toast.LENGTH_SHORT).show()
					map.moveCamera(
						CameraUpdateFactory.newLatLngZoom(
							LatLng(46.05295579928751, 14.511640124132384),
							11f
						)
					)
				}
			}
			.addOnFailureListener {
				map.moveCamera(
					CameraUpdateFactory.newLatLngZoom(
						LatLng(46.05295579928751, 14.511640124132384),
						12f
					)
				)
			}
	}

	private fun navigateToBottomSheet() {
		val action = MapsFragmentDirections.actionMapsFragmentToRestaurantFilterBottomSheet(
			restaurantTag,
			restaurantType,
			restaurantTagId,
			restaurantTypeId
		)
		findNavController().navigate(action)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
		mapFragment?.getMapAsync(this)
	}

	@SuppressLint("MissingPermission")
	override fun onMapReady(googleMap: GoogleMap) {
		map = googleMap
		mapUtils = MapsUtils(map)
		map.setPadding(0, 100, 0, 0)

		val sloveniaBounds = LatLngBounds(
			LatLng(45.37544721477564, 13.33498973992007),
			LatLng(46.967292530773975, 16.641049317698368)
		)
		map.setLatLngBoundsForCameraTarget(sloveniaBounds)

		mapUtils.setMapStyle(requireContext())

		clusterManager = ClusterManager(requireContext(), map)
		clusterManager.setOnClusterClickListener(this)
		clusterManager.renderer = MyClusterRenderer(requireContext(), map, clusterManager)
		clusterManager.markerCollection.setOnMarkerClickListener(this)
		clusterManager.markerCollection.setInfoWindowAdapter(CustomInfoAdapter(requireContext()))

		map.setOnCameraIdleListener(clusterManager)
		map.setOnInfoWindowCloseListener(this)

		map.setOnMapClickListener {
			hideRestaurantInfoWindow()
		}

//		We can suppress warning, because we already took care of permissions
		map.isMyLocationEnabled = true
//		map.setOnMyLocationButtonClickListener(this)

		map.uiSettings.apply {
			isZoomControlsEnabled = false
			isMyLocationButtonEnabled = false
			isZoomGesturesEnabled = true
			isRotateGesturesEnabled = true
			isTiltGesturesEnabled = false
			isCompassEnabled = false
			isScrollGesturesEnabled = true
		}

		map.setOnCameraMoveStartedListener { reasonCode ->
			if (reasonCode == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
				binding.lvSearchResults.visibility = View.GONE
				binding.clInfoWindow.visibility = View.INVISIBLE
				binding.svSearch.clearFocus()
			}
		}

		moveToLastKnownLocation()
		addRestaurantMarkers()
		setupSearchBar()
	}

	private fun hideRestaurantInfoWindow() {
		if (binding.clInfoWindow.isVisible) {
			binding.clInfoWindow.alpha = 1.0f
			binding.clInfoWindow.animate()
				.setDuration(200)
				.alpha(0.0f)
			binding.clInfoWindow.visibility = View.GONE
		}
	}

	private fun setupSearchBar() {
		val face: Typeface = Typeface.createFromAsset(context?.assets, "Montserrat-Regular.ttf")
		val searchText =
			binding.svSearch.findViewById<View>(androidx.appcompat.R.id.search_src_text) as TextView
		searchText.typeface = face
		searchText.textSize = 16f

		val restaurantNames: ArrayList<String> = arrayListOf()

		for (restaurant in restaurants) {
			if ((restaurantTag == "vse" && restaurantType == "vse") ||
				(restaurantTag == "vse" && restaurant.tags.contains(restaurantType)) ||
				(restaurant.tags.contains(restaurantTag) && restaurantType == "vse") ||
				(restaurant.tags.contains(restaurantTag) && restaurant.tags.contains(restaurantType))
			) {
				restaurantNames.add(restaurant.title)
			}
		}

		searchBarAdapter = ArrayAdapter<String>(
			requireContext(),
			R.layout.item_search_bar_result,
			restaurantNames
		)
		binding.lvSearchResults.adapter = searchBarAdapter

		binding.lvSearchResults.setOnItemClickListener { adapterView, view, position, l ->
			hideRestaurantInfoWindow()
			binding.lvSearchResults.visibility = View.GONE
			focusOnSelectedRestaurant(adapterView.getItemAtPosition(position) as String)
		}

		binding.svSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(input: String?): Boolean {
				if (input != null) {
					focusOnSelectedRestaurant(input)
					if (!binding.lvSearchResults.isVisible) {
						binding.lvSearchResults.visibility = View.VISIBLE
					}
				}
				return false
			}

			override fun onQueryTextChange(input: String?): Boolean {
				if (input.isNullOrEmpty()) {
					binding.svSearch.clearFocus()
					binding.lvSearchResults.visibility = View.GONE
				} else {
					if (!binding.lvSearchResults.isVisible) {
						binding.lvSearchResults.visibility = View.VISIBLE
					}
				}
				searchBarAdapter.filter.filter(input)
				return false
			}
		})
	}

	private fun focusOnSelectedRestaurant(restaurantName: String) {
		val restaurant = restaurants.find { it.title == restaurantName }

		if (restaurant != null) {
			for (marker in allMarkers) {
				if (marker.title == restaurant.title) {
					mapUtils.zoomCamera(marker.position, 18f, 1000)
					lifecycleScope.launch {
						delay(1000)
						displayRestaurantInfoWindow(restaurant)
					}
					break
				}
			}
		}
	}

	private fun addRestaurantMarkers() {
		val jsonFileString =
			Utils.getJsonDataFromAsset(requireContext(), "restaurants_data_all_w_rat.json")
		val gson = GsonBuilder().setPrettyPrinting().create()
		val restaurantList = object : TypeToken<List<Restaurant>>() {}.type
		restaurants = gson.fromJson(jsonFileString, restaurantList)

		for (restaurant in restaurants) {
			if (Utils.restaurantMatchesFilter(restaurant, restaurantTag, restaurantType)) {
				val addressSplit = restaurant.address.split(",").dropLast(1).joinToString(", ")

				val marker = MarkerItem(
					restaurant.geolocation!!.first,
					restaurant.geolocation!!.second,
					restaurant.title,
					"${addressSplit}\nDoplačilo: ${restaurant.subvention_price}",
					restaurant.tags
				)

				allMarkers.add(marker)
				clusterManager.addItem(marker)
			}
		}
	}

//	@SuppressLint("MissingPermission")
//	override fun onMyLocationButtonClick(): Boolean {
//		hideRestaurantInfoWindow()
//		fusedLocationProviderClient.lastLocation.addOnCompleteListener { loc ->
//			if (loc.result != null) {
//				mapUtils.zoomCamera(
//					LatLng(loc.result.latitude, loc.result.longitude),
//					16f,
//					1000
//				)
//			}
//		}
//		return true
//	}

	override fun onClusterClick(cluster: Cluster<MarkerItem>?): Boolean {
		hideRestaurantInfoWindow()
		mapUtils.zoomCamera(cluster!!.position, map.cameraPosition.zoom + 2f, 1000)
		return true
	}

	override fun onMarkerClick(marker: Marker): Boolean {
//		marker.hideInfoWindow()

//		markerCircle = drawCircle(marker.position)

//		marker.setIcon(Utils.getBitmapFromVectorDrawable(context, R.drawable.ic_marker_clicked)?.let {
//			BitmapDescriptorFactory.fromBitmap(it)
//		})

		val restaurant = restaurants.find { it.title == marker.title }
		displayRestaurantInfoWindow(restaurant!!)

		mapUtils.zoomCamera(marker.position, max(map.cameraPosition.zoom, 16f), 500)
		return true
	}

	private fun displayRestaurantInfoWindow(restaurant: Restaurant) {
		val tags = restaurant.tags

		binding.clInfoWindow.alpha = 0.0f
		binding.clInfoWindow.visibility = View.VISIBLE
		binding.clInfoWindow.animate()
			.setDuration(200)
			.alpha(1.0f)

		binding.tvTitle.text = restaurant.title
		val addressSplit = restaurant.address.split(",").dropLast(1).joinToString(", ")
		binding.tvAddress.text = addressSplit
		binding.tvPrice2.text = "${restaurant.subvention_price} €"

		binding.tvLatitude.text = restaurant.geolocation!!.first.toString()
		binding.tvLongitude.text = restaurant.geolocation!!.second.toString()

		val open: Boolean? = Utils.isRestaurantOpen(restaurant)

		if (open == null) {
			binding.tvStatus.text = "Ni podatka o urniku"
			binding.tvStatus.setTextColor(
				ContextCompat.getColor(
					requireContext(),
					R.color.textLightGray
				)
			)
			binding.ivStatus.visibility = View.GONE
		} else if (!open) {
			binding.tvStatus.text = "Zaprto"
			binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
			binding.tvStatus.visibility = View.VISIBLE
		} else {
			binding.tvStatus.text = "Odprto"
			binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
			binding.tvStatus.visibility = View.VISIBLE
		}

		if (restaurant.rating.isNotEmpty() && restaurant.rating != "0") {
			setRatingStars(restaurant.rating.toInt())
		} else {
			binding.ivStar1.visibility = View.GONE
			binding.ivStar2.visibility = View.GONE
			binding.ivStar3.visibility = View.GONE
			binding.ivStar4.visibility = View.GONE
			binding.ivStar5.visibility = View.GONE
			binding.tvRating.visibility = View.VISIBLE
		}

		if ("vegetarijansko" in tags) {
			binding.ivVegetarian.visibility = View.VISIBLE
		} else {
			binding.ivVegetarian.visibility = View.GONE
		}

		if ("pizza" in tags) {
			binding.ivPizza.visibility = View.VISIBLE
		} else {
			binding.ivPizza.visibility = View.GONE
		}

		if ("hitra hrana" in tags) {
			binding.ivFastFood.visibility = View.VISIBLE
		} else {
			binding.ivFastFood.visibility = View.GONE
		}

		if ("nov lokal" in tags) {
			binding.ivNew.visibility = View.VISIBLE
		} else {
			binding.ivNew.visibility = View.GONE
		}

		if ("dostava" in tags) {
			binding.ivDelivery.visibility = View.VISIBLE
		} else {
			binding.ivDelivery.visibility = View.GONE
		}

		if ("odprt ob vikendih" in tags) {
			binding.ivWeekendOpen.visibility = View.VISIBLE
		} else {
			binding.ivWeekendOpen.visibility = View.GONE
		}

		if ("vegansko" in tags) {
			binding.ivVegan.visibility = View.VISIBLE
		} else {
			binding.ivVegan.visibility = View.GONE
		}

		if ("dostop za invalide" in tags || "dostop za invalide (wc)" in tags) {
			binding.ivDisabled.visibility = View.VISIBLE
		} else {
			binding.ivDisabled.visibility = View.GONE
		}

		if ("za s seboj" in tags) {
			binding.ivTakeaway.visibility = View.VISIBLE
		} else {
			binding.ivTakeaway.visibility = View.GONE
		}
	}

	private fun setRatingStars(rating: Int) {
		binding.ivStar1.visibility = View.VISIBLE
		binding.ivStar2.visibility = View.VISIBLE
		binding.ivStar3.visibility = View.VISIBLE
		binding.ivStar4.visibility = View.VISIBLE
		binding.ivStar5.visibility = View.VISIBLE
		binding.tvRating.visibility = View.GONE

		val starDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_star)
		val emptyStarDrawable =
			ContextCompat.getDrawable(requireContext(), R.drawable.ic_star_empty)

		binding.ivStar1.setImageDrawable(emptyStarDrawable)
		binding.ivStar2.setImageDrawable(emptyStarDrawable)
		binding.ivStar3.setImageDrawable(emptyStarDrawable)
		binding.ivStar4.setImageDrawable(emptyStarDrawable)
		binding.ivStar5.setImageDrawable(emptyStarDrawable)

		if (rating >= 1) {
			binding.ivStar1.setImageDrawable(starDrawable)
		}
		if (rating >= 2) {
			binding.ivStar2.setImageDrawable(starDrawable)
		}
		if (rating >= 3) {
			binding.ivStar3.setImageDrawable(starDrawable)
		}
		if (rating >= 4) {
			binding.ivStar4.setImageDrawable(starDrawable)
		}
		if (rating >= 5) {
			binding.ivStar5.setImageDrawable(starDrawable)
		}
	}

	override fun onInfoWindowClose(marker: Marker) {
		marker.setIcon(
			mapUtils.getBitmapFromVectorDrawable(context, R.drawable.ic_marker_default)?.let {
				BitmapDescriptorFactory.fromBitmap(it)
			})
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}