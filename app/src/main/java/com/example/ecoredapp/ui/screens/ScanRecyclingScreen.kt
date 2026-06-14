package com.example.ecoredapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.ecoredapp.ui.theme.EcoGreenPrimary
import com.example.ecoredapp.ui.theme.EcoSecondary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanRecyclingScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToTienda: () -> Unit,
    onNavigateToPerfil: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadSuccess by remember { mutableStateOf(false) }

    // Launcher para Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri
    }

    // Inicializar Cloudinary
    LaunchedEffect(Unit) {
        try {
            MediaManager.init(context)
        } catch (e: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subir Reciclaje", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 12.dp) {
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
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sube una foto de tu reciclaje desde la galería para ganar EcoPuntos.",
                fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 32.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4))
            ) {
                if (imageUri == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary)
                            ) {
                                Text("Seleccionar de Galería")
                            }
                        }
                    }
                } else {
                    AsyncImage(
                        model = imageUri, contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (!uploadSuccess) {
                Button(
                    onClick = {
                        if (imageUri != null) {
                            isUploading = true
                            MediaManager.get().upload(imageUri)
                                .callback(object : UploadCallback {
                                    override fun onStart(requestId: String?) {}
                                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                                        val userId = auth.currentUser?.uid
                                        if (userId != null) {
                                            db.collection("usuarios").document(userId)
                                                .update("puntos", FieldValue.increment(50))
                                                .addOnSuccessListener {
                                                    // Notificación
                                                    val notification = hashMapOf(
                                                        "title" to "Reciclaje Subido",
                                                        "message" to "Has ganado 50 EcoPuntos por subir tu reciclaje.",
                                                        "timestamp" to System.currentTimeMillis()
                                                    )
                                                    db.collection("usuarios").document(userId).collection("notificaciones").add(notification)
                                                    
                                                    isUploading = false
                                                    uploadSuccess = true
                                                    Toast.makeText(context, "¡50 EcoPuntos ganados!", Toast.LENGTH_LONG).show()
                                                }
                                        }
                                    }
                                    override fun onError(requestId: String?, error: ErrorInfo?) {
                                        isUploading = false
                                        Toast.makeText(context, "Error: ${error?.description}", Toast.LENGTH_SHORT).show()
                                    }
                                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                                }).dispatch()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    enabled = !isUploading && imageUri != null
                ) {
                    if (isUploading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("SUBIR Y GANAR PUNTOS", fontWeight = FontWeight.Bold)
                }
                
                if (imageUri != null && !isUploading) {
                    TextButton(onClick = { imageUri = null }) {
                        Text("Cambiar imagen", color = Color.Red)
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(64.dp))
                    Text("¡Subida Exitosa!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Volver al Inicio") }
                }
            }
        }
    }
}
