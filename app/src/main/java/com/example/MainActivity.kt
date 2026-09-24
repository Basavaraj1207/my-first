package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.PhishGuardApp
import com.example.ui.theme.PhishGuardTheme
import com.example.ui.viewmodel.PhishGuardViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PhishGuardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhishGuardTheme {
                PhishGuardApp(viewModel = viewModel)
            }
        }
    }
}
