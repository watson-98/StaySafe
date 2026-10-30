package com.example.staysafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.staysafe.data.HotelRepository
import com.example.staysafe.ui.theme.StaySafeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // open the database (and create the demo data on the first run) before any screen reads it
        HotelRepository.initialize(applicationContext)

        enableEdgeToEdge()
        setContent {
            StaySafeTheme {
                AppNavigation()
            }
        }
    }
}