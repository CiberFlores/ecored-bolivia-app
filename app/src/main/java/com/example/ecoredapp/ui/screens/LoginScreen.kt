package com.example.ecoredapp.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoredapp.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Estudiante") }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(EcoGreenPrimary, EcoGreenLight)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // Fondo decorativo superior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.TopCenter)
                .background(gradientBrush)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono de la App
            Surface(
                modifier = Modifier.size(90.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Recycling,
                        contentDescription = null,
                        tint = EcoGreenPrimary,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isLoginMode) "¡Bienvenido!" else "Crear Cuenta",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EcoGreenPrimary
                    )
                    Text(
                        text = if (isLoginMode) "Inicia sesión para reciclar" else "Únete a la red ecológica",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = "" },
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = EcoGreenPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = EcoGreenPrimary,
                            cursorColor = EcoGreenPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = "" },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EcoGreenPrimary) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = EcoGreenPrimary,
                            cursorColor = EcoGreenPrimary
                        )
                    )

                    if (isLoginMode) {
                        TextButton(
                            onClick = {
                                if (email.isNotEmpty()) {
                                    isLoading = true
                                    auth.sendPasswordResetEmail(email.trim())
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful) {
                                                Toast.makeText(context, "Correo de recuperación enviado a $email", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Error: ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(context, "Por favor, ingresa tu correo primero", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.align(Alignment.End),
                            enabled = !isLoading
                        ) {
                            Text(text = "¿Olvidaste tu contraseña?", fontSize = 12.sp, color = EcoGreenPrimary)
                        }
                    }

                    AnimatedVisibility(visible = !isLoginMode) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Tipo de usuario", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                FilterChip(
                                    selected = selectedRole == "Estudiante",
                                    onClick = { selectedRole = "Estudiante" },
                                    label = { Text("Estudiante") }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                FilterChip(
                                    selected = selectedRole == "Reciclador",
                                    onClick = { selectedRole = "Reciclador" },
                                    label = { Text("Reciclador") }
                                )
                            }
                        }
                    }

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isLoginMode) 16.dp else 32.dp))

                    Button(
                        onClick = {
                            if (email.isNotEmpty() && (password.isNotEmpty() || !isLoginMode)) {
                                isLoading = true
                                if (isLoginMode) {
                                    auth.signInWithEmailAndPassword(email.trim(), password)
                                        .addOnCompleteListener { task ->
                                            isLoading = false
                                            if (task.isSuccessful) onLoginSuccess()
                                            else {
                                                val ex = task.exception
                                                errorMessage = when {
                                                    ex is FirebaseAuthInvalidUserException -> "Usuario no encontrado."
                                                    ex is FirebaseAuthInvalidCredentialsException -> "Credenciales incorrectas."
                                                    else -> "Error: ${ex?.localizedMessage}"
                                                }
                                            }
                                        }
                                } else {
                                    if (password.length < 6) {
                                        isLoading = false
                                        errorMessage = "La contraseña debe tener al menos 6 caracteres"
                                        return@Button
                                    }
                                    auth.createUserWithEmailAndPassword(email.trim(), password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val uid = auth.currentUser?.uid
                                                if (uid != null) {
                                                    val user = hashMapOf(
                                                        "email" to email.trim(),
                                                        "rol" to selectedRole,
                                                        "puntos" to 0
                                                    )
                                                    db.collection("usuarios").document(uid).set(user)
                                                        .addOnSuccessListener {
                                                            isLoading = false
                                                            isLoginMode = true
                                                            Toast.makeText(context, "Cuenta creada, inicia sesión", Toast.LENGTH_SHORT).show()
                                                            auth.signOut()
                                                        }
                                                }
                                            } else {
                                                isLoading = false
                                                errorMessage = if (task.exception is FirebaseAuthUserCollisionException) "El correo ya existe" else "Error al registrar"
                                            }
                                        }
                                }
                            } else errorMessage = "Completa todos los campos"
                        },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EcoGreenPrimary),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text(if (isLoginMode) "INGRESAR" else "REGISTRARSE", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { if (!isLoading) isLoginMode = !isLoginMode }) {
                        Text(
                            text = if (isLoginMode) "¿No tienes cuenta? Regístrate" else "¿Ya tienes cuenta? Ingresa",
                            color = EcoGreenPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
