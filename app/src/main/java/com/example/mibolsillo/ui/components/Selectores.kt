package com.example.mibolsillo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.mibolsillo.data.local.entity.Categoria
import com.example.mibolsillo.util.Fechas

/**
 * Categorías como "fichas" con icono y nombre; la elegida se resalta con su color.
 * Se usa en el formulario de movimientos y en el de presupuestos.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectorCategoria(
    categorias: List<Categoria>,
    seleccionada: Long?,
    onSeleccion: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        categorias.forEach { c ->
            val elegida = c.id == seleccionada
            val color = Color(c.color)
            Surface(
                onClick = { onSeleccion(c.id) },
                shape = MaterialTheme.shapes.large,
                color = if (elegida) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(
                    if (elegida) 2.dp else 1.dp,
                    if (elegida) color else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
                ) {
                    IconoCategoria(c.icono, c.color, tamano = 30.dp)
                    Spacer(Modifier.size(6.dp))
                    Text(c.nombre, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

/**
 * Calendario de Material 3 en un diálogo.
 * @param permitirFuturo true para metas (fecha límite); false para movimientos (no se registran gastos del futuro).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoFecha(
    fechaActual: Long,
    onConfirmar: (Long) -> Unit,
    onCancelar: () -> Unit,
    permitirFuturo: Boolean = false
) {
    val hoy = Fechas.hoyMillisDatePicker()
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = Fechas.aMillisDatePicker(fechaActual),
        selectableDates = object : SelectableDates {
            // Movimientos: solo hasta hoy. Metas: solo desde hoy.
            override fun isSelectableDate(utcTimeMillis: Long) =
                if (permitirFuturo) utcTimeMillis >= hoy else utcTimeMillis <= hoy
        }
    )
    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(onClick = {
                estado.selectedDateMillis?.let { onConfirmar(Fechas.desdeDatePicker(it)) } ?: onCancelar()
            }) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    ) {
        DatePicker(state = estado)
    }
}
