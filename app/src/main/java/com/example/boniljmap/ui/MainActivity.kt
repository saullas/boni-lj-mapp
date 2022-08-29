package com.example.boniljmap.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.boniljmap.R
import com.example.boniljmap.utils.Permissions

class MainActivity : AppCompatActivity() {

	private lateinit var navController: NavController

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)
		navController = navHostFragment!!.findNavController()

		if (Permissions.hasLocationPermission(this)) {
			navController.navigate(R.id.action_permissionFragment_to_mapsFragment)
		}
	}
}