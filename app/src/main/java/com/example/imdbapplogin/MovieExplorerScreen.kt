package com.example.imdbapplogin.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.imdbapplogin.HomeActivity
import com.example.imdbapplogin.MovieDetailActivity
import com.example.imdbapplogin.model.Movie
import com.example.imdbapplogin.network.RetrofitInstance
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieExplorerScreen() {
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var movieList by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val randomTerms = listOf("matrix", "avengers", "batman", "spider", "mission")

    LaunchedEffect(Unit) {
        val initialQuery = randomTerms.random()
        isLoading = true
        try {
            val response = RetrofitInstance.omdbApi.searchMovies(initialQuery, "181aeb53")
            movieList = if (response.Response == "True" && response.Search != null) {
                response.Search
            } else emptyList()
        } catch (e: Exception) {
            error = "Error al cargar películas: ${e.message}"
        }
        isLoading = false
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F1B2B), Color(0xFF1C1C2D))
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "🎬 Explora Películas",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color(0xFFFFC107)
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    coroutineScope.launch {
                        isLoading = true
                        error = null
                        try {
                            val response = RetrofitInstance.omdbApi.searchMovies(it, "181aeb53")
                            movieList = if (response.Response == "True" && response.Search != null) {
                                response.Search
                            } else emptyList()
                        } catch (e: Exception) {
                            error = "Error al buscar películas: ${e.message}"
                        }
                        isLoading = false
                    }
                },
                placeholder = { Text("Buscar películas...", color = Color.White.copy(0.5f)) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(30.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(0.08f),
                    unfocusedContainerColor = Color.White.copy(0.05f),
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFFFC107),
                    unfocusedBorderColor = Color.Gray
                ),
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFFC107))
                    }
                }

                error != null -> {
                    Text(error ?: "", color = Color.Red, modifier = Modifier.padding(16.dp))
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(movieList) { index, movie ->
                            val randomRating = remember { Random.nextDouble(6.5, 10.0) }
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(500)) + slideInHorizontally(initialOffsetX = { it })
                            ) {
                                MovieCardModern(movie = movie, rating = String.format("%.1f", randomRating))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieCardModern(movie: Movie, rating: String) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                val intent = Intent(context, MovieDetailActivity::class.java).apply {
                    putExtra("title", movie.Title)
                    putExtra("year", movie.Year)
                    putExtra("poster", movie.Poster)
                }
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2A38)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            AsyncImage(
                model = movie.Poster,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(movie.Title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                Text("Año: ${movie.Year}", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                Text("⭐ Valoración: $rating", fontSize = 13.sp, color = Color(0xFFFFC107), fontWeight = FontWeight.SemiBold)
                Text("Pulsa para más info", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}
