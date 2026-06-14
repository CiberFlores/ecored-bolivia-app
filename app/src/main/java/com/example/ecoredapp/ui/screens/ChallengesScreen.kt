package com.example.ecoredapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoredapp.ui.theme.EcoGreenPrimary
import com.example.ecoredapp.ui.theme.EcoSecondary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val points: Int = 0,
    val type: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToTienda: () -> Unit,
    onNavigateToPerfil: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    var challenges by remember { mutableStateOf<List<Challenge>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val sampleChallenges = listOf(
        Challenge("1", "Reciclador Novato", "Recicla 5 botellas de plástico esta semana.", 100, "Plástico"),
        Challenge("2", "Experto en Vidrio", "Lleva 3 frascos de vidrio al punto de acopio.", 150, "Vidrio"),
        Challenge("3", "Eco Ciudadano", "Reporta 2 puntos críticos de basura.", 200, "Reporte"),
        Challenge("4", "Cero Papel", "Recicla 2kg de papel o cartón.", 120, "Papel"),
        Challenge("5", "Limpia tu Barrio", "Participa en una jornada de limpieza.", 300, "Social")
    )

    LaunchedEffect(Unit) {
        db.collection("retos")
            .get()
            .addOnSuccessListener { result ->
                val firestoreChallenges = result.map { doc ->
                    Challenge(
                        id = doc.id,
                        title = doc.getString("titulo") ?: "",
                        description = doc.getString("descripcion") ?: "",
                        points = doc.getLong("puntos")?.toInt() ?: 0,
                        type = doc.getString("tipo") ?: ""
                    )
                }
                challenges = if (firestoreChallenges.isEmpty()) sampleChallenges else firestoreChallenges
                isLoading = false
            }
            .addOnFailureListener {
                challenges = sampleChallenges
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Retos Semanales", fontWeight = FontWeight.Bold) },
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EcoGreenPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(challenges) { challenge ->
                    ChallengeCard(challenge) { points ->
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            db.collection("usuarios").document(userId)
                                .update("puntos", FieldValue.increment(points.toLong()))
                                .addOnSuccessListener {
                                    // Enviar notificación de éxito
                                    val notification = hashMapOf(
                                        "title" to "¡Reto Completado!",
                                        "message" to "Has ganado $points puntos con el reto: ${challenge.title}",
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                    db.collection("usuarios").document(userId).collection("notificaciones").add(notification)
                                    
                                    Toast.makeText(context, "¡Reto completado! Ganaste $points puntos", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeCard(challenge: Challenge, onComplete: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = EcoGreenPrimary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = EcoGreenPrimary)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = challenge.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = challenge.description, fontSize = 13.sp, color = Color.Gray, maxLines = 2)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBC02D), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "+${challenge.points} Puntos", fontWeight = FontWeight.Bold, color = EcoGreenPrimary, fontSize = 13.sp)
                }
            }
            Button(
                onClick = { onComplete(challenge.points) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text("Completar", fontSize = 12.sp)
            }
        }
    }
}
