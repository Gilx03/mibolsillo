package com.example.mibolsillo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.example.mibolsillo.util.FormatoMoneda

/**
 * Símbolo de moneda elegido por el usuario, disponible en cualquier Composable
 * sin tener que pasarlo como parámetro por todas las pantallas.
 * Se "provee" una sola vez en MainActivity.
 */
val LocalMoneda = compositionLocalOf { "RD$" }

/** Formatea un monto con la moneda actual: dinero(1500.0) -> "RD$ 1,500.00". */
@Composable
fun dinero(monto: Double): String = FormatoMoneda.formatear(monto, LocalMoneda.current)
