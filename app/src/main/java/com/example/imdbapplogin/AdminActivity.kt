package com.example.imdbapplogin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.imdbapplogin.admin.ManageUsersActivity
import com.example.imdbapplogin.ui.theme.IMDbAppLoginTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.graphics.graphicsLayer


class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IMDbAppLoginTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AdminDashboard()
                }
            }
        }
    }
}
@Composable
fun AdminDashboard() {
    val context = LocalContext.current

    val gradientBackground = Brush.verticalGradient(
        listOf(Color(0xFF0F1B2B), Color(0xFF1C1C2D), Color(0xFF0F1B2B))
    )

    // Animación de entrada de la tarjeta
    val animatedAlpha = remember { Animatable(0f) }
    val animatedOffsetY = remember { Animatable(50f) }

    LaunchedEffect(Unit) {
        animatedAlpha.animateTo(1f, animationSpec = tween(durationMillis = 800))
        animatedOffsetY.animateTo(0f, animationSpec = tween(durationMillis = 800))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo mucho más grande
            Image(
                painter = painterResource(id = R.drawable.logo_cineverse), // Asegúrate de tenerlo en drawable
                contentDescription = "Logo CineVerse",
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
            )

            Text(
                text = "Panel de Administración",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            // Franja luminosa animada (decorativa)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFFFFC107),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tarjeta animada
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = animatedAlpha.value
                        translationY = animatedOffsetY.value
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2A2F3D)
                ),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    AdminActionButton("Gestionar Usuarios", Icons.Default.Group) {
                        context.startActivity(Intent(context, ManageUsersActivity::class.java))
                    }

                    AdminActionButton("Cerrar Sesión", Icons.Default.ExitToApp) {
                        FirebaseAuth.getInstance().signOut()
                        context.startActivity(Intent(context, MainActivity::class.java))
                        (context as? ComponentActivity)?.finish()
                    }
                }
            }
        }
    }
}


@Composable
fun AdminActionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF4E5A71),
            contentColor = Color.White
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}
