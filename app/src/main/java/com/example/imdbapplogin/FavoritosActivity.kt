package com.example.imdbapplogin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Favorito(
    val id: String = "",
    val title: String = "",
    val year: String = "",
    val poster: String = ""
)

class FavoritosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FavoritosScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen() {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    var favoritos by remember { mutableStateOf<List<Favorito>>(emptyList()) }
    var favoritoAEliminar by remember { mutableStateOf<Favorito?>(null) }

    // Fondo degradado animado
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F1B2B), Color(0xFF1C1C2D)),
        startY = offset,
        endY = offset + 1000f
    )

    // Escucha en tiempo real
    DisposableEffect(Unit) {
        val listener = user?.email?.let { email ->
            db.collection("favoritos")
                .whereEqualTo("email", email)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Toast.makeText(context, "Error al escuchar cambios", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        favoritos = snapshots.documents.map {
                            Favorito(
                                id = it.id,
                                title = it.getString("title") ?: "",
                                year = it.getString("year") ?: "",
                                poster = it.getString("poster") ?: ""
                            )
                        }
                    }
                }
        }
        onDispose { listener?.remove() }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MIS FAVORITOS",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107),
                        style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 8f))
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, HomeActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F1B2B),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush)
                .padding(padding)
        ) {
            if (favoritos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes favoritos aún", color = Color.White, fontSize = 18.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(favoritos) { fav ->
                        FavoritoCard(fav) {
                            favoritoAEliminar = fav
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación
    favoritoAEliminar?.let { fav ->
        AlertDialog(
            onDismissRequest = { favoritoAEliminar = null },
            title = { Text("¿Eliminar favorito?", color = Color.White) },
            text = { Text("¿Seguro que quieres quitar \"${fav.title}\" de tus favoritos?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    db.collection("favoritos").document(fav.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                        }
                    favoritoAEliminar = null
                }) {
                    Text("Sí", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { favoritoAEliminar = null }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = Color(0xFF203A43)
        )
    }
}

@Composable
fun FavoritoCard(fav: Favorito, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = fav.poster,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .padding(end = 8.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(fav.title, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Año: ${fav.year}", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
            }
            Text(
                text = "🗑",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .clickable { onDelete() },
                color = Color.Red.copy(alpha = 0.8f)
            )
        }
    }
}
