package com.example.ecoredapp.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ecoredapp.ui.screens.*
import com.example.ecoredapp.ui.theme.EcoGreenPrimary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Lógica Global de Notificaciones Flotantes (Tipo WhatsApp)
    LaunchedEffect(Unit) {
        auth.addAuthStateListener { firebaseAuth ->
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                db.collection("usuarios").document(userId).collection("notificaciones")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .addSnapshotListener { snapshot, _ ->
                        val doc = snapshot?.documents?.firstOrNull()
                        if (doc != null) {
                            val timestamp = doc.getLong("timestamp") ?: 0L
                            if (System.currentTimeMillis() - timestamp < 5000) {
                                val title = doc.getString("title") ?: "EcoRed"
                                val message = doc.getString("message") ?: ""
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "$title: $message",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") {
                SplashScreen(onNavigateToLogin = {
                    navController.navigate("login") { popUpTo("splash") { inclusive = true } }
                })
            }
            composable("login") {
                LoginScreen(onLoginSuccess = {
                    navController.navigate("home/0") { popUpTo("login") { inclusive = true } }
                })
            }
            composable(
                route = "home/{tabIndex}",
                arguments = listOf(navArgument("tabIndex") { type = NavType.IntType })
            ) { backStackEntry ->
                val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
                HomeScreen(
                    initialTab = tabIndex,
                    onNavigateToTienda = { navController.navigate("marketplace") },
                    onNavigateToMap = { navController.navigate("map") },
                    onNavigateToChallenges = { navController.navigate("challenges") },
                    onNavigateToReport = { navController.navigate("report") },
                    onNavigateToScan = { navController.navigate("scan") },
                    onNavigateToNotifications = { navController.navigate("notifications") },
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo("home/0") { inclusive = true }
                        }
                    }
                )
            }
            composable("marketplace") {
                RedeemRewardsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigate("home/0") },
                    onNavigateToPerfil = { navController.navigate("home/2") }
                )
            }
            composable("map") {
                MapScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigate("home/0") },
                    onNavigateToTienda = { navController.navigate("marketplace") },
                    onNavigateToPerfil = { navController.navigate("home/2") }
                )
            }
            composable("challenges") {
                ChallengesScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigate("home/0") },
                    onNavigateToTienda = { navController.navigate("marketplace") },
                    onNavigateToPerfil = { navController.navigate("home/2") }
                )
            }
            composable("report") {
                ReportTrashScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigate("home/0") },
                    onNavigateToTienda = { navController.navigate("marketplace") },
                    onNavigateToPerfil = { navController.navigate("home/2") }
                )
            }
            composable("scan") {
                ScanRecyclingScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToHome = { navController.navigate("home/0") },
                    onNavigateToTienda = { navController.navigate("marketplace") },
                    onNavigateToPerfil = { navController.navigate("home/2") }
                )
            }
            composable("notifications") {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }
        }

        // El Snackbar flotante "estilo WhatsApp" que aparece por encima de todo
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 16.dp, end = 16.dp)
        ) { data ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF323232)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = EcoGreenPrimary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.NotificationsActive, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = data.visuals.message,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
