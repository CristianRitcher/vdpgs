package com.example.vdgps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class WorkerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_worker)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set initial fragment // un mensaje
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AdminGeolocationFragment())
            .commit()

        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            val selectedFragment = when (menuItem.itemId) {
                R.id.home -> AdminGeolocationFragment()
                R.id.search -> LocationFragment()
                R.id.profile -> AdminGeolocationFragment()
                else -> null
            }
            selectedFragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, it)
                    .commit()
            }
            true
        }
    }
}