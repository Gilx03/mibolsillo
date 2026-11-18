package com.example.mibolsillo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * Tema de MiBolsillo.
 * No usamos "colores dinámicos" de Android 12 a propósito: queremos que la app
 * siempre tenga su identidad verde/turquesa, sin importar el fondo de pantalla del usuario.
 */

private val EsquemaClaro = lightColorScheme(
    primary = VerdeTurquesa,
    onPrimary = Color.White,
    primaryContainer = VerdeTurquesaClaro,
    onPrimaryContainer = VerdeTurquesaOscuroTexto,
    secondary = VerdeHoja,
    onSecondary = Color.White,
    secondaryContainer = VerdeHojaContenedor,
    onSecondaryContainer = VerdeHojaTexto,
    tertiary = Dorado,
    onTertiary = Color.White,
    tertiaryContainer = DoradoContenedor,
    onTertiaryContainer = DoradoTexto,
    background = FondoClaro,
    onBackground = TextoClaro,
    surface = FondoClaro,
    onSurface = TextoClaro,
    surfaceVariant = Color(0xFFDAE5E1),
    onSurfaceVariant = Color(0xFF3F4946),
    outline = Color(0xFF6F7976),
    outlineVariant = Color(0xFFBEC9C5),
    surfaceDim = Color(0xFFD5DBD9),
    surfaceBright = FondoClaro,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFEFF5F3),
    surfaceContainer = Color(0xFFE9EFED),
    surfaceContainerHigh = Color(0xFFE3EAE7),
    surfaceContainerHighest = Color(0xFFDDE4E1)
)

private val EsquemaOscuro = darkColorScheme(
    primary = TurquesaNeon,
    onPrimary = TurquesaNeonTexto,
    primaryContainer = TurquesaContenedorOscuro,
    onPrimaryContainer = VerdeTurquesaClaro,
    secondary = VerdeMenta,
    onSecondary = VerdeMentaTexto,
    secondaryContainer = VerdeContenedorOscuro,
    onSecondaryContainer = Color(0xFFABF2BF),
    tertiary = DoradoClaro,
    onTertiary = DoradoClaroTexto,
    tertiaryContainer = DoradoContenedorOscuro,
    onTertiaryContainer = DoradoContenedor,
    background = FondoOscuro,
    onBackground = TextoOscuro,
    surface = FondoOscuro,
    onSurface = TextoOscuro,
    surfaceVariant = Color(0xFF3F4946),
    onSurfaceVariant = Color(0xFFBEC9C5),
    outline = Color(0xFF899390),
    outlineVariant = Color(0xFF3F4946),
    surfaceDim = FondoOscuro,
    surfaceBright = Color(0xFF343B39),
    surfaceContainerLowest = Color(0xFF09100E),
    surfaceContainerLow = Color(0xFF171D1B),
    surfaceContainer = Color(0xFF1B211F),
    surfaceContainerHigh = Color(0xFF252B2A),
    surfaceContainerHighest = Color(0xFF303634)
)

/** Colores propios de la app que Material 3 no trae (ingreso, gasto, advertencia). */
@Immutable
data class ColoresFinanzas(
    val ingreso: Color,
    val gasto: Color,
    val advertencia: Color
)

private val FinanzasClaro = ColoresFinanzas(IngresoClaro, GastoClaro, AdvertenciaClaro)
private val FinanzasOscuro = ColoresFinanzas(IngresoOscuro, GastoOscuro, AdvertenciaOscuro)

val LocalColoresFinanzas = staticCompositionLocalOf { FinanzasClaro }

/** Acceso cómodo desde cualquier pantalla: MaterialTheme.finanzas.gasto */
val MaterialTheme.finanzas: ColoresFinanzas
    @Composable get() = LocalColoresFinanzas.current

@Composable
fun MiBolsilloTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalColoresFinanzas provides if (darkTheme) FinanzasOscuro else FinanzasClaro) {
        MaterialTheme(
            colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
            typography = Typography,
            content = content
        )
    }
}
