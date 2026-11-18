package com.example.mibolsillo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mibolsillo.data.local.entity.TipoMovimiento
import com.example.mibolsillo.data.local.model.MovimientoConCategoria
import com.example.mibolsillo.ui.theme.finanzas
import com.example.mibolsillo.util.Fechas

/** Círculo de color suave con el icono de la categoría (p. ej. un tenedor naranja para Comida). */
@Composable
fun IconoCategoria(icono: String, color: Long, tamano: Dp = 44.dp) {
    val c = Color(color)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(tamano)
            .clip(CircleShape)
            .background(c.copy(alpha = 0.18f))
    ) {
        Icon(IconosApp.categoria(icono), contentDescription = null, tint = c, modifier = Modifier.size(tamano * 0.55f))
    }
}

/**
 * Una fila de la lista de movimientos: icono, categoría, nota/fecha y monto.
 * Los ingresos se muestran en verde con "+" y los gastos en rojo con "-".
 */
@Composable
fun FilaMovimiento(
    item: MovimientoConCategoria,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mostrarFecha: Boolean = true
) {
    val m = item.movimiento
    val esIngreso = m.tipo == TipoMovimiento.INGRESO
    val subtitulo = listOfNotNull(
        m.nota.takeIf { it.isNotBlank() },
        Fechas.formatoCorto(m.fecha).takeIf { mostrarFecha }
    ).joinToString(" · ")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        IconoCategoria(item.categoria.icono, item.categoria.color)
        Column(Modifier.weight(1f)) {
            Text(item.categoria.nombre, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            if (subtitulo.isNotEmpty()) {
                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = (if (esIngreso) "+" else "-") + dinero(m.monto),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (esIngreso) MaterialTheme.finanzas.ingreso else MaterialTheme.finanzas.gasto
        )
    }
}
