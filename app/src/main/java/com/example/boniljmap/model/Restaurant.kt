package com.example.boniljmap.model

data class Restaurant(
	var title: String = "",
	var address: String = "",
	var geolocation: Pair<Double, Double>? = null,
	var timetable: List<String> = emptyList(),
	val rating: String = "",
	var subvention_price: String = "",
	var full_price: String = "",
	var tags: List<String> = emptyList()
)