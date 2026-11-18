package com.example.mibolsillo.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mibolsillo.data.preferencias.ModoTema
import com.example.mibolsillo.ui.AppViewModelProvider
import com.example.mibolsillo.ui.components.PantallaConBarra
import com.example.mibolsillo.util.FormatoMoneda

@Composable
fun PerfilScreen(
    onSesionCerrada: () -> Unit,
    viewModel: PerfilViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val usuario by viewModel.usuario.collectAsStateWithLifecycle()
    val moneda by viewModel.moneda.collectAsStateWithLifecycle()
    val modoTema by viewModel.modoTema.collectAsStateWithLifecycle()

    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    var elegirMoneda by rememberSaveable { mutableStateOf(false) }
    var confirmarDemo by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.mensaje) {
        viewModel.mensaje?.let {
            snackbar.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }

    PantallaConBarra(titulo = "Perfil", snackbarHost = { SnackbarHost(snackbar) }) { modifier ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ---------- Encabezado con avatar ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFF26A69A), Color(0xFF00695C))))
                ) {
                    Text(
                        usuario?.nombre?.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(usuario?.nombre.orEmpty(), style = MaterialTheme.typography.titleLarge)
                    Text(usuario?.correo.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // ---------- Preferencias ----------
            Seccion("Preferencias") {
                ListItem(
                    headlineContent = { Text("Moneda") },
                    supportingContent = {
                        Text("$moneda · " + (FormatoMoneda.MONEDAS.firstOrNull { it.first == moneda }?.second ?: ""))
                    },
                    leadingContent = { Icon(Icons.Filled.Payments, contentDescription = null) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { elegirMoneda = true }
                )
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                Column(Modifier.padding(16.dp)) {
                    Text("Apariencia", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    val opciones = listOf(
                        Triple(ModoTema.SISTEMA, "Sistema", Icons.Filled.BrightnessAuto),
                        Triple(ModoTema.CLARO, "Claro", Icons.Filled.LightMode),
                        Triple(ModoTema.OSCURO, "Oscuro", Icons.Filled.DarkMode)
                    )
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        opciones.forEachIndexed { i, (modo, texto, icono) ->
                            SegmentedButton(
                                selected = modoTema == modo,
                                onClick = { viewModel.cambiarTema(modo) },
                                shape = SegmentedButtonDefaults.itemShape(i, opciones.size),
                                icon = { Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp)) }
                            ) { Text(texto) }
                        }
                    }
                }
            }

            // ---------- Datos ----------
            Seccion("Datos") {
                ListItem(
                    headlineContent = { Text("Cargar datos de demostración") },
                    supportingContent = { Text("6 meses de movimientos, presupuestos y metas de ejemplo (solo en cuentas sin movimientos)") },
                    leadingContent = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
                    trailingContent = {
                        if (viewModel.cargandoDemo) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable(enabled = !viewModel.cargandoDemo) { confirmarDemo = true }
                )
            }

            // ---------- Acerca de ----------
            Seccion("Acerca de") {
                ListItem(
                    headlineContent = { Text("MiBolsillo ${versionApp()}") },
                    supportingContent = {
                        Text("Proyecto de Laboratorio de Programación para Dispositivos Móviles. Tus datos se guardan solo en este teléfono.")
                    },
                    leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            OutlinedButton(
                onClick = { confirmarSalida = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Cerrar sesión")
            }
        }
    }

    if (elegirMoneda) {
        DialogoMoneda(
            actual = moneda,
            onElegir = {
                viewModel.cambiarMoneda(it)
                elegirMoneda = false
            },
            onCerrar = { elegirMoneda = false }
        )
    }

    if (confirmarDemo) {
        AlertDialog(
            onDismissRequest = { confirmarDemo = false },
            icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
            title = { Text("¿Cargar datos de ejemplo?") },
            text = { Text("Se agregarán movimientos de los últimos 6 meses, 4 presupuestos y 3 metas a tu cuenta. Útil para probar o presentar la app.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarDemo = false
                    viewModel.cargarDatosDemo()
                }) { Text("Cargar") }
            },
            dismissButton = { TextButton(onClick = { confirmarDemo = false }) { Text("Cancelar") } }
        )
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tus datos quedan guardados en este dispositivo.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    viewModel.cerrarSesion(onSesionCerrada)
                }) { Text("Cerrar sesión") }
            },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") } }
        )
    }
}

/** Grupo de opciones con título y fondo de tarjeta. */
@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Column {
        Text(
            titulo,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            contenido()
        }
    }
}

/** Lista de monedas con botones de radio. */
@Composable
private fun DialogoMoneda(actual: String, onElegir: (String) -> Unit, onCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Moneda") },
        text = {
            Column {
                FormatoMoneda.MONEDAS.forEach { (simbolo, nombre) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .selectable(selected = simbolo == actual, role = Role.RadioButton) { onElegir(simbolo) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = simbolo == actual, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text("$simbolo  ·  $nombre")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}

/** Versión de la app leída del sistema (definida en build.gradle.kts: versionName). */
@Composable
private fun versionApp(): String {
    val context = LocalContext.current
    return remember {
        runCatching { "v" + context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrDefault("")
    }
}
