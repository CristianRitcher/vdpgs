package com.example.vdgps

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

class LocationFragment : Fragment() {

    private val locationManager by lazy {
        LocationManager(requireContext().applicationContext)
    }

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_location, container, false)

        // Request permissions
        ActivityCompat.requestPermissions(requireActivity(), permissions, 100)

        val locationTextView = view.findViewById<TextView>(R.id.locationTextView)
        val getLocationButton = view.findViewById<Button>(R.id.getLocationButton)
        val startTrackingButton = view.findViewById<Button>(R.id.startTrackingButton)
        val stopTrackingButton = view.findViewById<Button>(R.id.stopTrackingButton)

        getLocationButton.setOnClickListener {
            locationManager.getLocation { latitude, longitude ->
                locationTextView.text = "Location: ..$latitude / ..$longitude"
            }
        }

        startTrackingButton.setOnClickListener {
            Intent(requireContext(), LocationTrackerService::class.java).also {
                it.action = LocationTrackerService.Action.START.name
                requireContext().startService(it)
            }
        }

        stopTrackingButton.setOnClickListener {
            Intent(requireContext(), LocationTrackerService::class.java).also {
                it.action = LocationTrackerService.Action.STOP.name
                requireContext().startService(it)
            }
        }

        return view
    }
}