package com.example.mibolsillo.ui.metas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mibolsillo.ui.AppViewModelProvider
import com.example.mibolsillo.ui.components.BotonPrincipal
import com.example.mibolsillo.ui.components.CampoTexto
import com.example.mibolsillo.ui.components.DialogoFecha
import com.example.mibolsillo.ui.components.IconosApp
import com.example.mibolsillo.ui.components.LocalMoneda
import com.example.mibolsillo.ui.components.PantallaConBarra
import com.example.mibolsillo.util.Fechas

@Composable
fun MetaFormScreen(
    onTerminado: () -> Unit,
    viewModel: MetaFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado = viewModel.uiState
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(estado.terminado) {
        if (estado.terminado) onTerminado()
    }

    PantallaConBarra(
        titulo = if (estado.esEdicion) "Editar meta" else "Nueva meta",
        onAtras = onTerminado
    ) { modifier ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            CampoTexto(
                valor = estado.nombre,
                onValorChange = viewModel::onNombre,
                etiqueta = "Nombre (ej: Laptop nueva)",
                icono = Icons.Filled.Flag,
                error = estado.errorNombre
            )

            OutlinedTextField(
                value = estado.montoTexto,
                onValueChange = viewModel::onMonto,
                label = { Text("¿Cuánto quieres ahorrar?") },
                prefix = { Text(LocalMoneda.current + " ") },
                isError = estado.errorMonto != null,
                supportingText = estado.errorMonto?.let { { Text(it) } },
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedCard(onClick = { mostrarCalendario = true }, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text("Fecha límite", style = MaterialTheme.typography.labelMedium)
                        Text(Fechas.formatoCorto(estado.fechaLimite), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            estado.errorFecha?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(4.dp))
            Text("Icono", style = MaterialTheme.typography.titleSmall)
            SelectorIcono(seleccionado = estado.icono, onSeleccion = viewModel::onIcono)

            Spacer(Modifier.height(8.dp))
            BotonPrincipal(
                texto = if (estado.esEdicion) "Guardar cambios" else "Crear meta",
                onClick = viewModel::guardar,
                cargando = estado.guardando
            )
        }
    }

    if (mostrarCalendario) {
        DialogoFecha(
            fechaActual = estado.fechaLimite,
            permitirFuturo = true,
            onConfirmar = {
                viewModel.onFecha(it)
                mostrarCalendario = false
            },
            onCancelar = { mostrarCalendario = false }
        )
    }
}

/** Cuadrícula de iconos para elegir; el elegido se marca con un borde. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectorIcono(seleccionado: String, onSeleccion: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconosApp.metas.forEach { (clave, icono) ->
            val elegido = clave == seleccionado
            Surface(
                onClick = { onSeleccion(clave) },
                shape = CircleShape,
                color = if (elegido) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(
                    if (elegido) 2.dp else 1.dp,
                    if (elegido) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icono,
                        contentDescription = clave,
                        tint = if (elegido) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
