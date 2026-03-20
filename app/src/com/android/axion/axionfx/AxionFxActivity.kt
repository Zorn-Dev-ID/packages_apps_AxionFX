package com.android.axion.axionfx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.android.axion.axionfx.service.AxionFxService
import com.android.axion.axionfx.ui.AxionFxScreen
import com.android.axion.axionfx.ui.AxionFxViewModel
import com.android.axion.axionfx.ui.theme.AxionFxTheme

class AxionFxActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AxionFxService.start(this)

        val viewModel = AxionFxViewModel(AxionFxService.getPrefs(this))

        enableEdgeToEdge()
        setContent {
            AxionFxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    AxionFxScreen(viewModel = viewModel)
                }
            }
        }
    }
}
