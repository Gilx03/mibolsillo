package com.example.mibolsillo.ui.movimientos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mibolsillo.data.local.entity.TipoMovimiento
import com.example.mibolsillo.ui.AppViewModelProvider
import com.example.mibolsillo.ui.components.EstadoVacio
import com.example.mibolsillo.ui.components.FilaMovimiento
import com.example.mibolsillo.ui.components.IconoCategoria
import com.example.mibolsillo.ui.components.PantallaConBarra
import com.example.mibolsillo.ui.components.SelectorMes
import com.example.mibolsillo.ui.components.dinero
import com.example.mibolsillo.ui.theme.finanzas
import com.example.mibolsillo.util.Fechas

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MovimientosScreen(
    onAgregar: () -> Unit,
    onEditar: (Long) -> Unit,
    viewModel: MovimientosViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()
    val hayFiltros = estado.filtros.tipo != null || estado.filtros.categoriaId != null || busqueda.isNotBlank()

    PantallaConBarra(
        titulo = "Movimientos",
        botonFlotante = {
            FloatingActionButton(onClick = onAgregar) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar movimiento")
            }
        }
    ) { modifier ->
        Column(modifier.fillMaxSize()) {
            // ---------- Búsqueda ----------
            OutlinedTextField(
                value = busqueda,
                onValueChange = viewModel::buscar,
                placeholder = { Text("Buscar por nota o categoría") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (busqueda.isNotEmpty()) {
                        IconButton(onClick = { viewModel.buscar("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Borrar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            // ---------- Mes ----------
            SelectorMes(
                mes = estado.filtros.mes,
                onAnterior = viewModel::mesAnterior,
                onSiguiente = viewModel::mesSiguiente
            )

            // ---------- Filtros de tipo y categoría ----------
            FiltrosChips(
                estado = estado,
                onTipo = viewModel::cambiarTipo,
                onCategoria = viewModel::cambiarCategoria
            )

            ResumenFiltrado(estado.totalIngresos, estado.totalGastos)

            // ---------- Lista ----------
            when {
                estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                estado.vacio && hayFiltros -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    EstadoVacio(
                        icono = Icons.Filled.SearchOff,
                        titulo = "Sin resultados",
                        mensaje = "No hay movimientos que coincidan con los filtros en ${Fechas.nombreMes(estado.filtros.mes)}.",
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = viewModel::limpiarFiltros, modifier = Modifier.padding(bottom = 32.dp)) {
                        Text("Quitar filtros")
                    }
                }

                estado.vacio -> EstadoVacio(
                    icono = Icons.AutoMirrored.Filled.ReceiptLong,
                    titulo = "Aún no tienes movimientos",
                    mensaje = "Toca el botón + para registrar tu primer ingreso o gasto de ${Fechas.nombreMes(estado.filtros.mes)}."
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = 96.dp), // espacio para que el FAB no tape el último
                    modifier = Modifier.fillMaxSize()
                ) {
                    estado.grupos.forEach { grupo ->
                        stickyHeader(key = "h_${grupo.fecha}") {
                            EncabezadoDia(Fechas.etiquetaDia(grupo.fecha))
                        }
                        items(grupo.movimientos, key = { it.movimiento.id }) { item ->
                            FilaMovimiento(
                                item = item,
                                onClick = { onEditar(item.movimiento.id) },
                                mostrarFecha = false,
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun FiltrosChips(
    estado: MovimientosUiState,
    onTipo: (TipoMovimiento?) -> Unit,
    onCategoria: (Long?) -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }
    val f = estado.filtros
    // Solo se ofrecen las categorías del tipo elegido (o todas si no hay tipo)
    val categoriasDisponibles = estado.categorias.filter { f.tipo == null || it.tipo == f.tipo }
    val categoriaElegida = estado.categorias.firstOrNull { it.id == f.categoriaId }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        listOf(null to "Todos", TipoMovimiento.INGRESO to "Ingresos", TipoMovimiento.GASTO to "Gastos")
            .forEach { (tipo, texto) ->
                FilterChip(
                    selected = f.tipo == tipo,
                    onClick = { onTipo(tipo) },
                    label = { Text(texto) },
                    leadingIcon = if (f.tipo == tipo) {
                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }

        Box {
            FilterChip(
                selected = categoriaElegida != null,
                onClick = { menuAbierto = true },
                label = { Text(categoriaElegida?.nombre ?: "Categoría") },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }
            )
            DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                DropdownMenuItem(
                    text = { Text("Todas las categorías") },
                    onClick = {
                        onCategoria(null)
                        menuAbierto = false
                    }
                )
                categoriasDisponibles.forEach { c ->
                    DropdownMenuItem(
                        text = { Text(c.nombre) },
                        leadingIcon = { IconoCategoria(c.icono, c.color, tamano = 28.dp) },
                        onClick = {
                            onCategoria(c.id)
                            menuAbierto = false
                        }
                    )
                }
            }
        }
    }
}

/** Totales de lo que se está viendo (respetando los filtros). */
@Composable
private fun ResumenFiltrado(ingresos: Double, gastos: Double) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp)
    ) {
        Row(Modifier.padding(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Ingresos", style = MaterialTheme.typography.labelMedium)
                Text(
                    "+" + dinero(ingresos),
                    color = MaterialTheme.finanzas.ingreso,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("Gastos", style = MaterialTheme.typography.labelMedium)
                Text(
                    "-" + dinero(gastos),
                    color = MaterialTheme.finanzas.gasto,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EncabezadoDia(texto: String) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                texto,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.size(2.dp))
        }
    }
}
