package com.example.mibolsillo.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mibolsillo.data.consejos.Consejo
import com.example.mibolsillo.data.local.model.EstadoPresupuesto
import com.example.mibolsillo.data.local.model.PresupuestoConGasto
import com.example.mibolsillo.data.local.model.TotalPorCategoria
import com.example.mibolsillo.ui.AppViewModelProvider
import com.example.mibolsillo.ui.components.FilaMovimiento
import com.example.mibolsillo.ui.components.IconoCategoria
import com.example.mibolsillo.ui.presupuestos.colorEstado
import com.example.mibolsillo.ui.presupuestos.mensajeEstado
import com.example.mibolsillo.ui.components.GraficoDona
import com.example.mibolsillo.ui.components.PorcionGrafico
import com.example.mibolsillo.ui.components.dinero
import com.example.mibolsillo.util.Fechas
import java.time.LocalTime

@Composable
fun InicioScreen(
    onAgregarMovimiento: () -> Unit,
    onEditarMovimiento: (Long) -> Unit,
    onVerMovimientos: () -> Unit,
    onPresupuestos: () -> Unit,
    onConsejos: () -> Unit,
    viewModel: InicioViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAgregarMovimiento,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Agregar") }
            )
        }
    ) { padding ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { TarjetaSaldo(estado) }

            // Alertas de presupuesto (80% y 100%): solo aparecen si hay alguna
            if (estado.alertas.isNotEmpty()) {
                item { AlertasPresupuesto(estado.alertas, onPresupuestos) }
            }

            item { AccesosRapidos(onPresupuestos = onPresupuestos, onConsejos = onConsejos) }

            item { TarjetaConsejo(viewModel.consejoDelDia, onVerMas = onConsejos) }

            item { TarjetaGastosMes(estado) }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Últimos movimientos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    TextButton(onClick = onVerMovimientos) { Text("Ver todos") }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    if (estado.ultimos.isEmpty() && !estado.cargando) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Aún no tienes movimientos", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Registra tu primer ingreso o gasto para ver tu resumen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onAgregarMovimiento) { Text("Agregar movimiento") }
                        }
                    } else {
                        Column(Modifier.padding(vertical = 4.dp)) {
                            estado.ultimos.forEach { item ->
                                FilaMovimiento(item = item, onClick = { onEditarMovimiento(item.movimiento.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Saludo según la hora del día. */
private fun saludo(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Buenos días"
    in 12..18 -> "Buenas tardes"
    else -> "Buenas noches"
}

/** Tarjeta principal con degradado: saludo, saldo y resumen del mes. */
@Composable
private fun TarjetaSaldo(estado: InicioUiState) {
    val primerNombre = estado.nombre.substringBefore(" ")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF00897B), Color(0xFF004D40))))
            .padding(20.dp)
    ) {
        Column {
            Text(
                if (primerNombre.isBlank()) saludo() else "${saludo()}, $primerNombre",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(12.dp))
            Text("Saldo actual", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.75f))
            Text(
                dinero(estado.saldo),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                maxLines = 1
            )
            Spacer(Modifier.height(16.dp))
            Text(
                Fechas.nombreMes(estado.mes),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ResumenMes("Ingresos", estado.ingresosMes, Icons.Filled.ArrowUpward, Color(0xFFB9F6CA), Modifier.weight(1f))
                ResumenMes("Gastos", estado.gastosMes, Icons.Filled.ArrowDownward, Color(0xFFFFCDD2), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ResumenMes(titulo: String, monto: Double, icono: ImageVector, colorIcono: Color, modifier: Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(titulo, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            Text(
                dinero(monto),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AccesosRapidos(onPresupuestos: () -> Unit, onConsejos: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AccesoRapido("Presupuestos", "Controla tus límites", Icons.Filled.PieChart, onPresupuestos, Modifier.weight(1f))
        AccesoRapido("Consejos", "Aprende a ahorrar", Icons.Filled.School, onConsejos, Modifier.weight(1f))
    }
}

@Composable
private fun AccesoRapido(titulo: String, subtitulo: String, icono: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.height(8.dp))
            Text(titulo, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(
                subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun TarjetaConsejo(consejo: Consejo, onVerMas: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Consejo del día",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(consejo.titulo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(Modifier.height(4.dp))
            Text(
                consejo.texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(onClick = onVerMas, modifier = Modifier.align(Alignment.End)) {
                Text("Más consejos")
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/** Gráfica resumen: dona con los gastos del mes por categoría + leyenda. */
@Composable
private fun TarjetaGastosMes(estado: InicioUiState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            Text("Gastos de ${Fechas.nombreMes(estado.mes)}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            if (estado.gastosPorCategoria.isEmpty()) {
                Text(
                    "Todavía no registras gastos este mes. Cuando lo hagas, aquí verás en qué se va tu dinero.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val leyenda = agruparParaLeyenda(estado.gastosPorCategoria)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GraficoDona(
                        porciones = leyenda.map { PorcionGrafico(it.total.toFloat(), Color(it.color)) },
                        grosor = 18.dp,
                        colorVacio = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(130.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total", style = MaterialTheme.typography.labelSmall)
                            Text(
                                dinero(estado.gastosMes),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                        leyenda.forEach { item ->
                            val porcentaje = if (estado.gastosMes > 0) (item.total / estado.gastosMes * 100).toInt() else 0
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(item.color))
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    item.nombre,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text("$porcentaje%", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Muestra las 4 categorías con más gasto y suma el resto en "Otras" para que la leyenda no sea enorme. */
private fun agruparParaLeyenda(lista: List<TotalPorCategoria>): List<TotalPorCategoria> {
    if (lista.size <= 5) return lista
    val principales = lista.take(4)
    val resto = lista.drop(4).sumOf { it.total }
    return principales + TotalPorCategoria(-1, "Otras", "otros", 0xFF90A4AE, resto)
}

/** Tarjeta de avisos cuando algún presupuesto llegó al 80% o al 100%. */
@Composable
private fun AlertasPresupuesto(alertas: List<PresupuestoConGasto>, onVer: () -> Unit) {
    val hayExcedidos = alertas.any { it.estado == EstadoPresupuesto.EXCEDIDO }
    val colorPrincipal = colorEstado(if (hayExcedidos) EstadoPresupuesto.EXCEDIDO else EstadoPresupuesto.ALERTA)
    Card(
        onClick = onVer,
        colors = CardDefaults.cardColors(containerColor = colorPrincipal.copy(alpha = 0.12f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = colorPrincipal)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (alertas.size == 1) "Alerta de presupuesto" else "${alertas.size} alertas de presupuesto",
                    style = MaterialTheme.typography.titleSmall,
                    color = colorPrincipal
                )
            }
            Spacer(Modifier.height(8.dp))
            alertas.take(3).forEach { p ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    IconoCategoria(p.icono, p.color, tamano = 32.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.nombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(mensajeEstado(p), style = MaterialTheme.typography.bodySmall, color = colorEstado(p.estado))
                    }
                    Text(
                        "${(p.porcentaje * 100).toInt()}%",
                        style = MaterialTheme.typography.titleSmall,
                        color = colorEstado(p.estado)
                    )
                }
            }
        }
    }
}
