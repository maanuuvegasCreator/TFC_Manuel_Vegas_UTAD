package com.example.imdbapplogin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.imdbapplogin.ui.MovieExplorerScreen

class MovieExplorerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MovieExplorerScreen()
        }
    }
}
