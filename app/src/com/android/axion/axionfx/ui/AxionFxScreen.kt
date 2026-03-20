@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.android.axion.axionfx.ui

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import com.android.axion.axionfx.ui.screens.ConvolverScreen
import com.android.axion.axionfx.ui.screens.DashboardScreen
import com.android.axion.axionfx.ui.screens.EqualizerScreen
import com.android.axion.axionfx.ui.screens.ExciterScreen
import com.android.axion.axionfx.ui.screens.FirEqScreen
import com.android.axion.axionfx.ui.screens.MultibandScreen
import com.android.axion.axionfx.ui.screens.PresetsScreen

@Composable
fun AxionFxScreen(viewModel: AxionFxViewModel) {
    val activity = LocalContext.current as? Activity
    var currentScreen by rememberSaveable { mutableStateOf<String?>(null) }
    val dashboardScrollState = rememberScrollState()
    val motionScheme = MaterialTheme.motionScheme

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            val enterSpec = motionScheme.defaultSpatialSpec<IntOffset>()
            val exitSpec = motionScheme.fastSpatialSpec<IntOffset>()
            if (targetState != null) {
                (slideInHorizontally(enterSpec) { it / 3 } + fadeIn(motionScheme.defaultEffectsSpec()))
                    .togetherWith(slideOutHorizontally(exitSpec) { -it / 3 } + fadeOut(motionScheme.fastEffectsSpec()))
            } else {
                (slideInHorizontally(enterSpec) { -it / 3 } + fadeIn(motionScheme.defaultEffectsSpec()))
                    .togetherWith(slideOutHorizontally(exitSpec) { it / 3 } + fadeOut(motionScheme.fastEffectsSpec()))
            }.using(SizeTransform(clip = false))
        },
        label = "screen"
    ) { screen ->
        when (screen) {
            "equalizer" -> EqualizerScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = null },
            )
            "fir_eq" -> FirEqScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = null },
            )
            "multiband" -> MultibandScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = null },
            )
            "exciter" -> ExciterScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = null },
            )
            "convolver" -> ConvolverScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = null },
            )
            "presets" -> PresetsScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = null },
            )
            else -> DashboardScreen(
                viewModel = viewModel,
                onNavigate = { currentScreen = it },
                scrollState = dashboardScrollState,
            )
        }
    }
}
