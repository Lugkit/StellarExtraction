package com.lugkit.stellarextraction

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.lugkit.stellarextraction.ui.GameScreen
import com.lugkit.stellarextraction.ui.StellarExtractionTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StellarExtractionTheme {
                GameScreen(viewModel)
            }
        }
    }
}
