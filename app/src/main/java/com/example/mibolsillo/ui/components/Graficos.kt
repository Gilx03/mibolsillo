package com.example.mibolsillo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Una porción de una gráfica: su valor y su color. */
data class PorcionGrafico(val valor: Float, val color: Color)

/**
 * Gráfica de dona (pastel con hueco) dibujada a mano con Canvas de Compose.
 *
 * Cómo funciona:
 *  - Cada porción ocupa un ángulo proporcional a su valor: (valor / total) * 360°.
 *  - drawArc dibuja cada arco empezando donde terminó el anterior.
 *  - "progreso" va de 0 a 1 al aparecer, así la dona se dibuja girando (animación).
 *
 * @param contenidoCentro lo que se muestra en el hueco (p. ej. el total).
 */
@Composable
fun GraficoDona(
    porciones: List<PorcionGrafico>,
    modifier: Modifier = Modifier,
    grosor: Dp = 22.dp,
    colorVacio: Color = Color.LightGray.copy(alpha = 0.3f),
    contenidoCentro: @Composable BoxScope.() -> Unit = {}
) {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(porciones) {
        progreso.snapTo(0f)
        progreso.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val grosorPx = grosor.toPx()
            val diametro = size.minDimension - grosorPx
            val esquina = Offset((size.width - diametro) / 2, (size.height - diametro) / 2)
            val tamanoArco = Size(diametro, diametro)
            val total = porciones.sumOf { it.valor.toDouble() }.toFloat()

            // Anillo de fondo (se ve cuando no hay datos o mientras se anima)
            drawArc(colorVacio, 0f, 360f, false, esquina, tamanoArco, style = Stroke(grosorPx))

            if (total <= 0f) return@Canvas

            val separacion = if (porciones.size > 1) 2f else 0f // pequeño espacio entre porciones
            var anguloInicio = -90f // empezamos arriba (las 12 en punto)
            porciones.forEach { p ->
                val barrido = (p.valor / total) * 360f * progreso.value
                drawArc(
                    color = p.color,
                    startAngle = anguloInicio,
                    sweepAngle = (barrido - separacion).coerceAtLeast(0.5f),
                    useCenter = false,
                    topLeft = esquina,
                    size = tamanoArco,
                    style = Stroke(width = grosorPx, cap = StrokeCap.Butt)
                )
                anguloInicio += barrido
            }
        }
        contenidoCentro()
    }
}
