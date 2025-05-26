// HomeActivity.kt
package com.example.imdbapplogin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.imdbapplogin.trivial.TrivialActivity
import com.example.imdbapplogin.ui.theme.IMDbAppLoginTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.sign

class HomeActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            IMDbAppLoginTheme {
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = false
                    )
                }

                val context = this
                var selectedScreen by remember { mutableStateOf("home") }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0F1B2B),
                                    Color(0xFF1C1C2D)
                                )
                            )
                        )
                ) {
                    Scaffold(
                        containerColor = Color.Transparent,
                        bottomBar = {
                            BottomNavBar(
                                currentRoute = selectedScreen,
                                onItemSelected = { newRoute ->
                                    selectedScreen = newRoute
                                    when (newRoute) {
                                        "home" -> {}
                                        "explorer" -> startActivity(Intent(context, MovieExplorerActivity::class.java))
                                        "mapa" -> startActivity(Intent(context, MapaActivity::class.java))
                                        "trivial" -> startActivity(Intent(context, TrivialActivity::class.java))
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        if (selectedScreen == "home") {
                            HomeScreen(
                                email = auth.currentUser?.email ?: "Usuario",
                                onLogout = {
                                    auth.signOut()
                                    startActivity(Intent(context, MainActivity::class.java))
                                    finish()
                                },
                                onOpenExplorer = {
                                    selectedScreen = "explorer"
                                    startActivity(Intent(context, MovieExplorerActivity::class.java))
                                },
                                onOpenFavorites = {
                                    selectedScreen = "favoritos"
                                    startActivity(Intent(context, FavoritosActivity::class.java))
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    email: String,
    onLogout: () -> Unit,
    onOpenExplorer: () -> Unit,
    onOpenFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    val frases = listOf(
        "\"El cine es el arte de contar sin decir una palabra.\"",
        "\"Un día sin cine es como una noche sin estrellas.\"",
        "\"La magia del cine comienza con una historia.\""
    )
    val fraseAleatoria by remember { mutableStateOf(frases.random()) }
    var showProfileOptions by remember { mutableStateOf(false) }

    val estrenos = listOf(
        CarouselItem("Avatar: Fire and Ash", "https://upload.wikimedia.org/wikipedia/en/a/a4/Avatar_Fire_and_Ash_logo.jpg"),
        CarouselItem("Mission: Impossible – The Final Reckoning", "https://www.joblo.com/wp-content/uploads/2025/02/tom-cruise-last-mission-impossible-movie.jpg"),
        CarouselItem("Jurassic World: Rebirth", "https://upload.wikimedia.org/wikipedia/en/a/a5/Jurassic_World_Rebirth_poster.jpg"),
        CarouselItem("Zootopia 2", "https://i.blogs.es/15f73b/zootopia/1024_682.jpeg"),
    )

    val infiniteTransition = rememberInfiniteTransition()
    val profileScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(1000)) + slideInVertically(tween(600), initialOffsetY = { -80 })
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_cineverse),
                contentDescription = "Logo CineVerse",
                modifier = Modifier
                    .size(140.dp)
                    .alpha(0.95f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "\uD83C\uDF10 Bienvenido a CineVerse",
            style = TextStyle(
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 2f),
                    blurRadius = 6f
                )
            )
        )

        Text(
            text = fraseAleatoria,
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        AnimatedVisibility(
            visible = showProfileOptions,
            enter = fadeIn(tween(300)) + slideInVertically(tween(300))
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(email, color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = onOpenFavorites,
                        colors = ButtonDefaults.buttonColors(Color(0xFFFFC107)),
                        modifier = Modifier.shadow(4.dp, RoundedCornerShape(50))
                    ) {
                        Text("Favoritos", color = Color.Black)
                    }
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(Color.Red.copy(alpha = 0.2f)),
                        modifier = Modifier.shadow(2.dp, RoundedCornerShape(50))
                    ) {
                        Text("Cerrar sesión", color = Color.Red)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
                .clickable { showProfileOptions = !showProfileOptions }
                .shadow(6.dp, CircleShape)
                .graphicsLayer {
                    scaleX = profileScale
                    scaleY = profileScale
                },
            contentAlignment = Alignment.Center
        ) {
            Text("\uD83D\uDC64", fontSize = 30.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "\uD83C\uDF9E Últimos estrenos",
            color = Color(0xFFFFC107),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .shadow(6.dp)
        )

        Carousel(items = estrenos)

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("🎞 Acción", "😂 Comedia", "😱 Terror").forEach {
                Text(
                    text = it,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun Carousel(items: List<CarouselItem>) {
    var currentIndex by remember { mutableStateOf(0) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    currentIndex = (currentIndex - dragAmount.sign.toInt()).coerceIn(0, items.lastIndex)
                }
            }
    ) {
        itemsIndexed(items) { index, item ->
            val scale = if (index == currentIndex) 1f else 0.9f
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

data class CarouselItem(val title: String, val imageUrl: String)

data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onItemSelected: (String) -> Unit
) {
    val navItems = listOf(
        BottomNavItem("home", Icons.Default.Home, "Inicio"),
        BottomNavItem("explorer", Icons.Default.Search, "Explorar"),
        BottomNavItem("mapa", Icons.Default.Place, "Mapa"),
        BottomNavItem("trivial", Icons.Default.SportsEsports, "Trivial")
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .shadow(12.dp, RoundedCornerShape(30.dp)),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x66FFFFFF),
                                Color(0x22FFFFFF)
                            )
                        )
                    )
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val selected = currentRoute == item.route
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onItemSelected(item.route) }
                            .padding(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) Color(0xFFFFC107) else Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(26.dp)
                        )
                        if (selected) {
                            Text(
                                text = item.label,
                                color = Color(0xFFFFC107),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
