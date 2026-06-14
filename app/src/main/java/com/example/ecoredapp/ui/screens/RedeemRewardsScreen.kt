package com.example.ecoredapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ecoredapp.ui.theme.EcoGreenPrimary
import com.example.ecoredapp.ui.theme.EcoSecondary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

data class Reward(
    val id: String = "",
    val name: String = "",
    val pointsCost: Int = 0,
    val imageUrl: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemRewardsScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPerfil: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    var rewards by remember { mutableStateOf<List<Reward>>(emptyList()) }
    var userPoints by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val sampleRewards = listOf(
        Reward("1", "Termo Ecológico", 150, "https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=500&q=60"),
        Reward("2", "Bolsa de Tela", 50, "https://images.unsplash.com/photo-1597348989645-46b190ce4918?auto=format&fit=crop&w=500&q=60"),
        Reward("3", "Cepillo de Bambú", 30, "https://images.unsplash.com/photo-1605197148906-8167f5b72186?auto=format&fit=crop&w=500&q=60"),
        Reward("4", "Libreta Reciclada", 80, "https://images.unsplash.com/photo-1544816155-12df9643f363?auto=format&fit=crop&w=500&q=60"),
        Reward("5", "Llavero Eco", 20, "https://images.unsplash.com/photo-1544022613-e87ca75a784a?auto=format&fit=crop&w=500&q=60"),
        Reward("6", "Set de Cubiertos", 120, "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?auto=format&fit=crop&w=500&q=60")
    )

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("usuarios").document(userId).addSnapshotListener { snapshot, _ ->
                userPoints = snapshot?.getLong("puntos")?.toInt() ?: 0
            }
        }

        db.collection("Recompensas_Disponibles").get().addOnSuccessListener { result ->
            val firestoreRewards = result.map { doc ->
                Reward(
                    id = doc.id,
                    name = doc.getString("nombre") ?: "",
                    pointsCost = doc.getLong("puntos_costo")?.toInt() ?: 0,
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
            }
            rewards = if (firestoreRewards.isEmpty()) sampleRewards else firestoreRewards
            isLoading = false
        }.addOnFailureListener {
            rewards = sampleRewards
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tienda de Regalos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Surface(
                        color = Color(0xFFFFF9C4),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBC02D), modifier = Modifier.size(18.dp))
                            Text(text = " $userPoints", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
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
                    selected = true,
                    onClick = { /* Ya estamos aquí */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EcoGreenPrimary,
                        indicatorColor = EcoSecondary.copy(alpha = 0.2f)
                    )
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FA)),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(rewards) { reward ->
                    RewardCard(reward, userPoints >= reward.pointsCost) {
                        redeemReward(reward, auth.currentUser?.uid, db, context)
                    }
                }
            }
        }
    }
}

@Composable
fun RewardCard(reward: Reward, canAfford: Boolean, onRedeem: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = reward.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(140.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = reward.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                Text(text = "${reward.pointsCost} Puntos", color = Color(0xFF2E7D32), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRedeem,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Canjear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun redeemReward(reward: Reward, userId: String?, db: FirebaseFirestore, context: android.content.Context) {
    if (userId == null) return
    val userRef = db.collection("usuarios").document(userId)
    
    db.runTransaction { transaction ->
        val snapshot = transaction.get(userRef)
        val currentPoints = snapshot.getLong("puntos") ?: 0
        if (currentPoints >= reward.pointsCost) {
            transaction.update(userRef, "puntos", currentPoints - reward.pointsCost)
            
            // Registro de canje
            val redemption = hashMapOf(
                "userId" to userId,
                "rewardId" to reward.id,
                "rewardName" to reward.name,
                "timestamp" to FieldValue.serverTimestamp()
            )
            db.collection("canjes").add(redemption)
            
            // Envío de NOTIFICACIÓN
            val notification = hashMapOf(
                "title" to "¡Canje Realizado!",
                "message" to "Has canjeado ${reward.name} exitosamente.",
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("usuarios").document(userId).collection("notificaciones").add(notification)
            
        } else {
            throw Exception("Puntos insuficientes")
        }
    }.addOnSuccessListener {
        Toast.makeText(context, "¡Canje exitoso!", Toast.LENGTH_LONG).show()
    }.addOnFailureListener { e ->
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
