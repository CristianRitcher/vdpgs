package com.example.vdgps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng


import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.firebase.firestore.FirebaseFirestore



class MapFragment : Fragment(), OnMapReadyCallback {


    private lateinit var map: GoogleMap
    private var isMapReady = false
    private lateinit var vehicleSpinner: Spinner
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val vehicleList = mutableListOf<Vehicle>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_map_fragment, container, false)
        vehicleSpinner = view.findViewById(R.id.vehicle_spinner)
        setupVehicleSpinner()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        isMapReady = true
        val latLng = LatLng(24.0248, 104.6608)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
    }

    private fun setupVehicleSpinner() {
        // Load vehicles from Firestore
        firestore.collection("vehiculos")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val vehicle = document.toObject(Vehicle::class.java)
                    val lati = vehicle.latitud
                    Log.d("MapFragment", "Item selected at position: $lati")
                    vehicleList.add(vehicle)
                }
                val vehicleNames = vehicleList.map { it.modelo }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vehicleNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                vehicleSpinner.adapter = adapter

                // Configura el listener después de establecer el adaptador
                vehicleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        Log.d("MapFragment", "Item selected at position: $position")
                        val selectedVehicle = vehicleList[position]
                        updateMapLocation(selectedVehicle.latitud, selectedVehicle.longitud)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        Log.d("MapFragment", "No item selected")
                    }
                }
            }
    }

    private fun updateMapLocation(latitude: Double, longitude: Double) {
        if (isMapReady) {
            Log.d("MapFragment", "Updating location to lat: $latitude, long: $longitude")
            val latLng = LatLng(latitude, -longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
        } else {
            Log.d("map is not ready", "fail")
        }
    }
}

data class Vehicle(
    val modelo: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val usuario_id: String = ""
)


