package com.example.ecoredapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ecoredapp.ui.theme.EcoGreenPrimary
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToTienda: () -> Unit,
    onNavigateToPerfil: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Puntos de Reciclaje", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 12.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = onNavigateToHome
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Tienda") },
                    label = { Text("Tienda") },
                    selected = false,
                    onClick = onNavigateToTienda
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = false,
                    onClick = onNavigateToPerfil
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF1F3F4))
        ) {
            AndroidView(
                factory = { context ->
                    Configuration.getInstance().userAgentValue = context.packageName
                    MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        val mapController = controller
                        mapController.setZoom(15.0)
                        
                        // Centrado en una ubicación base (Cochabamba como ejemplo)
                        val centerPoint = GeoPoint(-17.3895, -66.1568)
                        mapController.setCenter(centerPoint)
                        
                        // Lista extendida de puntos de reciclaje
                        val points = listOf(
                            Triple(-17.3895, -66.1568, "Punto Central - Plaza Principal"),
                            Triple(-17.3930, -66.1520, "EcoPunto Norte - Av. América"),
                            Triple(-17.3850, -66.1600, "Centro de Reciclaje Sur - Av. Petrolera"),
                            Triple(-17.3750, -66.1450, "Punto Verde Este - Sacaba"),
                            Triple(-17.4000, -66.1700, "Recicladora Quillacollo"),
                            Triple(-17.3800, -66.1550, "Contenedor Inteligente - El Prado"),
                            Triple(-17.4100, -66.1300, "EcoEstación UV - Zona Universitaria"),
                            Triple(-17.3600, -66.1800, "Punto Limpio Oeste - Tiquipaya")
                        )

                        points.forEach { (lat, lon, title) ->
                            val marker = Marker(this).apply {
                                position = GeoPoint(lat, lon)
                                this.title = title
                                subDescription = "Recibe: Plásticos, Papel, Vidrio y Metales"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(marker)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
