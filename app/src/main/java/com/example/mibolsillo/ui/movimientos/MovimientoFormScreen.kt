package com.example.mibolsillo.ui.movimientos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mibolsillo.data.local.entity.TipoMovimiento
import com.example.mibolsillo.ui.AppViewModelProvider
import com.example.mibolsillo.ui.components.BotonPrincipal
import com.example.mibolsillo.ui.components.DialogoFecha
import com.example.mibolsillo.ui.components.SelectorCategoria
import com.example.mibolsillo.ui.components.LocalMoneda
import com.example.mibolsillo.ui.components.PantallaConBarra
import com.example.mibolsillo.ui.theme.finanzas
import com.example.mibolsillo.util.Fechas

@Composable
fun MovimientoFormScreen(
    onTerminado: () -> Unit,
    viewModel: MovimientoFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado = viewModel.uiState
    val categorias by viewModel.categorias.collectAsStateWithLifecycle()
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }
    var confirmarEliminar by rememberSaveable { mutableStateOf(false) }

    // Al guardar o eliminar, volvemos a la pantalla anterior
    LaunchedEffect(estado.terminado) {
        if (estado.terminado) onTerminado()
    }

    PantallaConBarra(
        titulo = if (estado.esEdicion) "Editar movimiento" else "Nuevo movimiento",
        onAtras = onTerminado,
        acciones = {
            if (estado.esEdicion) {
                IconButton(onClick = { confirmarEliminar = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar movimiento")
                }
            }
        }
    ) { modifier ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ---------- Tipo ----------
            val tipos = listOf(TipoMovimiento.GASTO to "Gasto", TipoMovimiento.INGRESO to "Ingreso")
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                tipos.forEachIndexed { i, (tipo, texto) ->
                    SegmentedButton(
                        selected = estado.tipo == tipo,
                        onClick = { viewModel.onTipoChange(tipo) },
                        shape = SegmentedButtonDefaults.itemShape(i, tipos.size)
                    ) { Text(texto) }
                }
            }

            // ---------- Monto ----------
            val colorMonto = if (estado.tipo == TipoMovimiento.INGRESO) MaterialTheme.finanzas.ingreso else MaterialTheme.finanzas.gasto
            OutlinedTextField(
                value = estado.montoTexto,
                onValueChange = viewModel::onMontoChange,
                label = { Text("Monto") },
                prefix = { Text(LocalMoneda.current + " ") },
                placeholder = { Text("0.00") },
                isError = estado.errorMonto != null,
                supportingText = estado.errorMonto?.let { { Text(it) } },
                textStyle = MaterialTheme.typography.headlineSmall.copy(color = colorMonto, fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )

            // ---------- Categoría ----------
            Text("Categoría", style = MaterialTheme.typography.titleSmall)
            SelectorCategoria(
                categorias = categorias.filter { it.tipo == estado.tipo },
                seleccionada = estado.categoriaId,
                onSeleccion = viewModel::onCategoriaChange
            )
            estado.errorCategoria?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // ---------- Fecha ----------
            OutlinedCard(onClick = { mostrarCalendario = true }, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text("Fecha", style = MaterialTheme.typography.labelMedium)
                        Text(Fechas.formatoCorto(estado.fecha), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            // ---------- Nota ----------
            OutlinedTextField(
                value = estado.nota,
                onValueChange = viewModel::onNotaChange,
                label = { Text("Nota (opcional)") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                supportingText = { Text("${estado.nota.length}/${MovimientoFormViewModel.MAX_NOTA}") },
                maxLines = 3,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))
            BotonPrincipal(
                texto = if (estado.esEdicion) "Guardar cambios" else "Guardar movimiento",
                onClick = viewModel::guardar,
                cargando = estado.guardando
            )
        }
    }

    if (mostrarCalendario) {
        DialogoFecha(
            fechaActual = estado.fecha,
            onConfirmar = {
                viewModel.onFechaChange(it)
                mostrarCalendario = false
            },
            onCancelar = { mostrarCalendario = false }
        )
    }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("¿Eliminar movimiento?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEliminar = false
                    viewModel.eliminar()
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") } }
        )
    }
}

