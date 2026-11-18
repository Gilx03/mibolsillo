package com.example.mibolsillo.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.ui.graphics.vector.ImageVector

/** Nombres (rutas) de todas las pantallas. Usar constantes evita errores de escritura. */
object Rutas {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTRO = "registro"

    // Pestañas de la barra inferior
    const val INICIO = "inicio"
    const val MOVIMIENTOS = "movimientos"
    const val ESTADISTICAS = "estadisticas"
    const val METAS = "metas"
    const val PERFIL = "perfil"

    // Pantallas secundarias (se abren desde Inicio)
    const val PRESUPUESTOS = "presupuestos"
    const val CONSEJOS = "consejos"

    // Formulario de movimiento con argumento opcional: "movimiento_form?id=5" edita, sin id crea uno nuevo
    const val MOVIMIENTO_FORM = "movimiento_form?id={id}"
    fun movimientoForm(id: Long? = null) = if (id == null) "movimiento_form" else "movimiento_form?id=$id"

    const val META_FORM = "meta_form?id={id}"
    fun metaForm(id: Long? = null) = if (id == null) "meta_form" else "meta_form?id=$id"
}

/** Una pestaña de la barra de navegación inferior. */
data class ItemBarraInferior(
    val ruta: String,
    val etiqueta: String,
    val iconoSeleccionado: ImageVector,
    val iconoNormal: ImageVector
)

val itemsBarraInferior = listOf(
    ItemBarraInferior(Rutas.INICIO, "Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    ItemBarraInferior(Rutas.MOVIMIENTOS, "Movimientos", Icons.Filled.SwapVert, Icons.Outlined.SwapVert),
    ItemBarraInferior(Rutas.ESTADISTICAS, "Estadísticas", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    ItemBarraInferior(Rutas.METAS, "Metas", Icons.Filled.Flag, Icons.Outlined.Flag),
    ItemBarraInferior(Rutas.PERFIL, "Perfil", Icons.Filled.Person, Icons.Outlined.Person)
)
