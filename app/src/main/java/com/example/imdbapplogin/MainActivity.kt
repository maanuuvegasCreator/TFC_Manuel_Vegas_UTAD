package com.example.imdbapplogin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            val uid = auth.currentUser?.uid
            val db = FirebaseFirestore.getInstance()

            if (uid != null) {
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { document ->
                        val role = document.getString("role")
                        if (role == "admin") {
                            startActivity(Intent(this, AdminActivity::class.java))
                        } else {
                            startActivity(Intent(this, HomeActivity::class.java))
                        }
                        finish()
                    }
                    .addOnFailureListener {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
        } else {
            setContent {
                IMDbAppLoginTheme {
                    LoginScreen(auth)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var showAlert by remember { mutableStateOf(false) }

    LaunchedEffect(alertMessage) {
        if (showAlert) {
            delay(3500)
            showAlert = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_cine),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { it / 2 })
            ) {
                LoginCard(
                    auth = auth,
                    context = context,
                    coroutineScope = coroutineScope,
                    email = email,
                    password = password,
                    showPassword = showPassword,
                    isLoading = isLoading,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onShowAlert = { showAlert = it },
                    onAlertMessageChange = { alertMessage = it },
                    onLoadingChange = { isLoading = it },
                    onTogglePasswordVisibility = { showPassword = !showPassword }
                )
            }
        }

        if (showAlert) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                containerColor = Color.Black.copy(alpha = 0.85f)
            ) {
                Text(text = alertMessage, color = Color.White)
            }
        }
    }
}

@Composable
fun LoginCard(
    auth: FirebaseAuth,
    context: android.content.Context,
    coroutineScope: CoroutineScope,
    email: String,
    password: String,
    showPassword: Boolean,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onShowAlert: (Boolean) -> Unit,
    onAlertMessageChange: (String) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    onTogglePasswordVisibility: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.05f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedLogo()

        Text(
            text = "Explora estrenos, tráilers y salas",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.75f),
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("📧 Correo electrónico", color = Color.White, fontSize = 15.sp) },
            textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
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
            onValueChange = onPasswordChange,
            label = { Text("🔒 Contraseña", color = Color.White, fontSize = 15.sp) },
            textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon: ImageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = onTogglePasswordVisibility) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFFFC107),
                unfocusedBorderColor = Color(0xFFFFC107).copy(alpha = 0.5f),
                cursorColor = Color.White,
                focusedLabelColor = Color(0xFFFFC107),
                unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
            )
        )

        LoginButtonModern(isLoading) {
            coroutineScope.launch {
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    onLoadingChange(true)
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            onLoadingChange(false)
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid
                                val db = FirebaseFirestore.getInstance()
                                if (uid != null) {
                                    db.collection("users").document(uid).get()
                                        .addOnSuccessListener { document ->
                                            val role = document.getString("role")
                                            Toast.makeText(context, "Rol detectado: $role", Toast.LENGTH_SHORT).show()
                                            if (role == "admin") {
                                                context.startActivity(Intent(context, AdminActivity::class.java))
                                            } else {
                                                context.startActivity(Intent(context, HomeActivity::class.java))
                                            }
                                            (context as? ComponentActivity)?.finish()
                                        }
                                        .addOnFailureListener {
                                            onAlertMessageChange("⚠️ No se pudo verificar el rol")
                                            onShowAlert(true)
                                        }
                                } else {
                                    onAlertMessageChange("⚠️ No se encontró el UID del usuario")
                                    onShowAlert(true)
                                }
                            } else {
                                onAlertMessageChange("❌ Credenciales incorrectas")
                                onShowAlert(true)
                            }
                        }
                } else {
                    onAlertMessageChange("⚠️ Completa todos los campos")
                    onShowAlert(true)
                }
            }
        }

        Text(
            "¿Olvidaste la contraseña?",
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.clickable {
                context.startActivity(Intent(context, ForgotPasswordActivity::class.java))
            }
        )

        Text(
            "Crear cuenta",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFC107),
            modifier = Modifier.clickable {
                context.startActivity(Intent(context, RegisterActivity::class.java))
            }
        )
    }
}

@Composable
fun AnimatedLogo() {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(900))
    }
    Image(
        painter = painterResource(id = R.drawable.logo_cineverse),
        contentDescription = "Logo CineVerse",
        modifier = Modifier
            .size(220.dp)
            .alpha(alpha.value)
    )
}

@Composable
fun LoginButtonModern(isLoading: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFFFC107), Color(0xFFFF8C00))
                )
            )
            .clickable(interactionSource = interactionSource, indication = null) {
                if (!isLoading) onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(26.dp)
            )
        } else {
            Text("Iniciar sesión", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}