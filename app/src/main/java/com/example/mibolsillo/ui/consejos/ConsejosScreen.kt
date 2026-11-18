package com.example.mibolsillo.ui.consejos

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mibolsillo.data.consejos.Consejo
import com.example.mibolsillo.data.consejos.Consejos
import com.example.mibolsillo.data.consejos.TemaConsejo
import com.example.mibolsillo.ui.components.PantallaConBarra

/** Icono y color de cada tema de consejos. */
private fun iconoTema(tema: TemaConsejo): ImageVector = when (tema) {
    TemaConsejo.AHORRO -> Icons.Filled.Savings
    TemaConsejo.PRESUPUESTO -> Icons.Filled.PieChart
    TemaConsejo.GASTOS -> Icons.Filled.ShoppingCart
    TemaConsejo.DEUDAS -> Icons.Filled.CreditCard
    TemaConsejo.FUTURO -> Icons.AutoMirrored.Filled.TrendingUp
    TemaConsejo.SEGURIDAD -> Icons.Filled.Shield
}

private fun colorTema(tema: TemaConsejo): Color = when (tema) {
    TemaConsejo.AHORRO -> Color(0xFF26A69A)
    TemaConsejo.PRESUPUESTO -> Color(0xFF42A5F5)
    TemaConsejo.GASTOS -> Color(0xFFFF7043)
    TemaConsejo.DEUDAS -> Color(0xFFEC407A)
    TemaConsejo.FUTURO -> Color(0xFF7E57C2)
    TemaConsejo.SEGURIDAD -> Color(0xFF78909C)
}

/**
 * Pantalla de educación financiera.
 * No necesita ViewModel: el contenido es fijo (Consejos.lista) y el único estado
 * es el filtro elegido, que se guarda con rememberSaveable.
 */
@Composable
fun ConsejosScreen(onAtras: () -> Unit) {
    // Guardamos el nombre del tema (String) porque rememberSaveable lo conserva al rotar la pantalla
    var temaElegido by rememberSaveable { mutableStateOf<String?>(null) }
    val consejoDelDia = Consejos.delDia()
    val lista = Consejos.lista.filter { temaElegido == null || it.tema.name == temaElegido }

    PantallaConBarra(titulo = "Consejos", onAtras = onAtras) { modifier ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = modifier.fillMaxSize()
        ) {
            item {
                TarjetaDestacada(consejoDelDia, Modifier.padding(horizontal = 16.dp))
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    FilterChip(
                        selected = temaElegido == null,
                        onClick = { temaElegido = null },
                        label = { Text("Todos") }
                    )
                    TemaConsejo.entries.forEach { tema ->
                        FilterChip(
                            selected = temaElegido == tema.name,
                            onClick = { temaElegido = tema.name },
                            label = { Text(tema.etiqueta) },
                            leadingIcon = { Icon(iconoTema(tema), contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }

            items(lista, key = { it.id }) { consejo ->
                TarjetaConsejo(
                    consejo,
                    Modifier
                        .padding(horizontal = 16.dp)
                        .animateItem()
                )
            }
        }
    }
}

@Composable
private fun TarjetaDestacada(consejo: Consejo, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(8.dp))
                Text("Consejo del día", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
            Spacer(Modifier.height(8.dp))
            Text(consejo.titulo, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(Modifier.height(6.dp))
            Text(consejo.texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }
}

/** Tarjeta que se expande al tocarla para leer el consejo completo. */
@Composable
private fun TarjetaConsejo(consejo: Consejo, modifier: Modifier = Modifier) {
    var expandida by rememberSaveable(consejo.id) { mutableStateOf(false) }
    val color = colorTema(consejo.tema)

    Card(
        onClick = { expandida = !expandida },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize() // anima el cambio de alto al expandir
    ) {
        Row(Modifier.padding(16.dp)) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.16f), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(iconoTema(consejo.tema), contentDescription = null, tint = color)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(consejo.tema.etiqueta.uppercase(), style = MaterialTheme.typography.labelSmall, color = color)
                Text(consejo.titulo, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    consejo.texto,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expandida) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                if (expandida) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expandida) "Mostrar menos" else "Leer más",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
