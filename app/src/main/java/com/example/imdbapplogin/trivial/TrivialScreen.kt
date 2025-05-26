package com.example.imdbapplogin.trivial

import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.imdbapplogin.HomeActivity
import com.example.imdbapplogin.R
import com.example.imdbapplogin.utils.cleanHtmlEntities
import com.example.imdbapplogin.utils.shuffledAnswers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrivialScreen(viewModel: TrivialViewModel = viewModel()) {
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.acierto) }

    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val score by viewModel.score.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val gameOver by viewModel.gameOver.collectAsState()
    val waitUntilTomorrow by viewModel.waitUntilTomorrow.collectAsState()

    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var countdownText by remember { mutableStateOf("") }
    LaunchedEffect(waitUntilTomorrow) {
        waitUntilTomorrow?.let { duration ->
            while (true) {
                val now = LocalDateTime.now()
                val targetTime = now.plus(duration)
                val millisLeft = java.time.Duration.between(now, targetTime).toMillis()
                if (millisLeft <= 0) break
                val hours = TimeUnit.MILLISECONDS.toHours(millisLeft)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisLeft) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisLeft) % 60
                countdownText = "⏳ Próxima partida en: %02d:%02d:%02d".format(hours, minutes, seconds)
                delay(1000)
            }
        }
    }

    // Fondo animado degradado
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
        colors = listOf(Color(0xFF0F1B2B), Color(0xFF1C1C2D))
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "🎬 Trivial de Pelis",
                            fontSize = 22.sp,
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
                        containerColor = Color(0xFF0F1B2B), // Color de fondo igual que el fondo de pantalla
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(brush = backgroundBrush)
        ) {
            if (showConfetti) {
                KonfettiView(
                    modifier = Modifier.fillMaxSize(),
                    parties = listOf(
                        Party(
                            speed = 10f,
                            maxSpeed = 50f,
                            damping = 0.9f,
                            spread = 360,
                            colors = listOf(
                                Color.Yellow.toArgb(),
                                Color.Green.toArgb(),
                                Color.Magenta.toArgb(),
                                Color.Cyan.toArgb()
                            ),
                            emitter = Emitter(duration = 1, TimeUnit.SECONDS).perSecond(100),
                            position = Position.Relative(0.5, 0.3)
                        )
                    )
                )
            }

            when {
                waitUntilTomorrow != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🚫 Ya has jugado hoy", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Vuelve mañana para más preguntas", fontSize = 16.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(countdownText, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = {
                            context.startActivity(Intent(context, HomeActivity::class.java))
                        }) {
                            Text("Volver al inicio 🏠")
                        }
                    }
                }

                gameOver -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉 ¡Has completado las 10 preguntas de hoy!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Puntuación final: $score", fontSize = 18.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, HomeActivity::class.java))
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Volver al inicio 🏠")
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("⭐ Puntos: $score", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            Text("⏳ $timeLeft s", fontWeight = FontWeight.Bold, color = if (timeLeft <= 5) Color.Red else Color.White)
                        }

                        when {
                            isLoading -> CircularProgressIndicator(color = Color.White)
                            error != null -> Text("❌ Error: $error", color = Color.White)
                            currentQuestion != null -> {
                                val question = currentQuestion!!
                                val answers = remember(question) { question.shuffledAnswers().map { it.cleanHtmlEntities() } }
                                val correctAnswer = question.correct_answer.cleanHtmlEntities()

                                Text(
                                    text = question.question.cleanHtmlEntities(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 20.dp).fillMaxWidth(),
                                    color = Color.White
                                )

                                answers.forEach { answer ->
                                    val isCorrect = answer == correctAnswer
                                    val backgroundColor by animateColorAsState(
                                        when {
                                            !showResult -> Color.White
                                            selectedAnswer == answer && isCorrect -> Color(0xFFB9FBC0)
                                            selectedAnswer == answer && !isCorrect -> Color(0xFFFFADAD)
                                            isCorrect -> Color(0xFFCAFFBF)
                                            else -> Color.White
                                        }
                                    )

                                    Card(
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .shadow(4.dp, RoundedCornerShape(20.dp))
                                            .clickable(enabled = !showResult && selectedAnswer == null) {
                                                selectedAnswer = answer
                                                showResult = true
                                                viewModel.stopTimer()

                                                if (answer == correctAnswer) {
                                                    viewModel.increaseScore()
                                                    showConfetti = true
                                                    mediaPlayer.start()

                                                    coroutineScope.launch {
                                                        delay(2000)
                                                        showConfetti = false
                                                    }
                                                }

                                                coroutineScope.launch {
                                                    delay(2000)
                                                    selectedAnswer = null
                                                    showResult = false
                                                    viewModel.loadNewQuestion()
                                                }
                                            },
                                        colors = CardDefaults.cardColors(containerColor = backgroundColor)
                                    ) {
                                        Text(
                                            text = answer,
                                            modifier = Modifier.padding(16.dp),
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
