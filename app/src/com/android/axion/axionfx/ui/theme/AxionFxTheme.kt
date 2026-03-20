package com.android.axion.axionfx.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.android.axion.compose.theme.AxionTheme

@Composable
fun AxionFxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    AxionTheme(darkTheme = darkTheme, content = content)
}
