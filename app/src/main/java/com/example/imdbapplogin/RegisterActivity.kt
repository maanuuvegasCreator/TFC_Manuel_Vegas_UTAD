package com.example.imdbapplogin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.imdbapplogin.ui.theme.IMDbAppLoginTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContent {
            IMDbAppLoginTheme {
                RegisterScreen(auth)
            }
        }
    }
}

@Composable
fun RegisterScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val filledFields = listOf(email, password, confirmPassword).count { it.isNotEmpty() }
    val progress = filledFields / 3f

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_cine),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "REGISTRO",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFC107)
                )

                Text(
                    "Crea una cuenta para comenzar",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                LinearProgressIndicator(
                    progress = progress,
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("📧 Correo electrónico", color = Color.White, fontSize = 15.sp) },
                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.04f)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFC107),
                        unfocusedBorderColor = Color(0xFFFFC107).copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedLabelColor = Color(0xFFFFC107),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    )
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("🔒 Contraseña", color = Color.White, fontSize = 15.sp) },
                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.04f)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFC107),
                        unfocusedBorderColor = Color(0xFFFFC107).copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedLabelColor = Color(0xFFFFC107),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    )
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("🔐 Confirmar contraseña", color = Color.White, fontSize = 15.sp) },
                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.04f)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFC107),
                        unfocusedBorderColor = Color(0xFFFFC107).copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedLabelColor = Color(0xFFFFC107),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    )
                )

                LoginButtonModern(isLoading = false) {
                    if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                        if (password == confirmPassword) {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val uid = task.result.user?.uid
                                        val db = FirebaseFirestore.getInstance()

                                        val userMap = hashMapOf(
                                            "email" to email,
                                            "nombre" to "",
                                            "role" to "user"
                                        )

                                        uid?.let {
                                            db.collection("users").document(it).set(userMap)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                                    context.startActivity(Intent(context, MainActivity::class.java))
                                                }
                                                .addOnFailureListener {
                                                    Toast.makeText(context, "Error al guardar en Firestore", Toast.LENGTH_LONG).show()
                                                }
                                        }
                                    } else {
                                        Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        } else {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    }
                }

                Text(
                    text = "¿Ya tienes cuenta? Inicia sesión",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, MainActivity::class.java))
                    }
                )
            }
        }
    }
}
