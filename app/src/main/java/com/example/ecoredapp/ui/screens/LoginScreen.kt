package com.example.ecoredapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    // Estado para guardar el rol seleccionado
    var selectedRole by remember { mutableStateOf("Estudiante") }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance() // Inicializamos tu nueva base de datos

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF1F3F4)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isLoginMode) "¡Hola de nuevo!" else "Únete a EcoRed",
                    fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32)
                )
                Text(
                    text = if (isLoginMode) "Inicia sesión para continuar" else "Crea tu cuenta para reciclar",
                    fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF81C784)) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32), cursorColor = Color(0xFF2E7D32)), singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Contraseña (mín 6 letras)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF81C784)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32), cursorColor = Color(0xFF2E7D32)), singleLine = true
                )

                // Botones para elegir rol (Solo aparecen al registrarse)
                if (!isLoginMode) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "¿Qué tipo de usuario eres?", fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedRole == "Estudiante",
                            onClick = { selectedRole = "Estudiante" },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2E7D32))
                        )
                        Text(text = "Estudiante")

                        Spacer(modifier = Modifier.width(16.dp))

                        RadioButton(
                            selected = selectedRole == "Reciclador",
                            onClick = { selectedRole = "Reciclador" },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2E7D32))
                        )
                        Text(text = "Reciclador")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            if (isLoginMode) {
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            onLoginSuccess()
                                        } else {
                                            Toast.makeText(context, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val userId = auth.currentUser?.uid
                                            if (userId != null) {
                                                // Preparamos los datos para tu base vacía
                                                val userMap = hashMapOf(
                                                    "email" to email,
                                                    "rol" to selectedRole
                                                )
                                                // Los enviamos
                                                db.collection("usuarios").document(userId)
                                                    .set(userMap)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(context, "Cuenta creada como $selectedRole", Toast.LENGTH_LONG).show()
                                                        onLoginSuccess()
                                                    }
                                            }
                                        } else {
                                            Toast.makeText(context, "Error al registrarse", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Llena todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isLoginMode) "ENTRAR" else "REGISTRARSE", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { isLoginMode = !isLoginMode }) {
                    Text(
                        text = if (isLoginMode) "Crear una cuenta nueva" else "¿Ya tienes cuenta? Inicia sesión",
                        color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}