package com.example.ecoredapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoredapp.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToTienda: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToChallenges: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogout: () -> Unit,
    initialTab: Int = 0
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Listener para notificaciones flotantes en tiempo real (Estilo WhatsApp)
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("usuarios").document(userId).collection("notificaciones")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val doc = snapshot?.documents?.firstOrNull()
                    if (doc != null) {
                        val timestamp = doc.getLong("timestamp") ?: 0L
                        val currentTime = System.currentTimeMillis()
                        // Solo mostrar si la notificación ocurrió hace menos de 10 segundos
                        if (currentTime - timestamp < 10000) {
                            val title = doc.getString("title") ?: "EcoRed"
                            val message = doc.getString("message") ?: ""
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "🔔 $title: $message",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                }
        }
    }

    Scaffold(
        snackbarHost = {
            // Posicionamos el Snackbar arriba para el efecto flotante tipo WhatsApp
            Box(modifier = Modifier.fillMaxSize()) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) { data ->
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = EcoGreenPrimary),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NotificationsActive, null, tint = Color.White)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = data.visuals.message,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 12.dp,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EcoGreenPrimary,
                        indicatorColor = EcoSecondary.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Tienda") },
                    label = { Text("Tienda") },
                    selected = false,
                    onClick = { onNavigateToTienda() },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EcoGreenPrimary,
                        indicatorColor = EcoSecondary.copy(alpha = 0.2f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> InicioContent(
                    onScan = onNavigateToScan,
                    onMap = onNavigateToMap,
                    onChallenges = onNavigateToChallenges,
                    onReport = onNavigateToReport,
                    onNotifications = onNavigateToNotifications
                )
                2 -> PerfilContent(onLogout = onLogout)
            }
        }
    }
}

@Composable
fun InicioContent(onScan: () -> Unit, onMap: () -> Unit, onChallenges: () -> Unit, onReport: () -> Unit, onNotifications: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var userPoints by remember { mutableStateOf(0) }
    var userName by remember { mutableStateOf("EcoUsuario") }

    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { uid ->
            db.collection("usuarios").document(uid).addSnapshotListener { snapshot, _ ->
                userPoints = snapshot?.getLong("puntos")?.toInt() ?: 0
                val email = snapshot?.getString("email") ?: ""
                userName = email.split("@")[0].replaceFirstChar { it.uppercase() }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cabecera de bienvenida
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "¡Hola, $userName!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                Text(text = "Tu progreso hoy", fontSize = 14.sp, color = TextGray)
            }
            Surface(
                modifier = Modifier.size(48.dp).clickable { onNotifications() },
                shape = CircleShape,
                color = EcoGreenPrimary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = EcoGreenPrimary)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Tarjeta de Puntos Destacada
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(EcoGreenPrimary, EcoGreenLight))),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Stars, contentDescription = null, tint = EcoTertiary, modifier = Modifier.size(32.dp))
                    Text(text = userPoints.toString(), fontSize = 54.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text(text = "EcoPuntos acumulados", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Acciones Ecológicas",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.Start),
            color = TextDark
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Grid de Botones con mejor aspecto
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ModernMenuButton(Modifier.weight(1f), "Subir Foto", Icons.Default.CloudUpload, EcoGreenPrimary, onScan)
            ModernMenuButton(Modifier.weight(1f), "Mapa", Icons.Default.Public, Color(0xFF1E88E5), onMap)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ModernMenuButton(Modifier.weight(1f), "Retos", Icons.Default.EmojiEvents, EcoTertiary, onChallenges)
            ModernMenuButton(Modifier.weight(1f), "Reportar", Icons.Default.AddLocationAlt, Color(0xFFE53935), onReport)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ModernMenuButton(modifier: Modifier, title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
        }
    }
}

@Composable
fun PerfilContent(onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Mi Perfil", fontSize = 26.sp, fontWeight = FontWeight.Black, color = EcoGreenPrimary)
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(4.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = EcoGreenPrimary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(70.dp), tint = EcoGreenPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = user?.email ?: "EcoUsuario", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Text(text = "Miembro activo", fontSize = 14.sp, color = EcoGreenPrimary, fontWeight = FontWeight.Medium)
        
        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                ProfileDetailRow(Icons.Default.Email, "Email", user?.email ?: "")
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = BackgroundGray)
                ProfileDetailRow(Icons.Default.Verified, "Certificación", "Ambiental Gold")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                user?.email?.let { email ->
                    auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Correo de recuperación enviado a $email", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = EcoGreenPrimary)
        ) {
            Icon(Icons.Default.LockReset, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "CAMBIAR CONTRASEÑA", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                auth.signOut()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEEBEE), contentColor = Color(0xFFD32F2F))
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "CERRAR SESIÓN", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(8.dp),
            color = EcoGreenPrimary.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = EcoGreenPrimary, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = TextGray)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
    }
}
