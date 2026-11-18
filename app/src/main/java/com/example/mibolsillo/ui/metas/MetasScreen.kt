package com.example.mibolsillo.ui.metas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mibolsillo.data.local.entity.MetaAhorro
import com.example.mibolsillo.ui.AppViewModelProvider
import com.example.mibolsillo.ui.components.EstadoVacio
import com.example.mibolsillo.ui.components.IconosApp
import com.example.mibolsillo.ui.components.LocalMoneda
import com.example.mibolsillo.ui.components.PantallaConBarra
import com.example.mibolsillo.ui.components.dinero
import com.example.mibolsillo.ui.theme.finanzas
import com.example.mibolsillo.util.Fechas
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun MetasScreen(
    onNuevaMeta: () -> Unit,
    onEditarMeta: (Long) -> Unit,
    viewModel: MetasViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var metaAEliminar by remember { mutableStateOf<MetaAhorro?>(null) }

    PantallaConBarra(
        titulo = "Metas de ahorro",
        botonFlotante = {
            ExtendedFloatingActionButton(
                onClick = onNuevaMeta,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva meta") }
            )
        }
    ) { modifier ->
        when {
            estado.cargando -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            estado.metas.isEmpty() -> EstadoVacio(
                icono = Icons.Filled.Savings,
                titulo = "Aún no tienes metas de ahorro",
                mensaje = "¿Una laptop, un viaje o un fondo de emergencia? Crea una meta y registra tus aportes para ver cómo avanzas.",
                modifier = modifier
            )

            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                modifier = modifier.fillMaxSize()
            ) {
                item { ResumenMetas(estado) }
                items(estado.metas, key = { it.id }) { meta ->
                    TarjetaMeta(
                        meta = meta,
                        onAportar = { viewModel.abrirAporte(meta) },
                        onEditar = { onEditarMeta(meta.id) },
                        onEliminar = { metaAEliminar = meta },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    viewModel.formAporte?.let { form ->
        DialogoAporte(
            form = form,
            onMonto = viewModel::onMontoAporte,
            onConfirmar = viewModel::registrarAporte,
            onCerrar = viewModel::cerrarAporte
        )
    }

    metaAEliminar?.let { meta ->
        AlertDialog(
            onDismissRequest = { metaAEliminar = null },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("¿Eliminar \"${meta.nombre}\"?") },
            text = { Text("Se perderá el registro de lo ahorrado (${dinero(meta.montoActual)}). Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(meta)
                    metaAEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { metaAEliminar = null }) { Text("Cancelar") } }
        )
    }

    viewModel.metaCompletada?.let { nombre ->
        DialogoFelicitacion(nombreMeta = nombre, onCerrar = viewModel::felicitacionVista)
    }
}

@Composable
private fun ResumenMetas(estado: MetasUiState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Total ahorrado", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(dinero(estado.totalAhorrado), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    "de ${dinero(estado.totalObjetivo)} en ${estado.metas.size} meta(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "${estado.completadas} cumplida(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Sugerencia de ahorro: cuánto aportar por semana para llegar a la fecha límite.
 * Divide lo que falta entre las semanas que quedan (redondeando hacia arriba).
 */
@Composable
private fun sugerencia(meta: MetaAhorro): String {
    val dias = ChronoUnit.DAYS.between(LocalDate.now(), Fechas.aLocalDate(meta.fechaLimite))
    val falta = meta.montoObjetivo - meta.montoActual
    return when {
        dias < 0 -> "La fecha límite ya pasó. ¡Aún puedes completarla!"
        dias == 0L -> "¡Hoy es la fecha límite! Te faltan ${dinero(falta)}"
        else -> {
            val semanas = ceil(dias / 7.0).coerceAtLeast(1.0)
            "Ahorra ${dinero(falta / semanas)} por semana · quedan $dias días"
        }
    }
}

@Composable
private fun TarjetaMeta(
    meta: MetaAhorro,
    onAportar: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuAbierto by remember { mutableStateOf(false) }
    val progreso by animateFloatAsState(meta.progreso, tween(800), label = "progreso_meta")
    val colorProgreso = if (meta.completada) MaterialTheme.finanzas.ingreso else MaterialTheme.colorScheme.primary

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            IconosApp.meta(meta.icono),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(meta.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Fecha límite: ${Fechas.formatoCorto(meta.fechaLimite)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opciones de la meta")
                    }
                    DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                            onClick = {
                                menuAbierto = false
                                onEditar()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                            onClick = {
                                menuAbierto = false
                                onEliminar()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(dinero(meta.montoActual), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    " de ${dinero(meta.montoObjetivo)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 1.dp)
                )
                Text(
                    "${(meta.progreso * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    color = colorProgreso
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progreso },
                color = colorProgreso,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                drawStopIndicator = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(12.dp))

            if (meta.completada) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.finanzas.ingreso)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "¡Meta cumplida!",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.finanzas.ingreso
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        sugerencia(meta),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledTonalButton(onClick = onAportar) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Aportar")
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogoAporte(
    form: FormAporte,
    onMonto: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit
) {
    val falta = form.meta.montoObjetivo - form.meta.montoActual
    AlertDialog(
        onDismissRequest = onCerrar,
        icon = { Icon(IconosApp.meta(form.meta.icono), contentDescription = null) },
        title = { Text("Aportar a \"${form.meta.nombre}\"") },
        text = {
            Column {
                Text(
                    "Te faltan ${dinero(falta)} para completar esta meta.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = form.montoTexto,
                    onValueChange = onMonto,
                    label = { Text("Monto del aporte") },
                    prefix = { Text(LocalMoneda.current + " ") },
                    isError = form.error != null,
                    supportingText = form.error?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirmar) { Text("Aportar") } },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}
