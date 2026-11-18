package com.example.mibolsillo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * En la base de datos guardamos el icono como texto (p. ej. "comida").
 * Aquí traducimos ese texto al icono de Material que se dibuja en pantalla.
 */
object IconosApp {

    private val categorias: Map<String, ImageVector> = mapOf(
        "comida" to Icons.Filled.Restaurant,
        "transporte" to Icons.Filled.DirectionsBus,
        "estudios" to Icons.Filled.School,
        "entretenimiento" to Icons.Filled.Movie,
        "salud" to Icons.Filled.LocalHospital,
        "hogar" to Icons.Filled.Home,
        "ropa" to Icons.Filled.Checkroom,
        "servicios" to Icons.Filled.Bolt,
        "otros" to Icons.Filled.MoreHoriz,
        "salario" to Icons.Filled.Work,
        "mesada" to Icons.Filled.AccountBalanceWallet,
        "beca" to Icons.Filled.EmojiEvents,
        "ventas" to Icons.Filled.Storefront,
        "regalo" to Icons.Filled.CardGiftcard,
        "otros_ingresos" to Icons.Filled.AddCircle
    )

    /** Iconos que el usuario puede elegir para una meta de ahorro (clave -> icono). */
    val metas: List<Pair<String, ImageVector>> = listOf(
        "alcancia" to Icons.Filled.Savings,
        "laptop" to Icons.Filled.Laptop,
        "celular" to Icons.Filled.PhoneAndroid,
        "viaje" to Icons.Filled.Flight,
        "carro" to Icons.Filled.DirectionsCar,
        "casa" to Icons.Filled.Home,
        "estudios" to Icons.Filled.School,
        "regalo" to Icons.Filled.CardGiftcard,
        "emergencia" to Icons.Filled.HealthAndSafety,
        "musica" to Icons.Filled.MusicNote
    )

    fun categoria(clave: String): ImageVector = categorias[clave] ?: Icons.Filled.Category

    fun meta(clave: String): ImageVector = metas.firstOrNull { it.first == clave }?.second ?: Icons.Filled.Savings
}
