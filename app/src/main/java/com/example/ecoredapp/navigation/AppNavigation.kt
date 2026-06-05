package com.example.ecoredapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecoredapp.ui.screens.HomeScreen
import com.example.ecoredapp.ui.screens.LoginScreen
import com.example.ecoredapp.ui.screens.SplashScreen
import com.example.ecoredapp.ui.screens.MarketplaceScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onNavigateToLogin = {
                navController.navigate("login") { popUpTo("splash") { inclusive = true } }
            })
        }
        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("home") { popUpTo("login") { inclusive = true } }
            })
        }
        composable("home") {
            // Le pasamos la instrucción al Home de cómo viajar a la tienda
            HomeScreen(onNavigateToTienda = {
                navController.navigate("marketplace")
            })
        }
        composable("marketplace") {
            MarketplaceScreen()
        }
    }
}