package com.example.mibolsillo.ui.presupuestos

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mibolsillo.data.local.model.EstadoPresupuesto
import com.example.mibolsillo.data.local.model.PresupuestoConGasto
import com.example.mibolsillo.ui.AppViewModelProvider
import com.example.mibolsillo.ui.components.EstadoVacio
import com.example.mibolsillo.ui.components.IconoCategoria
import com.example.mibolsillo.ui.components.LocalMoneda
import com.example.mibolsillo.ui.components.PantallaConBarra
import com.example.mibolsillo.ui.components.SelectorCategoria
import com.example.mibolsillo.ui.components.SelectorMes
import com.example.mibolsillo.ui.components.dinero
import com.example.mibolsillo.ui.theme.finanzas
import com.example.mibolsillo.util.Fechas
import java.time.YearMonth
import kotlin.math.roundToInt

@Composable
fun PresupuestosScreen(
    onAtras: () -> Unit,
    viewModel: PresupuestosViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    // Muestra los mensajes del ViewModel en un Snackbar
    LaunchedEffect(viewModel.mensaje) {
        viewModel.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    PantallaConBarra(
        titulo = "Presupuestos",
        onAtras = onAtras,
        snackbarHost = { SnackbarHost(snackbar) },
        botonFlotante = {
            if (estado.categoriasDisponibles.isNotEmpty()) {
                FloatingActionButton(onClick = viewModel::abrirNuevo) {
                    Icon(Icons.Filled.Add, contentDescription = "Nuevo presupuesto")
                }
            }
        }
    ) { modifier ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            modifier = modifier.fillMaxSize()
        ) {
            item {
                SelectorMes(
                    mes = estado.mes,
                    onAnterior = viewModel::mesAnterior,
                    onSiguiente = viewModel::mesSiguiente,
                    mesMaximo = YearMonth.now().plusMonths(1)
                )
            }

            when {
                estado.cargando -> item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                estado.presupuestos.isEmpty() -> item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        EstadoVacio(
                            icono = Icons.Filled.PieChart,
                            titulo = "Sin presupuestos en ${Fechas.nombreMes(estado.mes)}",
                            mensaje = "Define cuánto quieres gastar como máximo en cada categoría. " +
                                "Te avisaremos al llegar al 80% y al 100%.",
                            modifier = Modifier.height(320.dp)
                        )
                        Button(onClick = viewModel::abrirNuevo) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Crear presupuesto")
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = viewModel::copiarMesAnterior) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Copiar del mes anterior")
                        }
                    }
                }

                else -> {
                    item { ResumenPresupuestos(estado.totalGastado, estado.totalLimite) }
                    items(estado.presupuestos, key = { it.id }) { p ->
                        TarjetaPresupuesto(p, onClick = { viewModel.abrirEditar(p) }, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }

    viewModel.form?.let { form ->
        DialogoPresupuesto(
            form = form,
            estado = estado,
            onCategoria = viewModel::onCategoria,
            onMonto = viewModel::onMonto,
            onGuardar = viewModel::guardarForm,
            onEliminar = viewModel::eliminarDesdeForm,
            onCerrar = viewModel::cerrarForm
        )
    }
}

/** Color del semáforo según el estado del presupuesto. */
@Composable
fun colorEstado(estado: EstadoPresupuesto): Color = when (estado) {
    EstadoPresupuesto.NORMAL -> MaterialTheme.finanzas.ingreso
    EstadoPresupuesto.ALERTA -> MaterialTheme.finanzas.advertencia
    EstadoPresupuesto.EXCEDIDO -> MaterialTheme.finanzas.gasto
}

/** Texto que explica cómo va el presupuesto. */
@Composable
fun mensajeEstado(p: PresupuestoConGasto): String = when {
    p.gastado > p.montoLimite -> "Te pasaste por ${dinero(-p.restante)}"
    p.estado == EstadoPresupuesto.EXCEDIDO -> "Llegaste a tu límite"
    p.estado == EstadoPresupuesto.ALERTA -> "¡Cuidado! Ya usaste el ${(p.porcentaje * 100).roundToInt()}%"
    else -> "Te quedan ${dinero(p.restante)}"
}

@Composable
private fun ResumenPresupuestos(gastado: Double, limite: Double) {
    val fraccion = if (limite > 0) (gastado / limite).toFloat() else 0f
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp)) {
            Text("Total del mes", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(dinero(gastado), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    " de ${dinero(limite)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { fraccion.coerceIn(0f, 1f) },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                drawStopIndicator = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )
        }
    }
}

/** Tarjeta de un presupuesto con barra de progreso de color verde / amarillo / rojo. */
@Composable
fun TarjetaPresupuesto(p: PresupuestoConGasto, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = colorEstado(p.estado)
    // La barra se anima cuando cambia el porcentaje
    val progreso by animateFloatAsState(p.porcentaje.toFloat().coerceIn(0f, 1f), tween(700), label = "progreso")

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoCategoria(p.icono, p.color, tamano = 40.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(p.nombre, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${dinero(p.gastado)} de ${dinero(p.montoLimite)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${(p.porcentaje * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progreso },
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                drawStopIndicator = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (p.estado != EstadoPresupuesto.NORMAL) {
                    Icon(
                        if (p.estado == EstadoPresupuesto.EXCEDIDO) Icons.Filled.Error else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    mensajeEstado(p),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (p.estado == EstadoPresupuesto.NORMAL) MaterialTheme.colorScheme.onSurfaceVariant else color
                )
            }
        }
    }
}

@Composable
private fun DialogoPresupuesto(
    form: FormPresupuesto,
    estado: PresupuestosUiState,
    onCategoria: (Long) -> Unit,
    onMonto: (String) -> Unit,
    onGuardar: () -> Unit,
    onEliminar: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (form.esEdicion) "Presupuesto de ${form.nombreCategoria}" else "Nuevo presupuesto") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Límite de gasto para ${Fechas.nombreMes(estado.mes)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = form.montoTexto,
                    onValueChange = onMonto,
                    label = { Text("Monto límite") },
                    prefix = { Text(LocalMoneda.current + " ") },
                    isError = form.errorMonto != null,
                    supportingText = form.errorMonto?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Al editar, la categoría no cambia; al crear se elige entre las que no tienen presupuesto
                if (!form.esEdicion) {
                    Spacer(Modifier.height(8.dp))
                    Text("Categoría", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    SelectorCategoria(
                        categorias = estado.categoriasDisponibles,
                        seleccionada = form.categoriaId,
                        onSeleccion = onCategoria
                    )
                    form.errorCategoria?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onGuardar) { Text("Guardar") } },
        dismissButton = {
            Row {
                if (form.esEdicion) {
                    TextButton(onClick = onEliminar) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onCerrar) { Text("Cancelar") }
            }
        }
    )
}
