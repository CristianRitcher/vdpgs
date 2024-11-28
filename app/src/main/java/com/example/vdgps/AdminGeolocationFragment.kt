package com.example.vdgps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class AdminGeolocationFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var isMapReady = false
    private lateinit var vehicleSpinner: Spinner
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val vehicleList = mutableListOf<VehicleModel>()
    private val vehicleMarkers = mutableMapOf<String, Marker>() // Para gestionar marcadores

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_geolocation, container, false)
        vehicleSpinner = view.findViewById(R.id.spinner_vehicle)
        setupVehicleSpinner()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        isMapReady = true
        val initialLatLng = LatLng(24.0248, 104.6608) // Cambiar por tu latitud y longitud inicial
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 15.0f))
        listenToVehicleUpdates() // Escucha cambios en tiempo real
    }

    private fun setupVehicleSpinner() {
        firestore.collection("vehiculos")
            .get()
            .addOnSuccessListener { documents ->
                vehicleList.clear()
                for (document in documents) {
                    val vehicle = document.toObject(VehicleModel::class.java)
                    vehicleList.add(vehicle)
                }
                val vehicleNames = vehicleList.map { it.modelo }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vehicleNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                vehicleSpinner.adapter = adapter

                vehicleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedVehicle = vehicleList[position]
                        updateMapLocation(selectedVehicle.latitud, selectedVehicle.longitud)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        Log.d("AdminGeolocation", "No vehicle selected")
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AdminGeolocation", "Error loading vehicles", exception)
            }
    }

    private fun listenToVehicleUpdates() {
        firestore.collection("vehiculos")
            .addSnapshotListener { snapshots, exception ->
                if (exception != null) {
                    Log.e("AdminGeolocation", "Error listening for updates", exception)
                    return@addSnapshotListener
                }

                snapshots?.forEach { document ->
                    val vehicle = document.toObject(VehicleModel::class.java)
                    val latLng = LatLng(vehicle.latitud, vehicle.longitud)

                    // Si el marcador ya existe, actualiza su posición
                    if (vehicleMarkers.containsKey(vehicle.usuario_id)) {
                        vehicleMarkers[vehicle.usuario_id]?.position = latLng
                    } else {
                        // Si no, crea un nuevo marcador
                        val marker = map.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(vehicle.modelo)
                        )
                        vehicleMarkers[vehicle.usuario_id] = marker!!
                    }
                }
            }
    }

    private fun updateMapLocation(latitude: Double, longitude: Double) {
        if (isMapReady) {
            val latLng = LatLng(latitude, longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
        } else {
            Log.d("AdminGeolocation", "Map is not ready yet")
        }
    }
}

data class VehicleModel(
    val modelo: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val usuario_id: String = ""
)