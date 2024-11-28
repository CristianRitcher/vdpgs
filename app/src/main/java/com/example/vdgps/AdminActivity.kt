package com.example.vdgps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_geolocation)

        // Referencia al BottomNavigationView
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        // Configurar el fragmento inicial al cargar la actividad
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AdminGeolocationFragment())
            .commit()

        // Manejar los clics en los elementos del menú utilizando el método actualizado
        bottomNavigationView.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_geolocation -> AdminGeolocationFragment()
                R.id.nav_dashboard -> AdminDashboardFragment()
                R.id.nav_orders -> AdminOrdersFragment()
                else -> AdminGeolocationFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            true
        }
    }
}