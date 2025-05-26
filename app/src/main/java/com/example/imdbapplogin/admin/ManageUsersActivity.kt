package com.example.imdbapplogin.admin

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.imdbapplogin.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore

data class User(
    val uid: String = "",
    val email: String = "",
    val nombre: String = "",
    val role: String = ""
)

class ManageUsersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            var users by remember { mutableStateOf(emptyList<UserModel>()) }

            fun loadUsers() {
                FirebaseFirestore.getInstance().collection("users")
                    .get()
                    .addOnSuccessListener { result ->
                        val userList = result.mapNotNull { doc ->
                            val email = doc.getString("email")
                            val role = doc.getString("role")
                            val id = doc.id
                            if (email != null && role != null) {
                                UserModel(id, email, role)
                            } else null
                        }
                        users = userList
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
                        Log.e("ManageUsers", "Firestore error", it)
                    }
            }

            LaunchedEffect(Unit) { loadUsers() }

            ManageUsersScreen(users) { userToDelete ->
                FirebaseFirestore.getInstance().collection("users")
                    .document(userToDelete.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                        loadUsers()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al eliminar usuario", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
@Composable
fun ManageUsersScreen(users: List<UserModel>, onDeleteUser: (UserModel) -> Unit) {
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F1B2B), Color(0xFF1C1C2D), Color(0xFF0F1B2B))
    )

    var userToConfirmDelete by remember { mutableStateOf<UserModel?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 16.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header elegante y centrado
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1F2A3B),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 20.dp, horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFFFFC107), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Usuarios",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Usuarios Registrados",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            // Lista de usuarios
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(users) { user ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { 40 }),
                        exit = fadeOut()
                    ) {
                        UserCard(user = user, onDelete = {
                            userToConfirmDelete = user
                        })
                    }
                }
            }
        }

        // Dialogo de confirmación elegante
        userToConfirmDelete?.let { user ->
            AlertDialog(
                onDismissRequest = { userToConfirmDelete = null },
                title = {
                    Text(
                        text = "¿Eliminar usuario?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Text("¿Estás seguro de eliminar a:\n${user.email}?",
                        fontSize = 16.sp)
                },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteUser(user)
                        userToConfirmDelete = null
                    }) {
                        Text("Eliminar", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToConfirmDelete = null }) {
                        Text("Cancelar")
                    }
                },
                containerColor = Color(0xFF2A2F3D),
                titleContentColor = Color.White,
                textContentColor = Color.LightGray
            )
        }
    }
}
@Composable
fun UserCard(user: UserModel, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C3E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = user.email,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = user.role.uppercase(),
                    color = Color(0xFFFFC107),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar usuario",
                    tint = Color.Red
                )
            }
        }
    }
}



