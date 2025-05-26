package com.example.imdbapplogin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.net.URL

data class NearbyCinema(
    val name: String,
    val address: String,
    val location: LatLng,
    val placeId: String,
    var distanceMeters: Float = 0f,
    var website: String? = null
)

class MapaActivity : ComponentActivity() {
    private lateinit var mapView: MapView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView = MapView(this)
        mapView.onCreate(savedInstanceState)
        setContent { MapaPantalla(mapView) }
    }

    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { mapView.onPause(); super.onPause() }
    override fun onDestroy() { mapView.onDestroy(); super.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaPantalla(mapView: MapView) {
    val context = LocalContext.current
    val fusedLocation = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()
    val allCinemas = remember { mutableStateListOf<NearbyCinema>() }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var search by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val showFab = remember { mutableStateOf(true) }
    val apiKey = "AIzaSyBArWm18sOvkNX9FLWY3KX1aTqEkVq-UfM"

    val filteredCinemas = allCinemas.filter { it.name.contains(search, ignoreCase = true) }
    val backgroundBrush = Brush.verticalGradient(colors = listOf(Color(0xFF0F1B2B), Color(0xFF1C1C2D)))

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val location = fusedLocation.lastLocation.await()
            userLocation = location
            val userLatLng = LatLng(location.latitude, location.longitude)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 10f))
            googleMap?.addMarker(
                MarkerOptions()
                    .position(userLatLng)
                    .title("Estás aquí")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )

            val radius = 200000
            var pageToken: String? = null

            do {
                val urlBuilder = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                urlBuilder.append("?location=${location.latitude},${location.longitude}&radius=$radius&keyword=cinema&key=$apiKey")
                if (pageToken != null) {
                    urlBuilder.append("&pagetoken=$pageToken")
                    delay(2000)
                }
                val response = withContext(Dispatchers.IO) { URL(urlBuilder.toString()).readText() }
                val json = JSONObject(response)
                val results = json.getJSONArray("results")
                pageToken = if (json.has("next_page_token")) json.getString("next_page_token") else null

                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)
                    val name = item.getString("name")
                    val address = item.optString("vicinity", "Dirección desconocida")
                    val locObj = item.getJSONObject("geometry").getJSONObject("location")
                    val lat = locObj.getDouble("lat")
                    val lng = locObj.getDouble("lng")
                    val placeId = item.getString("place_id")
                    val placeLoc = Location("cine").apply { latitude = lat; longitude = lng }
                    val distance = userLocation?.distanceTo(placeLoc) ?: 0f
                    val cine = NearbyCinema(name, address, LatLng(lat, lng), placeId, distance)
                    if (allCinemas.none { it.name == cine.name && it.address == cine.address }) {
                        allCinemas.add(cine)
                        googleMap?.addMarker(
                            MarkerOptions()
                                .position(cine.location)
                                .title(cine.name)
                                .snippet(cine.address)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                        )
                    }
                }
            } while (pageToken != null)
            allCinemas.sortBy { it.distanceMeters }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cines Cercanos",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 10f))
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val intent = Intent(context, HomeActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F1B2B))
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab.value && filteredCinemas.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        val cineMasCercano = filteredCinemas.first()
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${cineMasCercano.location.latitude},${cineMasCercano.location.longitude}")
                        }
                        context.startActivity(intent)
                    },
                    containerColor = Color(0xFFEF4444),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Ir al más cercano")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundBrush)
                .padding(padding)
                .padding(16.dp)
        ) {
            AndroidView(
                factory = {
                    mapView.apply {
                        getMapAsync { map ->
                            googleMap = map
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                map.isMyLocationEnabled = true
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Buscar cine por nombre", color = Color.White) },
                textStyle = TextStyle(color = Color.White),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Pulsa para ver la Página Web:", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredCinemas) { cine ->
                    val distancia = if (cine.distanceMeters >= 1000) String.format("%.1f km", cine.distanceMeters / 1000f) else "${cine.distanceMeters.toInt()} m"

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                coroutineScope.launch {
                                    val website = getWebsiteFromPlaceId(cine.placeId, apiKey)
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(website ?: "https://www.google.com/search?q=cartelera ${cine.name}")
                                    }
                                    context.startActivity(intent)
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = "Ubicación", tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = cine.name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = cine.address, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Distancia: $distancia", fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

suspend fun getWebsiteFromPlaceId(placeId: String, apiKey: String): String? {
    val url = "https://maps.googleapis.com/maps/api/place/details/json?place_id=$placeId&fields=website&key=$apiKey"
    return withContext(Dispatchers.IO) {
        try {
            val response = URL(url).readText()
            val json = JSONObject(response)
            if (json.getString("status") == "OK") {
                val result = json.getJSONObject("result")
                result.optString("website", null)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
