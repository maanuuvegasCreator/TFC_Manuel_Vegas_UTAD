package com.example.imdbapplogin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class MovieDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra("title") ?: "Sin título"
        val year = intent.getStringExtra("year") ?: "¿?"
        val poster = intent.getStringExtra("poster") ?: ""

        setContent {
            MovieDetailScreen(title, year, poster)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(title: String, year: String, posterUrl: String) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val density = LocalContext.current.resources.displayMetrics.density

    var isInFavorites by remember { mutableStateOf(false) }
    var rating by remember { mutableStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    var showStar by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(600),
        label = "rotation"
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F1B2B), Color(0xFF1C1C2D))
    )

    LaunchedEffect(Unit) {
        val email = user?.email ?: return@LaunchedEffect
        try {
            val favs = db.collection("favoritos")
                .whereEqualTo("email", email)
                .whereEqualTo("title", title)
                .get().await()
            isInFavorites = !favs.isEmpty

            val ratings = db.collection("valoraciones")
                .whereEqualTo("email", email)
                .whereEqualTo("title", title)
                .get().await()
            if (!ratings.isEmpty) {
                rating = (ratings.first()["rating"] as? Long)?.toInt() ?: 0
            }
        } catch (_: Exception) {
            Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f) // 75% del ancho de la pantalla
                    .aspectRatio(2f / 3f) // proporción tipo póster
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1A1A))
                    .clickable { flipped = !flipped }
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 8 * density
                    }
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {



            if (rotation <= 90f) {
                    Box {
                        AsyncImage(
                            model = posterUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )



                        if (isInFavorites) {
                            Text(
                                "⭐ Favorito",
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(10.dp)
                                    .background(Color(0xFF66BB6A), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2A38)),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎞️ $title", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Año de estreno: $year", color = Color.LightGray, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Pulsa la imagen para volver", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            MovieDetailButton(
                text = "🎬 Ver tráiler en YouTube",
                color = Color(0xFFFFC107)
            ) {
                val query = Uri.encode("$title trailer")
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query")))
            }

            MovieDetailButton(
                text = "📤 Compartir película",
                color = Color(0xFF03DAC5)
            ) {
                val text = "🎬 Mira esta película: $title ($year)\nhttps://www.youtube.com/results?search_query=${Uri.encode("$title trailer")}"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir película con..."))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                repeat(5) { i ->
                    val star = i + 1
                    Text(
                        if (rating >= star) "★" else "☆",
                        fontSize = 30.sp,
                        color = if (rating >= star) Color.Yellow else Color.White,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                rating = star
                                val email = user?.email ?: return@clickable
                                scope.launch {
                                    val docs = db.collection("valoraciones")
                                        .whereEqualTo("email", email)
                                        .whereEqualTo("title", title)
                                        .get().await()
                                    if (docs.isEmpty) {
                                        db.collection("valoraciones").add(
                                            hashMapOf("email" to email, "title" to title, "rating" to star)
                                        )
                                    } else {
                                        docs.forEach {
                                            db.collection("valoraciones").document(it.id)
                                                .update("rating", star)
                                        }
                                    }
                                    snackbarHostState.showSnackbar("Has calificado con $star estrellas")
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            MovieDetailButton(
                text = if (isInFavorites) "❌ Quitar de favoritos" else "⭐ Añadir a favoritos",
                color = if (isInFavorites) Color(0xFFFF7043) else Color.White.copy(alpha = 0.1f),
                textColor = Color.White
            ) {
                val email = user?.email ?: return@MovieDetailButton
                scope.launch {
                    val docs = db.collection("favoritos")
                        .whereEqualTo("email", email)
                        .whereEqualTo("title", title)
                        .get().await()
                    if (docs.isEmpty) {
                        db.collection("favoritos").add(
                            hashMapOf("email" to email, "title" to title, "year" to year, "poster" to posterUrl)
                        )
                        isInFavorites = true
                        showStar = true
                        snackbarHostState.showSnackbar("Película añadida a favoritos ⭐")
                        delay(1500)
                        showStar = false
                    } else {
                        docs.forEach { db.collection("favoritos").document(it.id).delete() }
                        isInFavorites = false
                        snackbarHostState.showSnackbar("Película eliminada de favoritos ❌")
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showStar,
            enter = fadeIn(),
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD700).copy(alpha = 0.85f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("⭐", fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun MovieDetailButton(
    text: String,
    color: Color,
    textColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = textColor),
        elevation = ButtonDefaults.buttonElevation(8.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
