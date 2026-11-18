package com.example.mibolsillo.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mibolsillo.ui.AppViewModelProvider
import com.example.mibolsillo.ui.components.LogoMiBolsillo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de bienvenida animada.
 * 1) El logo "rebota" al aparecer (animación spring) y el texto sube con un fundido.
 * 2) Al terminar, comprueba la sesión: si existe va a Inicio, si no a Login.
 */
@Composable
fun SplashScreen(
    onIrAInicio: () -> Unit,
    onIrALogin: () -> Unit,
    viewModel: SplashViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // Valores animados (empiezan "ocultos")
    val escalaLogo = remember { Animatable(0.3f) }
    val opacidadTexto = remember { Animatable(0f) }
    val desplazamientoTexto = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        // Las tres animaciones corren en paralelo, cada una en su corrutina
        launch {
            escalaLogo.animateTo(
                1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
        }
        launch {
            delay(250)
            opacidadTexto.animateTo(1f, animationSpec = tween(600))
        }
        launch {
            delay(250)
            desplazamientoTexto.animateTo(0f, animationSpec = tween(600))
        }
        delay(1600) // tiempo total visible del splash
        if (viewModel.haySesionActiva()) onIrAInicio() else onIrALogin()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF00897B), Color(0xFF004D40)))
            )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        scaleX = escalaLogo.value
                        scaleY = escalaLogo.value
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    LogoMiBolsillo(tamano = 116.dp)
                }
            }
            Spacer(Modifier.height(24.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = opacidadTexto.value
                    translationY = desplazamientoTexto.value
                }
            ) {
                Text(
                    "MiBolsillo",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Controla tus finanzas",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}
