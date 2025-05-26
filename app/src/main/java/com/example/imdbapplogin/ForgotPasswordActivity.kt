package com.example.imdbapplogin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.imdbapplogin.ui.theme.IMDbAppLoginTheme
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContent {
            IMDbAppLoginTheme {
                ForgotPasswordScreen(auth)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

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

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("", color = Color.Transparent) },
                    navigationIcon = {
                        IconButton(onClick = {
                            context.startActivity(Intent(context, MainActivity::class.java))
                            (context as? ComponentActivity)?.finish()
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800)) + slideInVertically(initialOffsetY = { -300 }, animationSpec = tween(800))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp)
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "\uD83D\uDCE7",
                        fontSize = 56.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "RECUPERAR CONTRASEÑA",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFC107),
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.8f),
                                blurRadius = 8f
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Introduce tu correo y te enviaremos un enlace para cambiar tu contraseña.",
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo electrónico", color = Color.White, fontSize = 15.sp) },
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

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFFC107), Color(0xFFFF8C00))
                                )
                            )
                            .clickable {
                                if (email.isNotEmpty()) {
                                    auth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    context,
                                                    "Correo de recuperación enviado",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                context.startActivity(Intent(context, MainActivity::class.java))
                                                (context as? ComponentActivity)?.finish()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Error: ${task.exception?.message}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                } else {
                                    Toast.makeText(context, "Por favor, introduce tu correo", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Enviar correo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}