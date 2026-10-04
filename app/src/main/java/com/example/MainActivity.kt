package com.example

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.MyApplicationTheme
import java.io.File

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize WebView code cache directories to eliminate Chromium simple_file_enumerator and index reconstruction errors
        try {
            val baseCache = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache")
            File(baseCache, "wasm").mkdirs()
            File(baseCache, "js").mkdirs()
            val httpCache = File(cacheDir, "WebView/Default/HTTP Cache")
            if (!httpCache.exists()) httpCache.mkdirs()

            // Enable software whole document draw to prevent Mesa rendernode missing errors in virtualized emulator environments
            WebView.enableSlowWholeDocumentDraw()
        } catch (_: Exception) {}

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

