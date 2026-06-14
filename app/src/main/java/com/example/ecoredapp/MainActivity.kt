package com.example.ecoredapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ecoredapp.navigation.AppNavigation
import com.example.ecoredapp.ui.theme.EcoRedAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoRedAppTheme {
                AppNavigation()
            }
        }
    }
}