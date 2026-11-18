package com.example.mibolsillo.ui.estadisticas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mibolsillo.data.local.entity.TipoMovimiento
import com.example.mibolsillo.ui.AppViewModelProvider
import com.example.mibolsillo.ui.components.EstadoVacio
import com.example.mibolsillo.ui.components.GraficoBarras
import com.example.mibolsillo.ui.components.GraficoDona
import com.example.mibolsillo.ui.components.GrupoBarras
import com.example.mibolsillo.ui.components.IconoCategoria
import com.example.mibolsillo.ui.components.PantallaConBarra
import com.example.mibolsillo.ui.components.PorcionGrafico
import com.example.mibolsillo.ui.components.SelectorMes
import com.example.mibolsillo.ui.components.dinero
import com.example.mibolsillo.ui.theme.finanzas
import com.example.mibolsillo.util.Fechas
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun EstadisticasScreen(
    viewModel: EstadisticasViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    PantallaConBarra(titulo = "Estadísticas") { modifier ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            modifier = modifier.fillMaxSize()
        ) {
            item { SelectorMes(estado.mes, viewModel::mesAnterior, viewModel::mesSiguiente) }

            when {
                estado.cargando -> item {
                    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                estado.sinDatos -> item {
                    EstadoVacio(
                        icono = Icons.Filled.BarChart,
                        titulo = "Aún no hay datos para graficar",
                        mensaje = "Registra ingresos y gastos para ver tus estadísticas de ${Fechas.nombreMes(estado.mes)} y los 5 meses anteriores.",
                        modifier = Modifier.height(400.dp)
                    )
                }

                else -> {
                    item { Indicadores(estado) }
                    item { TarjetaDistribucion(estado, viewModel::cambiarTipo) }
                    item { TarjetaBarras(estado, viewModel::seleccionarBarra) }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Indicadores rápidos (cuadrícula 2 x 2)
// ---------------------------------------------------------------------------

@Composable
private fun Indicadores(estado: EstadisticasUiState) {
    val colores = MaterialTheme.finanzas
    val tasa = estado.tasaAhorro
    val variacion = estado.variacionGastos
    val mayorGasto = if (estado.tipo == TipoMovimiento.GASTO) estado.porCategoria.firstOrNull() else null

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Indicador(
                titulo = "Tasa de ahorro",
                valor = tasa?.let { "${(it * 100).roundToInt()}%" } ?: "—",
                detalle = when {
                    tasa == null -> "Sin ingresos este mes"
                    tasa >= 0.2 -> "¡Excelente! Meta: 20%"
                    tasa >= 0 -> "Intenta llegar al 20%"
                    else -> "Gastaste más de lo que ganaste"
                },
                colorValor = when {
                    tasa == null -> MaterialTheme.colorScheme.onSurface
                    tasa >= 0.2 -> colores.ingreso
                    tasa >= 0 -> colores.advertencia
                    else -> colores.gasto
                },
                modifier = Modifier.weight(1f)
            )
            Indicador(
                titulo = "Vs. mes anterior",
                valor = variacion?.let { (if (it > 0) "+" else "") + "${(it * 100).roundToInt()}%" } ?: "—",
                detalle = when {
                    variacion == null -> "Sin datos del mes anterior"
                    variacion > 0 -> "Gastaste más"
                    variacion < 0 -> "Gastaste menos"
                    else -> "Igual que el mes pasado"
                },
                // Gastar más es "malo" (rojo), gastar menos es "bueno" (verde)
                colorValor = when {
                    variacion == null || abs(variacion) < 0.005 -> MaterialTheme.colorScheme.onSurface
                    variacion > 0 -> colores.gasto
                    else -> colores.ingreso
                },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Indicador(
                titulo = "Gasto promedio",
                valor = dinero(estado.promedioGastos),
                detalle = "Por mes (últimos 6)",
                modifier = Modifier.weight(1f)
            )
            Indicador(
                titulo = "Mayor gasto",
                valor = mayorGasto?.nombre ?: "—",
                detalle = mayorGasto?.let { dinero(it.total) } ?: "Sin gastos este mes",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Indicador(
    titulo: String,
    valor: String,
    detalle: String,
    modifier: Modifier = Modifier,
    colorValor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(
                valor,
                style = MaterialTheme.typography.titleLarge,
                color = colorValor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Gráfica de pastel (dona) por categoría
// ---------------------------------------------------------------------------

@Composable
private fun TarjetaDistribucion(estado: EstadisticasUiState, onTipo: (TipoMovimiento) -> Unit) {
    val esGasto = estado.tipo == TipoMovimiento.GASTO
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            Text("Distribución por categoría", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            val opciones = listOf(TipoMovimiento.GASTO to "Gastos", TipoMovimiento.INGRESO to "Ingresos")
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                opciones.forEachIndexed { i, (tipo, texto) ->
                    SegmentedButton(
                        selected = estado.tipo == tipo,
                        onClick = { onTipo(tipo) },
                        shape = SegmentedButtonDefaults.itemShape(i, opciones.size)
                    ) { Text(texto) }
                }
            }
            Spacer(Modifier.height(16.dp))

            if (estado.porCategoria.isEmpty()) {
                Text(
                    "No registraste ${if (esGasto) "gastos" else "ingresos"} en ${Fechas.nombreMes(estado.mes)}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
                return@Column
            }

            GraficoDona(
                porciones = estado.porCategoria.map { PorcionGrafico(it.total.toFloat(), Color(it.color)) },
                grosor = 30.dp,
                colorVacio = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (esGasto) "Gastos" else "Ingresos",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(dinero(estado.totalPastel), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))

            // Leyenda con monto, porcentaje y barra de cada categoría
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                estado.porCategoria.forEach { c ->
                    val fraccion = if (estado.totalPastel > 0) (c.total / estado.totalPastel).toFloat() else 0f
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconoCategoria(c.icono, c.color, tamano = 36.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row {
                                Text(c.nombre, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text(dinero(c.total), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { fraccion },
                                    color = Color(c.color),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    drawStopIndicator = {},
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(CircleShape)
                                )
                                Text(
                                    "${(fraccion * 100).roundToInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier
                                        .width(40.dp)
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Gráfica de barras: ingresos vs gastos de 6 meses
// ---------------------------------------------------------------------------

@Composable
private fun TarjetaBarras(estado: EstadisticasUiState, onSeleccion: (Int) -> Unit) {
    val colorIngreso = MaterialTheme.finanzas.ingreso
    val colorGasto = MaterialTheme.finanzas.gasto
    val seleccion = estado.barras.getOrNull(estado.barraSeleccionada)

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            Text("Ingresos vs gastos", style = MaterialTheme.typography.titleMedium)
            Text(
                "Últimos 6 meses · toca un mes para ver el detalle",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Leyenda("Ingresos", colorIngreso)
                Leyenda("Gastos", colorGasto)
            }
            Spacer(Modifier.height(12.dp))

            GraficoBarras(
                grupos = estado.barras.map {
                    GrupoBarras(Fechas.mesCorto(it.mes), it.ingresos.toFloat(), it.gastos.toFloat())
                },
                colorA = colorIngreso,
                colorB = colorGasto,
                seleccionado = estado.barraSeleccionada,
                onSeleccion = onSeleccion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            // Detalle del mes tocado
            if (seleccion != null) {
                val balance = seleccion.ingresos - seleccion.gastos
                Spacer(Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Text(Fechas.nombreMes(seleccion.mes), style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        FilaDetalle("Ingresos", dinero(seleccion.ingresos), colorIngreso)
                        FilaDetalle("Gastos", dinero(seleccion.gastos), colorGasto)
                        FilaDetalle(
                            "Balance",
                            (if (balance >= 0) "+" else "") + dinero(balance),
                            if (balance >= 0) colorIngreso else colorGasto
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Leyenda(texto: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun FilaDetalle(etiqueta: String, valor: String, color: Color) {
    Row(Modifier.fillMaxWidth()) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(valor, style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.SemiBold)
    }
}
