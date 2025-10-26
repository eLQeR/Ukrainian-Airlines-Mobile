package com.example.ukrainianairlines.ui.screens

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ukrainianairlines.R
import com.example.ukrainianairlines.ui.viewmodels.AuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup bottom navigation
        val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_nav_view)
        bottomNavView.setupWithNavController(navController)

        // Setup app bar (include airports as a top-level destination)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_search, R.id.nav_airports, R.id.nav_bookings, R.id.nav_profile)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Observe authentication state
        authViewModel.isLoggedIn.observe(this) { isLoggedIn ->
            updateNavigationVisibility(isLoggedIn)
        }
    }

    private fun updateNavigationVisibility(isLoggedIn: Boolean) {
        // Update UI based on login state if needed
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}