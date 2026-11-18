package com.example.mibolsillo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.example.mibolsillo.util.FormatoMoneda
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow

/** Un grupo de la gráfica (un mes) con dos barras: A (ingresos) y B (gastos). */
data class GrupoBarras(val etiqueta: String, val valorA: Float, val valorB: Float)

/**
 * Gráfica de barras agrupadas dibujada con Canvas.
 *
 * Partes del dibujo:
 *  1. Eje Y: 5 líneas guía (0%, 25%, 50%, 75%, 100% del máximo) con su valor.
 *  2. Por cada mes, dos barras una al lado de la otra; su alto es proporcional al valor.
 *  3. Debajo, el nombre del mes.
 * Al tocar un mes se llama a [onSeleccion] con su índice (para mostrar el detalle).
 */
@Composable
fun GraficoBarras(
    grupos: List<GrupoBarras>,
    colorA: Color,
    colorB: Color,
    seleccionado: Int?,
    onSeleccion: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(grupos) {
        progreso.snapTo(0f)
        progreso.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    val medidorTexto = rememberTextMeasurer()
    val estiloEtiqueta = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val estiloSeleccion = estiloEtiqueta.copy(color = MaterialTheme.colorScheme.primary)
    val colorRejilla = MaterialTheme.colorScheme.outlineVariant
    val colorFondoSeleccion = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    val maximo = escalaRedonda(grupos.maxOfOrNull { maxOf(it.valorA, it.valorB) } ?: 0f)

    Canvas(
        modifier = modifier.pointerInput(grupos) {
            // Detecta en qué columna (mes) se tocó
            detectTapGestures { toque ->
                val ejeIzquierdo = 44.dp.toPx()
                val anchoGrupo = (size.width - ejeIzquierdo) / grupos.size
                val indice = ((toque.x - ejeIzquierdo) / anchoGrupo).toInt()
                if (indice in grupos.indices) onSeleccion(indice)
            }
        }
    ) {
        if (grupos.isEmpty()) return@Canvas
        val ejeIzquierdo = 44.dp.toPx()
        val ejeInferior = 24.dp.toPx()
        val margenSuperior = 8.dp.toPx()
        val altoUtil = size.height - ejeInferior - margenSuperior
        val anchoGrupo = (size.width - ejeIzquierdo) / grupos.size

        // 1) Líneas guía y valores del eje Y
        for (i in 0..4) {
            val y = margenSuperior + altoUtil * (1 - i / 4f)
            drawLine(colorRejilla, Offset(ejeIzquierdo, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            val texto = medidorTexto.measure(FormatoMoneda.compacto((maximo * i / 4f).toDouble()), estiloEtiqueta)
            drawText(texto, topLeft = Offset(0f, y - texto.size.height / 2f))
        }

        // 2) Barras de cada mes
        val anchoBarra = min(anchoGrupo * 0.28f, 18.dp.toPx())
        val espacio = 3.dp.toPx()
        val radio = CornerRadius(4.dp.toPx())
        grupos.forEachIndexed { i, g ->
            val inicioGrupo = ejeIzquierdo + i * anchoGrupo
            val centro = inicioGrupo + anchoGrupo / 2

            if (i == seleccionado) {
                drawRoundRect(
                    colorFondoSeleccion,
                    topLeft = Offset(inicioGrupo + 2.dp.toPx(), margenSuperior),
                    size = Size(anchoGrupo - 4.dp.toPx(), altoUtil + ejeInferior),
                    cornerRadius = CornerRadius(8.dp.toPx())
                )
            }

            fun barra(valor: Float, x: Float, color: Color) {
                val alto = (valor / maximo) * altoUtil * progreso.value
                if (alto <= 0f) return
                drawRoundRect(
                    color,
                    topLeft = Offset(x, margenSuperior + altoUtil - alto),
                    size = Size(anchoBarra, alto),
                    cornerRadius = radio
                )
            }
            barra(g.valorA, centro - anchoBarra - espacio / 2, colorA)
            barra(g.valorB, centro + espacio / 2, colorB)

            // 3) Etiqueta del mes
            val texto = medidorTexto.measure(g.etiqueta, if (i == seleccionado) estiloSeleccion else estiloEtiqueta)
            drawText(
                texto,
                topLeft = Offset(centro - texto.size.width / 2f, size.height - ejeInferior + 4.dp.toPx())
            )
        }
    }
}

/**
 * Redondea el máximo hacia arriba a un número "bonito" (1, 2, 2.5, 5 o 10 × potencia de 10)
 * para que el eje Y muestre valores fáciles de leer: 3,700 -> 5,000.
 */
fun escalaRedonda(maximo: Float): Float {
    if (maximo <= 0f) return 1000f
    val potencia = 10.0.pow(floor(log10(maximo.toDouble()))).toFloat()
    val normalizado = maximo / potencia
    val bonito = when {
        normalizado <= 1f -> 1f
        normalizado <= 2f -> 2f
        normalizado <= 2.5f -> 2.5f
        normalizado <= 5f -> 5f
        else -> 10f
    }
    return bonito * potencia
}
