package com.example.ecoredapp.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportTrashScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToTienda: () -> Unit,
    onNavigateToPerfil: () -> Unit
) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(Unit) {
        try {
            MediaManager.init(context)
        } catch (e: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportar Basura", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ayúdanos a identificar puntos críticos de basura en la ciudad.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                if (imageUri == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = { launcher.launch("image/*") }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Seleccionar Foto")
                        }
                    }
                } else {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (imageUri != null) {
                        isUploading = true
                        MediaManager.get().upload(imageUri)
                            .callback(object : UploadCallback {
                                override fun onStart(requestId: String?) {}
                                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                                    val imageUrl = resultData?.get("secure_url") as String
                                    saveReportToFirestore(imageUrl, description, context) {
                                        isUploading = false
                                        onBack()
                                    }
                                }
                                override fun onError(requestId: String?, error: ErrorInfo?) {
                                    isUploading = false
                                    Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
                                }
                                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                            }).dispatch()
                    } else {
                        Toast.makeText(context, "Por favor selecciona una foto", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ENVIAR REPORTE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun saveReportToFirestore(url: String, desc: String, context: android.content.Context, onSuccess: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val report = hashMapOf(
        "imageUrl" to url,
        "description" to desc,
        "timestamp" to com.google.firebase.Timestamp.now(),
        "status" to "Pendiente"
    )

    db.collection("reportes").add(report)
        .addOnSuccessListener {
            Toast.makeText(context, "Reporte enviado con éxito", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error al guardar reporte", Toast.LENGTH_SHORT).show()
        }
}
