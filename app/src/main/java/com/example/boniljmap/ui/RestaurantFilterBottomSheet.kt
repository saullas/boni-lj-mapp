package com.example.boniljmap.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.boniljmap.databinding.FragmentRestaurantFilterBottomSheetBinding
import com.example.boniljmap.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import java.util.*

class RestaurantFilterBottomSheet: BottomSheetDialogFragment() {

	private val args by navArgs<RestaurantFilterBottomSheetArgs>()

	private var restaurantTag = Constants.DEFAULT_TAG
	private var restaurantTagId = 0
	private var restaurantType = Constants.DEFAULT_RESTAURANT_TYPE
	private var restaurantTypeId = 0

	private var _binding : FragmentRestaurantFilterBottomSheetBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		// Inflate the layout for this fragment
		_binding = FragmentRestaurantFilterBottomSheetBinding.inflate(inflater, container, false)

		restaurantTag = args.restaurantTag
		restaurantTagId = args.restaurantTagId
		restaurantType = args.restaurantType
		restaurantTypeId = args.restaurantTypeId

//		checkSelectedChips()
		val tagChip = binding.cgTags.findViewWithTag<Chip>(restaurantTag)
		tagChip.isChecked = true

		val typeChip = binding.cgDietType.findViewWithTag<Chip>(restaurantType)
		typeChip.isChecked = true

		binding.cgTags.setOnCheckedChangeListener { group, selectedChipId ->
			val chip = group.findViewById<Chip>(selectedChipId)
			restaurantTag = chip.text.toString().lowercase(Locale.ROOT)
			restaurantTagId = selectedChipId
		}

		binding.cgDietType.setOnCheckedChangeListener { group, selectedChipId ->
			val chip = group.findViewById<Chip>(selectedChipId)
			restaurantType = chip.text.toString().lowercase(Locale.ROOT)
			restaurantTypeId = selectedChipId
		}

		binding.btnApply.setOnClickListener {
			navigateToMapsFragment()
		}

		return binding.root
	}

	private fun navigateToMapsFragment() {
		val action = RestaurantFilterBottomSheetDirections.actionRestaurantFilterBottomSheetToMapsFragment(
			true,
			restaurantTag,
			restaurantType,
			restaurantTagId,
			restaurantTypeId
		)
		findNavController().navigate(action)
	}

//	private fun checkSelectedChips() {
//		for (i in 0 until binding.cgTags.childCount) {
//			val chip = binding.cgTags.getChildAt(i) as Chip
//			chip.isChecked = chip.text == restaurantTag
//		}
//		for (i in 0 until binding.cgTags.childCount) {
//			val chip = binding.cgDietType.getChildAt(i) as Chip
//			chip.isChecked = chip.text == restaurantType
//		}
//	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}